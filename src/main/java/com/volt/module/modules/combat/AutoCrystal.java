package com.volt.module.modules.combat;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.InventoryUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * @author koi
 */
public final class AutoCrystal extends Module {

    private final KeybindSetting crystalKey = new KeybindSetting("Crystal Key", GLFW.GLFW_MOUSE_BUTTON_4, false);

    private final NumberSetting delay = new NumberSetting("Delay (MS)", 1, 200, 50, 1);

    private final BooleanSetting antiSuicide = new BooleanSetting("Anti Suicide", true);

    private final BooleanSetting autoSwitch = new BooleanSetting("Auto Switch", true);

    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true);

    private final BooleanSetting antiWeakness = new BooleanSetting("Anti Weakness", true);

    private final TimerUtil timer = new TimerUtil();

    private int originalSlot = -1;

    public AutoCrystal() {
        super("Auto Crystal", "Hold key to spam crystals", -1, Category.COMBAT);
        this.addSettings(crystalKey, delay, antiSuicide, autoSwitch, switchBack, antiWeakness);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(crystalKey));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;

        if (!KeyUtils.isKeyPressed(crystalKey.getKeyCode())) return;

        if (timer.hasElapsedTime(delay.getValueInt())) {
            processCrystal();
            timer.reset();
        }
    }

    private void processCrystal() {
        if (antiSuicide.getValue() && !mc.player.isOnGround()) return;
        if (mc.crosshairTarget instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof EndCrystalEntity crystal) {
                if (!crystal.isRemoved() && crystal.isAlive() && mc.world.getEntityById(crystal.getId()) != null) {
                    
                    if (mc.player.getPos().distanceTo(crystal.getPos()) <= 4.5) {
                        if (antiWeakness.getValue() && mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.WEAKNESS)) {
                            InventoryUtil.swapToWeapon(SwordItem.class);
                        }
                        ((MinecraftClientAccessor) mc).invokeDoAttack();
                    }
                }
                return;
            }
        }
        
        if (mc.crosshairTarget instanceof BlockHitResult blockHit) {
            BlockPos targetBlock = blockHit.getBlockPos();
            BlockPos placementPos = targetBlock.offset(blockHit.getSide());

            if (isObsidianOrBedrock(targetBlock) && isValidCrystalPosition(placementPos)) {
                if (autoSwitch.getValue() && hasItemInHotbar()) {
                    InventoryUtil.swapToSlot(Items.END_CRYSTAL);
                }

                if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
                    ((MinecraftClientAccessor) mc).invokeDoItemUse();
                }
            }
        }
    }

    private boolean hasItemInHotbar() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL) return true;
        }
        return false;
    }

    private boolean isObsidianOrBedrock(BlockPos pos) {
        if (mc.world == null) return false;
        var block = mc.world.getBlockState(pos).getBlock();
        return block == net.minecraft.block.Blocks.OBSIDIAN || block == net.minecraft.block.Blocks.BEDROCK;
    }

    private boolean isValidCrystalPosition(BlockPos pos) {
        if (mc.world == null) return false;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 4.5) return false;

        if (!mc.world.getBlockState(pos).isAir()) return false;
        if (!mc.world.getBlockState(pos.up()).isAir()) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        return !pos.equals(playerPos) && !pos.equals(playerPos.up());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (autoSwitch.getValue()) originalSlot = mc.player.getInventory().selectedSlot;
        timer.reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (switchBack.getValue() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
        originalSlot = -1;
    }

    @Override
    public int getKey() {
        return -1;
    }
}
