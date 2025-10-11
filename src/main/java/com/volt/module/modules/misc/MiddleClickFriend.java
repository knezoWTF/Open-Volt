package com.volt.module.modules.misc;

import com.volt.event.impl.input.MouseClickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.utils.friend.FriendManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public final class MiddleClickFriend extends Module {

    public MiddleClickFriend() {
        super("Middle Click Friend", "Middle click on players to add/remove them from friends list", -1, Category.MISC);
    }

    @EventHandler
    private void onMouseClick(MouseClickEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && event.action() == GLFW.GLFW_PRESS) {
            if (isNull()) return;

            HitResult hitResult = mc.crosshairTarget;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                if (entityHitResult.getEntity() instanceof PlayerEntity player) {
                    if (player == mc.player) return;

                    FriendManager.toggleFriend(player.getUuid());

                    if (FriendManager.isFriend(player.getUuid())) {
                        mc.player.sendMessage(net.minecraft.text.Text.literal("§a" + player.getName().getString() + " added to friends"), false);
                    } else {
                        mc.player.sendMessage(net.minecraft.text.Text.literal("§c" + player.getName().getString() + " removed from friends"), false);
                    }
                }
            }
        }
    }
}