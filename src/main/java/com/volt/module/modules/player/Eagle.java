package com.volt.module.modules.player;


import com.volt.event.impl.render.EventRender2D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class Eagle extends Module {
    public static BooleanSetting onlyWorkWithShift = new BooleanSetting("Only work while holding shift", false);

    public Eagle() {
        super("Eagle", "Helps you fastbridge", -1, Category.PLAYER);
        this.addSetting(onlyWorkWithShift);
    }

    @EventHandler
    private void onEventRender2D(EventRender2D event) {
        if (isNull() || !mc.player.isOnGround()) {
            mc.options.sneakKey.setPressed(false);
            return;
        }
        if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_PRESS && onlyWorkWithShift.getValue()) {
            return;
        }

        double x = mc.player.getX();
        double y = mc.player.getY() - 0.1;
        double z = mc.player.getZ();

        boolean onEdge =
                isAir(x + 0.1, y, z + 0.1) ||
                        isAir(x - 0.1, y, z + 0.1) ||
                        isAir(x + 0.1, y, z - 0.1) ||
                        isAir(x - 0.1, y, z - 0.1);

        mc.options.sneakKey.setPressed(onEdge);
    }

    private boolean isAir(double x, double y, double z) {
        BlockPos pos = new BlockPos((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        assert mc.world != null;
        return mc.world.getBlockState(pos).getBlock() == Blocks.AIR;
    }

    @Override
    public void onDisable() {
        mc.options.sneakKey.setPressed(false);
    }
}
