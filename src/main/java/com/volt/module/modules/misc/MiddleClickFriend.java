package com.volt.module.modules.misc;

import com.volt.event.impl.input.MouseClickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.utils.friend.FriendManager;
import com.volt.utils.mc.ChatUtils;
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
                        ChatUtils.addChatMessage("<green>" + player.getName().getString() + " added to friends</green>");
                    } else {
                        ChatUtils.addChatMessage("<red>" + player.getName().getString() + " removed from friends</red>");
                    }
                }
            }
        }
    }
}
