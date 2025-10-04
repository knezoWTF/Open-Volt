package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OreESP extends Module {

    private static final int SCAN_INTERVAL_MS = 500;
    private final ModeSetting renderMode = new ModeSetting("Render Mode", "Outline", "Outline", "Filled", "Both");
    private final NumberSetting scanRadius = new NumberSetting("Scan Radius", 1, 64, 32, 1);
    private final NumberSetting scanHeight = new NumberSetting("Scan Height", 1, 32, 16, 1);
    private final NumberSetting outlineWidth = new NumberSetting("Outline Width", 0.5, 5.0, 2.0, 0.5);
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", true);
    private final NumberSetting transparency = new NumberSetting("Transparency", 0.1, 1.0, 0.5, 0.1);
    private final BooleanSetting enableDiamond = new BooleanSetting("Enable Diamond", true);
    private final BooleanSetting enableEmerald = new BooleanSetting("Enable Emerald", true);
    private final BooleanSetting enableGold = new BooleanSetting("Enable Gold", true);
    private final BooleanSetting enableIron = new BooleanSetting("Enable Iron", true);
    private final BooleanSetting enableCoal = new BooleanSetting("Enable Coal", true);
    private final BooleanSetting enableCopper = new BooleanSetting("Enable Copper", true);
    private final BooleanSetting enableRedstone = new BooleanSetting("Enable Redstone", true);
    private final BooleanSetting enableLapis = new BooleanSetting("Enable Lapis", true);
    private final BooleanSetting enableNetherite = new BooleanSetting("Enable Netherite", false);
    private final BooleanSetting enableNetherGold = new BooleanSetting("Enable Nether Gold", false);
    private final BooleanSetting enableNetherQuartz = new BooleanSetting("Enable Nether Quartz", false);
    private final TimerUtil scanTimer = new TimerUtil();
    private final Map<BlockPos, OreType> detectedOres = new ConcurrentHashMap<>();
    private BlockPos lastPlayerPos;

    public OreESP() {
        super("Ore ESP", "Highlights ore blocks through walls", -1, Category.RENDER);

        this.addSettings(
                renderMode, scanRadius, scanHeight, outlineWidth, throughWalls, transparency,
                enableDiamond, enableEmerald, enableGold, enableIron, enableCoal, enableCopper,
                enableRedstone, enableLapis, enableNetherite, enableNetherGold, enableNetherQuartz
        );
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (scanTimer.hasElapsedTime(SCAN_INTERVAL_MS)) {
            performOreScan();
            scanTimer.reset();
        }
    }

    @EventHandler
    private void onEventRender3D(EventRender3D event) {
        if (isNull() || detectedOres.isEmpty()) return;

        MatrixStack matrices = event.getMatrixStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        if (!throughWalls.getValue()) {
            RenderSystem.enableDepthTest();
        }

        List<BlockPos> toRemove = null;
        for (Map.Entry<BlockPos, OreType> entry : detectedOres.entrySet()) {
            BlockPos pos = entry.getKey();
            OreType oreType = entry.getValue();

            OreType liveType = getOreType(mc.world.getBlockState(pos).getBlock());
            if (liveType == OreType.NONE || liveType != oreType) {
                if (toRemove == null) toRemove = new ArrayList<>();
                toRemove.add(pos);
                continue;
            }

            if (isOreEnabled(oreType)) {
                Color color = getOreColor(oreType);
                renderOreBlock(matrices, pos, color);
            }
        }
        if (toRemove != null) {
            for (BlockPos pos : toRemove) detectedOres.remove(pos);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void performOreScan() {
        if (isNull()) return;

        BlockPos playerPos = mc.player.getBlockPos();

        if (lastPlayerPos == null || playerPos.getSquaredDistance(lastPlayerPos) > 100) {
            detectedOres.clear();
            lastPlayerPos = playerPos;
        }

        int radius = scanRadius.getValueInt();
        int height = scanHeight.getValueInt();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -height; y <= height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos scanPos = playerPos.add(x, y, z);
                    final Block block = mc.world.getBlockState(scanPos).getBlock();
                    final OreType oreType = getOreType(block);

                    if (oreType != OreType.NONE && isOreEnabled(oreType)) {
                        detectedOres.put(scanPos, oreType);
                    }
                }
            }
        }
    }

    private OreType getOreType(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return OreType.DIAMOND;
        }

        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return OreType.EMERALD;
        }

        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return OreType.GOLD;
        }

        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return OreType.IRON;
        }


        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return OreType.COAL;
        }


        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return OreType.COPPER;
        }


        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return OreType.REDSTONE;
        }


        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return OreType.LAPIS;
        }


        if (block == Blocks.ANCIENT_DEBRIS) {
            return OreType.NETHERITE;
        }

        if (block == Blocks.NETHER_GOLD_ORE) {
            return OreType.NETHER_GOLD;
        }

        if (block == Blocks.NETHER_QUARTZ_ORE) {
            return OreType.NETHER_QUARTZ;
        }

        return OreType.NONE;
    }

    private boolean isOreEnabled(OreType oreType) {
        return switch (oreType) {
            case DIAMOND -> enableDiamond.getValue();
            case EMERALD -> enableEmerald.getValue();
            case GOLD -> enableGold.getValue();
            case IRON -> enableIron.getValue();
            case COAL -> enableCoal.getValue();
            case COPPER -> enableCopper.getValue();
            case REDSTONE -> enableRedstone.getValue();
            case LAPIS -> enableLapis.getValue();
            case NETHERITE -> enableNetherite.getValue();
            case NETHER_GOLD -> enableNetherGold.getValue();
            case NETHER_QUARTZ -> enableNetherQuartz.getValue();
            default -> false;
        };
    }

    private Color getOreColor(OreType oreType) {
        return switch (oreType) {
            case DIAMOND -> new Color(0x5DADE2);
            case EMERALD -> new Color(0x2ECC71);
            case GOLD -> new Color(0xF1C40F);
            case IRON -> new Color(0xD5DBDB);
            case COAL -> new Color(0x2C3E50);
            case COPPER -> new Color(0xE67E22);
            case REDSTONE -> new Color(0xE74C3C);
            case LAPIS -> new Color(0x3498DB);
            case NETHERITE -> new Color(0x8E44AD);
            case NETHER_GOLD -> new Color(0xF39C12);
            case NETHER_QUARTZ -> new Color(0xECF0F1);
            default -> new Color(0xFFFFFF);
        };
    }

    private void renderOreBlock(MatrixStack matrices, BlockPos pos, Color color) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;

        matrices.push();
        matrices.translate(x, y, z);

        float alpha = transparency.getValueFloat();
        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;

        String mode = renderMode.getMode();

        if (mode.equals("Filled") || mode.equals("Both")) {
            drawFilledBox();
        }

        if (mode.equals("Outline") || mode.equals("Both")) {
            float outlineAlpha = Math.min(1.0f, alpha + 0.3f);
            drawOutlineBox(matrices, red, green, blue, outlineAlpha);
        }

        matrices.pop();
    }

    private void drawFilledBox() {

    }

    private void drawOutlineBox(MatrixStack matrices, float red, float green, float blue, float alpha) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        GL11.glEnable(GL13.GL_MULTISAMPLE);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        RenderSystem.lineWidth(outlineWidth.getValueFloat());

        buffer.vertex(matrix, 0, 0, 0).color(red, green, blue, alpha);
        buffer.vertex(matrix, 1, 0, 0).color(red, green, blue, alpha);

        buffer.vertex(matrix, 1, 0, 0).color(red, green, blue, alpha);
        buffer.vertex(matrix, 1, 0, 1).color(red, green, blue, alpha);

        buffer.vertex(matrix, 1, 0, 1).color(red, green, blue, alpha);
        buffer.vertex(matrix, 0, 0, 1).color(red, green, blue, alpha);

        buffer.vertex(matrix, 0, 0, 1).color(red, green, blue, alpha);
        buffer.vertex(matrix, 0, 0, 0).color(red, green, blue, alpha);


        buffer.vertex(matrix, 0, 1, 0).color(red, green, blue, alpha);
        buffer.vertex(matrix, 1, 1, 0).color(red, green, blue, alpha);

        buffer.vertex(matrix, 1, 1, 0).color(red, green, blue, alpha);
        buffer.vertex(matrix, 1, 1, 1).color(red, green, blue, alpha);

        buffer.vertex(matrix, 1, 1, 1).color(red, green, blue, alpha);
        buffer.vertex(matrix, 0, 1, 1).color(red, green, blue, alpha);

        buffer.vertex(matrix, 0, 1, 1).color(red, green, blue, alpha);
        buffer.vertex(matrix, 0, 1, 0).color(red, green, blue, alpha);


        buffer.vertex(matrix, 0, 0, 0).color(red, green, blue, alpha);
        buffer.vertex(matrix, 0, 1, 0).color(red, green, blue, alpha);

        buffer.vertex(matrix, 1, 0, 0).color(red, green, blue, alpha);
        buffer.vertex(matrix, 1, 1, 0).color(red, green, blue, alpha);

        buffer.vertex(matrix, 1, 0, 1).color(red, green, blue, alpha);
        buffer.vertex(matrix, 1, 1, 1).color(red, green, blue, alpha);

        buffer.vertex(matrix, 0, 0, 1).color(red, green, blue, alpha);
        buffer.vertex(matrix, 0, 1, 1).color(red, green, blue, alpha);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL13.GL_MULTISAMPLE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        scanTimer.reset();
        lastPlayerPos = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        detectedOres.clear();
        lastPlayerPos = null;
    }

    public enum OreType {
        NONE,
        DIAMOND,
        EMERALD,
        GOLD,
        IRON,
        COAL,
        COPPER,
        REDSTONE,
        LAPIS,
        NETHERITE,
        NETHER_GOLD,
        NETHER_QUARTZ
    }
}
