package com.volt.module.modules.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.render.EventRender2D;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.font.util.RendererUtils;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

public final class TrapSave extends Module {
    private static final int SCAN_INTERVAL_MS = 500;
    private static final int WARNING_DURATION_MS = 3000;
    private static final String CHAT_PREFIX = "<red>[TrapSave]</red> ";

    private static final Box REDSTONE_WIRE_BOUNDS = new Box(0, 0, 0, 1, 0.0625, 1);
    private static final Box TORCH_BOUNDS = new Box(0.375, 0, 0.375, 0.625, 0.625, 0.625);
    private static final Box LEVER_BOUNDS = new Box(0.25, 0, 0.25, 0.75, 0.6, 0.75);
    private static final Box PRESSURE_PLATE_BOUNDS = new Box(0.0625, 0, 0.0625, 0.9375, 0.0625, 0.9375);
    private static final Box TRIPWIRE_HOOK_BOUNDS = new Box(0.375, 0, 0.125, 0.625, 0.625, 0.875);
    private static final Box THIN_BLOCK_BOUNDS = new Box(0, 0, 0, 1, 0.125, 1);
    private static final Box FULL_BLOCK_BOUNDS = new Box(0, 0, 0, 1, 1, 1);
    private static final Box ARMOR_STAND_BOUNDS = new Box(-0.5, 0, -0.5, 0.5, 1.975, 0.5);

    private static final Predicate<ArmorStandEntity> ARMOR_STAND_FILTER = Objects::nonNull;

    private final NumberSetting scanRadius = new NumberSetting("Scan Radius", 3, 20, 8, 1);
    private final NumberSetting scanHeight = new NumberSetting("Scan Height", 0, 30, 8, 1);
    private final NumberSetting maxExpansion = new NumberSetting("Max Expansion", 0, 100, 20, 1);
    private final BooleanSetting soundAlert = new BooleanSetting("Sound Alert", true);
    private final BooleanSetting showWarning = new BooleanSetting("Show Warning", true);
    private final NumberSetting outlineWidth = new NumberSetting("Outline Width", 1, 5, 2, 1);
    private final NumberSetting tntColor = new NumberSetting("TNT Color", 0, 16777215, 0xFFA500, 1);
    private final NumberSetting redstoneColor = new NumberSetting("Redstone Color", 0, 16777215, 0xFF0000, 1);
    private final NumberSetting pistonColor = new NumberSetting("Piston Color", 0, 16777215, 0x8B4513, 1);
    private final NumberSetting leverColor = new NumberSetting("Lever Color", 0, 16777215, 0xFFFF00, 1);
    private final NumberSetting armorStandColor = new NumberSetting("Armor Stand Color", 0, 16777215, 0x00FFFF, 1);

    private final TimerUtil scanTimer = new TimerUtil();
    private final TimerUtil warningTimer = new TimerUtil();
    private final List<TrapCluster> detectedTraps = new ArrayList<>();

    @EventHandler
    private void onEventRender3D(EventRender3D event) {
        if (!isValidGameState() || detectedTraps.isEmpty()) return;

        renderTrapOutlines(event.getMatrixStack(), mc.gameRenderer.getCamera().getPos());
    }

    ;
    private boolean trapDetected;
    private String detectedTrapType = "";
    private int trapCount;

    @EventHandler
    private void onEventRender2D(EventRender2D event) {
        if (!isValidGameState() || !showWarning.getValue() || !trapDetected) return;

        renderWarningOverlay(event);
    }

    ;
    private BlockPos lastScanPosition;
    private int lastScanRadius;
    private int lastScanHeight;

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (!isValidGameState()) return;

        if (scanTimer.hasElapsedTime(SCAN_INTERVAL_MS)) {
            performTrapScan();
            scanTimer.reset();
        }

