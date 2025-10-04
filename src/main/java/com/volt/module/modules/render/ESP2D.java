package com.volt.module.modules.render;

import com.volt.event.impl.render.EventRender2D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.font.util.RendererUtils;
import com.volt.utils.friend.FriendManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;

public class ESP2D extends Module {

    //TODO: Fix the esp having the holy grail of microstutters :Sob: (walper if ur reading this im sorry)

    private final NumberSetting lineWidth = new NumberSetting("Line Width", 1.0, 8.0, 2.0, 0.5);
    private final ColorSetting color = new ColorSetting("Color", new Color(255, 100, 100, 200));
    private final BooleanSetting showSelf = new BooleanSetting("Show Self", false);
    private final BooleanSetting teamCheck = new BooleanSetting("Team Check", false);
    private final NumberSetting range = new NumberSetting("Range", 10, 250, 120, 5);
    private final BooleanSetting healthBar = new BooleanSetting("Health Bar", true);
    private final NumberSetting healthBarWidth = new NumberSetting("Health Width", 1.0, 8.0, 2.0, 1.0);

    public ESP2D() {
        super("2D ESP", "Draws 2D boxes around players", -1, Category.RENDER);
        addSettings(lineWidth, color, showSelf, teamCheck, range, healthBar, healthBarWidth);
    }

    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext context = event.getContext();
        final int fbWidth = mc.getWindow().getFramebufferWidth();
        final int fbHeight = mc.getWindow().getFramebufferHeight();
        final int[] viewport = new int[]{0, 0, fbWidth, fbHeight};
        final int displayHeight = mc.getWindow().getHeight();
        final double scale = mc.getWindow().getScaleFactor();

        final Matrix4f combined = new Matrix4f(RendererUtils.lastProjMat).mul(new Matrix4f(RendererUtils.lastModMat));
        final Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        final float partialTicks = mc.getRenderTickCounter().getTickDelta(true);

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!shouldRender(player)) continue;

            Box box = getPlayerInterpolatedBox(player, partialTicks);
            int[] rect = projectBoxTo2D(box, viewport, displayHeight, scale, combined, cameraPos);
            if (rect == null) continue;

            Color drawColor = FriendManager.isFriend(player.getUuid())
                    ? new Color(128, 0, 128, color.getValue().getAlpha())
                    : color.getValue();

            int x1 = rect[0];
            int y1 = rect[1];
            int x2 = rect[2];
            int y2 = rect[3];

            drawRectOutline(context, x1, y1, x2, y2, drawColor.getRGB(), (int) Math.max(1, Math.round(lineWidth.getValue())));

            if (healthBar.getValue()) {
                drawHealthBar(context, player, x1, y1, y2);
            }
        }
    }

    private boolean shouldRender(PlayerEntity player) {
        if (player == mc.player && !showSelf.getValue()) return false;
        if (mc.player.distanceTo(player) > range.getValue()) return false;
        if (teamCheck.getValue()) {
            if (mc.player.getScoreboardTeam() != null && player.getScoreboardTeam() != null) {
                return !mc.player.getScoreboardTeam().equals(player.getScoreboardTeam());
            }
        }
        return true;
    }

    private Box getPlayerInterpolatedBox(PlayerEntity player, float partialTicks) {
        double ix = player.prevX + (player.getX() - player.prevX) * partialTicks;
        double iy = player.prevY + (player.getY() - player.prevY) * partialTicks;
        double iz = player.prevZ + (player.getZ() - player.prevZ) * partialTicks;

        double halfWidth = player.getWidth() / 2.0;
        double height = player.getHeight();

        return new Box(
                ix - halfWidth, iy, iz - halfWidth,
                ix + halfWidth, iy + height, iz + halfWidth
        );
    }

    private int[] projectBoxTo2D(Box box, int[] viewport, int displayHeight, double scaleFactor, Matrix4f combined, Vec3d cameraPos) {
        double[] xs = {box.minX, box.minX, box.minX, box.minX, box.maxX, box.maxX, box.maxX, box.maxX};
        double[] ys = {box.minY, box.minY, box.maxY, box.maxY, box.minY, box.minY, box.maxY, box.maxY};
        double[] zs = {box.minZ, box.maxZ, box.minZ, box.maxZ, box.minZ, box.maxZ, box.minZ, box.maxZ};

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        boolean anyVisible = false;

        Vector3f target = new Vector3f();
        Vector4f coords = new Vector4f();
        for (int i = 0; i < 8; i++) {
            if (projectWorldToScreen(xs[i], ys[i], zs[i], viewport, combined, cameraPos, target, coords)) {
                anyVisible = true;
                double sx = target.x / scaleFactor;
                double sy = (displayHeight - target.y) / scaleFactor;
                minX = Math.min(minX, sx);
                minY = Math.min(minY, sy);
                maxX = Math.max(maxX, sx);
                maxY = Math.max(maxY, sy);
            }
        }

        if (!anyVisible) return null;

        int x1 = (int) Math.floor(minX);
        int y1 = (int) Math.floor(minY);
        int x2 = (int) Math.ceil(maxX);
        int y2 = (int) Math.ceil(maxY);
        if (x2 - x1 < 2 || y2 - y1 < 2) return null;

        return new int[]{x1, y1, x2, y2};
    }

    private boolean projectWorldToScreen(double worldX, double worldY, double worldZ,
                                         int[] viewport,
                                         Matrix4f combined, Vec3d cameraPos, Vector3f outTarget, Vector4f reuseCoords) {
        double dx = worldX - cameraPos.x;
        double dy = worldY - cameraPos.y;
        double dz = worldZ - cameraPos.z;

        reuseCoords.set((float) dx, (float) dy, (float) dz, 1.0f).mul(RendererUtils.lastWorldSpaceMatrix);
        combined.project(reuseCoords.x(), reuseCoords.y(), reuseCoords.z(), viewport, outTarget);
        return outTarget.z > -1 && outTarget.z < 1;
    }

    private void drawRectOutline(DrawContext context, int x1, int y1, int x2, int y2, int color, int thickness) {
        int w = Math.max(1, thickness);
        context.fill(x1, y1, x2, y1 + w, color);
        context.fill(x1, y2 - w, x2, y2, color);
        context.fill(x1, y1, x1 + w, y2, color);
        context.fill(x2 - w, y1, x2, y2, color);
    }

    private void drawHealthBar(DrawContext context, PlayerEntity player, int x1, int y1, int y2) {
        int height = Math.max(1, y2 - y1);
        int barWidth = Math.max(1, (int) Math.round(healthBarWidth.getValue()));
        int gap = 2;

        int barX1 = x1 - gap - barWidth;
        int barX2 = x1 - gap;

        int bg = new Color(0, 0, 0, 120).getRGB();
        context.fill(barX1, y1, barX2, y2, bg);

        float maxHealth = Math.max(1.0f, player.getMaxHealth());
        float health = Math.max(0.0f, Math.min(player.getHealth(), maxHealth));
        float fraction = health / maxHealth;

        int filled = (int) Math.floor(height * fraction);
        if (filled <= 0) return;

        int fy1 = y2 - filled;

        float hue = Math.max(0.0f, Math.min(0.33f, fraction * 0.33f));
        Color base = Color.getHSBColor(hue, 0.95f, 0.95f);
        Color barColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), 200);

        context.fill(barX1, fy1, barX2, y2, barColor.getRGB());

        int edge = new Color(255, 255, 255, 60).getRGB();
        if (fy1 > y1) {
            context.fill(barX1, fy1, barX2, fy1 + 1, edge);
        }
    }
}