        if (trapDetected && warningTimer.hasElapsedTime(WARNING_DURATION_MS)) {
            resetTrapDetection();
        }
    }

    ;

    public TrapSave() {
        super("Trap Save", "Detects trap blocks and armor stands around the player", -1, Category.PLAYER);
        this.addSettings(scanRadius, scanHeight, maxExpansion, soundAlert, showWarning, outlineWidth,
                tntColor, redstoneColor, pistonColor, leverColor, armorStandColor);
    }

    private boolean isValidGameState() {
        return mc.player != null && mc.world != null && isEnabled();
    }

    private void performTrapScan() {
        final BlockPos playerPos = mc.player.getBlockPos();
        final int radius = scanRadius.getValueInt();
        final int height = scanHeight.getValueInt();

        if (shouldSkipScan(playerPos, radius, height)) return;

        clearDetectedTraps();
        final List<TrapCluster> foundTraps = scanForTrapClusters(playerPos, radius, height);

        updateScanCache(playerPos, radius, height);

        if (!foundTraps.isEmpty() && !trapDetected) {
            handleTrapDetection(foundTraps);
        }
    }

    private boolean shouldSkipScan(BlockPos currentPos, int currentRadius, int currentHeight) {
        return lastScanPosition != null &&
                lastScanPosition.equals(currentPos) &&
                lastScanRadius == currentRadius &&
                lastScanHeight == currentHeight;
    }

    private void updateScanCache(BlockPos position, int radius, int height) {
        lastScanPosition = position;
        lastScanRadius = radius;
        lastScanHeight = height;
    }

    private List<TrapCluster> scanForTrapClusters(BlockPos playerPos, int radius, int height) {
        final Set<BlockPos> scannedBlocks = new HashSet<>();
        final List<TrapCluster> clusters = new ArrayList<>();
        final int radiusSquared = radius * radius;
        final boolean infiniteHeight = height == 0;
        final int minY = infiniteHeight ? mc.world.getBottomY() : playerPos.getY() - height;
        final int maxY = infiniteHeight ? mc.world.getTopY() - 1 : playerPos.getY() + height;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radiusSquared) continue;

                for (int y = minY; y <= maxY; y++) {
                    final BlockPos scanPos = new BlockPos(playerPos.getX() + x, y, playerPos.getZ() + z);

                    if (scannedBlocks.contains(scanPos)) continue;

                    final Block block = mc.world.getBlockState(scanPos).getBlock();
                    final TrapType trapType = getTrapType(block);

                    if (trapType != TrapType.NONE) {
                        final TrapCluster cluster = expandTrapCluster(scanPos, trapType, scannedBlocks);
                        if (!cluster.isEmpty()) {
                            clusters.add(cluster);
                        }
                    }
                }
            }
        }

        scanArmorStands(playerPos, radius, height, clusters);
        detectedTraps.addAll(clusters);

        return clusters;
    }

    private TrapCluster expandTrapCluster(BlockPos startPos, TrapType startType, Set<BlockPos> globalScanned) {
        final TrapCluster cluster = new TrapCluster(startType);
        final Queue<BlockPos> toExpand = new ArrayDeque<>();
        final Set<BlockPos> clusterScanned = new HashSet<>();
        final int maxBlocks = maxExpansion.getValueInt();
        final boolean infiniteExpansion = maxBlocks == 0;
        final int safetyLimit = 1000;

        toExpand.offer(startPos);

        while (!toExpand.isEmpty() &&
                (infiniteExpansion ? cluster.size() < safetyLimit : cluster.size() < maxBlocks)) {
            final BlockPos current = toExpand.poll();

            if (clusterScanned.contains(current) || globalScanned.contains(current)) continue;

            clusterScanned.add(current);
            globalScanned.add(current);

            final Block block = mc.world.getBlockState(current).getBlock();
            final TrapType trapType = getTrapType(block);

            if (trapType != TrapType.NONE && isRelatedTrapType(startType, trapType)) {
                cluster.addBlock(current, trapType);

                for (BlockPos neighbor : getNeighbors(current)) {
                    if (!clusterScanned.contains(neighbor)) {
                        toExpand.offer(neighbor);
                    }
                }
            }
        }

        return cluster;
    }

    private boolean isRelatedTrapType(TrapType original, TrapType candidate) {
        if (original == candidate) return true;

        final Set<TrapType> redstoneGroup = EnumSet.of(TrapType.REDSTONE, TrapType.LEVER, TrapType.PRESSURE_PLATE, TrapType.TRIPWIRE);
        final Set<TrapType> mechanicalGroup = EnumSet.of(TrapType.PISTON, TrapType.DISPENSER, TrapType.TNT);

        if (redstoneGroup.contains(original) && redstoneGroup.contains(candidate)) return true;
        if (mechanicalGroup.contains(original) && mechanicalGroup.contains(candidate)) return true;
        if (redstoneGroup.contains(original) && mechanicalGroup.contains(candidate)) return true;
        return mechanicalGroup.contains(original) && redstoneGroup.contains(candidate);
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        return Arrays.asList(
                pos.north(), pos.south(), pos.east(), pos.west(),
                pos.up(), pos.down(),
                pos.north().up(), pos.south().up(), pos.east().up(), pos.west().up(),
                pos.north().down(), pos.south().down(), pos.east().down(), pos.west().down()
        );
    }

    private void scanArmorStands(BlockPos playerPos, int radius, int height, List<TrapCluster> clusters) {
        final boolean infiniteHeight = height == 0;
        final int minY = infiniteHeight ? mc.world.getBottomY() : playerPos.getY() - height;
        final int maxY = infiniteHeight ? mc.world.getTopY() - 1 : playerPos.getY() + height;

        final Vec3d minPos = new Vec3d(playerPos.getX() - radius, minY, playerPos.getZ() - radius);
        final Vec3d maxPos = new Vec3d(playerPos.getX() + radius, maxY, playerPos.getZ() + radius);
        final List<ArmorStandEntity> armorStands = mc.world.getEntitiesByClass(
                ArmorStandEntity.class, new Box(minPos, maxPos), ARMOR_STAND_FILTER
        );

        if (!armorStands.isEmpty()) {
            final TrapCluster armorStandCluster = new TrapCluster(TrapType.ARMOR_STAND);
            armorStands.forEach(armorStandCluster::addArmorStand);
            clusters.add(armorStandCluster);
        }
    }

    private TrapType getTrapType(Block block) {
        if (block == Blocks.TNT) return TrapType.TNT;
        if (block == Blocks.LEVER) return TrapType.LEVER;
        if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON) return TrapType.PISTON;
        if (isRedstoneBlock(block)) return TrapType.REDSTONE;
        if (isPressurePlate(block)) return TrapType.PRESSURE_PLATE;
        if (block == Blocks.TRIPWIRE_HOOK) return TrapType.TRIPWIRE;
        if (block == Blocks.DISPENSER || block == Blocks.DROPPER) return TrapType.DISPENSER;
        return TrapType.NONE;
    }

    private boolean isRedstoneBlock(Block block) {
        return block == Blocks.REDSTONE_WIRE || block == Blocks.REDSTONE_TORCH ||
                block == Blocks.REDSTONE_WALL_TORCH || block == Blocks.REDSTONE_BLOCK ||
                block == Blocks.OBSERVER || block == Blocks.REPEATER || block == Blocks.COMPARATOR;
    }

    private boolean isPressurePlate(Block block) {
        return block == Blocks.STONE_PRESSURE_PLATE ||
                block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }

    private void handleTrapDetection(List<TrapCluster> traps) {
        trapDetected = true;
        detectedTrapType = traps.get(0).getPrimaryType().getDisplayName();
        trapCount = traps.size();
        warningTimer.reset();

        if (soundAlert.getValue()) {
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 2.0f));
        }

        ChatUtils.addChatMessage(CHAT_PREFIX + "Trap detected: " + detectedTrapType + " (" + trapCount + " traps)");
    }

    private void renderWarningOverlay(EventRender2D event) {
        final int centerX = event.getWidth() / 2;
        final int warningY = 50;
        final int warningWidth = 210;
        final int warningHeight = 40;

        final int backgroundX = centerX - warningWidth / 2;

        final Component warningComponent = MiniMessage.miniMessage().deserialize("<red><bold>TRAP DETECTED!</bold></red>");
        final Component detailComponent = MiniMessage.miniMessage().deserialize("<white>" + (detectedTrapType == null ? "" : detectedTrapType) + " (" + trapCount + " traps)</white>");

        final String warningJson = GsonComponentSerializer.gson().serialize(warningComponent);
        final String detailJson = GsonComponentSerializer.gson().serialize(detailComponent);

        final Text warningText = Text.Serialization.fromJson(warningJson, mc.world.getRegistryManager());
        final Text detailText = Text.Serialization.fromJson(detailJson, mc.world.getRegistryManager());

        event.getContext().fill(backgroundX - 10, warningY - 10, backgroundX + warningWidth, warningY + warningHeight, 0x80FF0000);
        event.getContext().drawText(mc.textRenderer, warningText, backgroundX, warningY, 0xFFFFFF, true);
        event.getContext().drawText(mc.textRenderer, detailText, backgroundX, warningY + 15, 0xFFFFFF, true);
    }

    private void renderTrapOutlines(MatrixStack matrices, Vec3d cameraPos) {
        RendererUtils.setupRender();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        for (TrapCluster cluster : detectedTraps) {
            renderTrapCluster(matrices, cluster, cameraPos);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RendererUtils.endRender();
    }

    private void renderTrapCluster(MatrixStack matrices, TrapCluster cluster, Vec3d cameraPos) {
        final Color clusterColor = getCustomColor(cluster.getPrimaryType());

        cluster.getBlocks().forEach((blockPos, trapType) -> {
            final Block block = mc.world.getBlockState(blockPos).getBlock();
            renderBlockOutline(matrices, blockPos, block, clusterColor, cameraPos);
        });

        cluster.getArmorStands().forEach(armorStand -> {
            renderArmorStandOutline(matrices, armorStand, clusterColor, cameraPos);
        });
    }

    private void renderBlockOutline(MatrixStack matrices, BlockPos blockPos, Block block, Color color, Vec3d cameraPos) {
        final Vec3d relativePos = getRelativePosition(blockPos, cameraPos);
        final Box bounds = getBlockBounds(block);

        matrices.push();
        matrices.translate(relativePos.x, relativePos.y, relativePos.z);
        RenderSystem.lineWidth(outlineWidth.getValueFloat());
        renderOutlineBox(matrices, bounds, color);
        matrices.pop();
    }

    private void renderArmorStandOutline(MatrixStack matrices, ArmorStandEntity armorStand, Color color, Vec3d cameraPos) {
        final Vec3d relativePos = getRelativePosition(armorStand.getPos(), cameraPos);

        matrices.push();
        matrices.translate(relativePos.x, relativePos.y, relativePos.z);
        RenderSystem.lineWidth(outlineWidth.getValueFloat());
        renderOutlineBox(matrices, ARMOR_STAND_BOUNDS, color);
        matrices.pop();
    }

    private Color getCustomColor(TrapType trapType) {
        final int alpha = 200;
        switch (trapType) {
            case TNT:
                return new Color(tntColor.getValueInt() | (alpha << 24), true);
            case REDSTONE:
                return new Color(redstoneColor.getValueInt() | (alpha << 24), true);
            case PISTON:
                return new Color(pistonColor.getValueInt() | (alpha << 24), true);
            case LEVER:
            case PRESSURE_PLATE:
                return new Color(leverColor.getValueInt() | (alpha << 24), true);
            case ARMOR_STAND:
                return new Color(armorStandColor.getValueInt() | (alpha << 24), true);
            case TRIPWIRE:
                return new Color(0x800080 | (alpha << 24), true);
            case DISPENSER:
                return new Color(0x696969 | (alpha << 24), true);
            default:
                return new Color(0xFFFFFF | (alpha << 24), true);
        }
    }

    private Vec3d getRelativePosition(BlockPos blockPos, Vec3d cameraPos) {
        return new Vec3d(blockPos.getX() - cameraPos.x, blockPos.getY() - cameraPos.y, blockPos.getZ() - cameraPos.z);
    }

    private Vec3d getRelativePosition(Vec3d worldPos, Vec3d cameraPos) {
        return worldPos.subtract(cameraPos);
    }

    private Box getBlockBounds(Block block) {
        if (block == Blocks.REDSTONE_WIRE) return REDSTONE_WIRE_BOUNDS;
        if (block == Blocks.REDSTONE_TORCH || block == Blocks.REDSTONE_WALL_TORCH) return TORCH_BOUNDS;
        if (block == Blocks.LEVER) return LEVER_BOUNDS;
        if (isPressurePlate(block)) return PRESSURE_PLATE_BOUNDS;
        if (block == Blocks.TRIPWIRE_HOOK) return TRIPWIRE_HOOK_BOUNDS;
        if (block == Blocks.REPEATER || block == Blocks.COMPARATOR) return THIN_BLOCK_BOUNDS;
        return FULL_BLOCK_BOUNDS;
    }

    private void renderOutlineBox(MatrixStack matrices, Box box, Color color) {
        final Matrix4f matrix = matrices.peek().getPositionMatrix();
        final BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        final float[] rgba = {color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f};
        final float[] bounds = {(float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ};

        drawBoxEdges(buffer, matrix, bounds, rgba);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawBoxEdges(BufferBuilder buffer, Matrix4f matrix, float[] bounds, float[] rgba) {
        final float minX = bounds[0], minY = bounds[1], minZ = bounds[2];
        final float maxX = bounds[3], maxY = bounds[4], maxZ = bounds[5];
        final float r = rgba[0], g = rgba[1], b = rgba[2], a = rgba[3];

        addEdge(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        addEdge(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        addEdge(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        addEdge(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        addEdge(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        addEdge(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        addEdge(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        addEdge(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        addEdge(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        addEdge(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        addEdge(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        addEdge(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private void addEdge(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
    }

    private void clearDetectedTraps() {
        detectedTraps.clear();
    }

    private void resetTrapDetection() {
        trapDetected = false;
        detectedTrapType = "";
        trapCount = 0;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        clearDetectedTraps();
        resetTrapDetection();
        lastScanPosition = null;
    }

    @Override
    public void onDisable() {
        resetTrapDetection();
        clearDetectedTraps();
        lastScanPosition = null;
        super.onDisable();
    }

    private enum TrapType {
        NONE("", Color.WHITE),
        TNT("TNT", Color.ORANGE),
        LEVER("Lever", Color.YELLOW),
        PISTON("Piston", Color.decode("#8B4513")),
        REDSTONE("Redstone", Color.RED),
        PRESSURE_PLATE("Pressure Plate", Color.YELLOW),
        TRIPWIRE("Tripwire", Color.decode("#800080")),
        DISPENSER("Dispenser", Color.GRAY),
        ARMOR_STAND("Armor Stand", Color.CYAN);

        private final String displayName;
        private final Color defaultColor;

        TrapType(String displayName, Color defaultColor) {
            this.displayName = displayName;
            this.defaultColor = defaultColor;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private static class TrapCluster {
        private final TrapType primaryType;
        private final Map<BlockPos, TrapType> blocks = new HashMap<>();
        private final List<ArmorStandEntity> armorStands = new ArrayList<>();

        public TrapCluster(TrapType primaryType) {
            this.primaryType = primaryType;
        }

        public void addBlock(BlockPos pos, TrapType type) {
            blocks.put(pos, type);
        }

        public void addArmorStand(ArmorStandEntity armorStand) {
            armorStands.add(armorStand);
        }

        public TrapType getPrimaryType() {
            return primaryType;
        }

        public Map<BlockPos, TrapType> getBlocks() {
            return blocks;
        }

        public List<ArmorStandEntity> getArmorStands() {
            return armorStands;
        }

        public boolean isEmpty() {
            return blocks.isEmpty() && armorStands.isEmpty();
        }

        public int size() {
            return blocks.size() + armorStands.size();
        }
    }
}
