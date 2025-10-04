package com.volt.mixin;

import com.volt.Volt;
import com.volt.module.Module;
import com.volt.utils.keybinding.KeyUtils;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle()) {
            if (this.client.currentScreen == null) {
                for (Module module : Volt.INSTANCE.moduleManager.getModules()) {
                    if (key == module.getKey() && action == GLFW.GLFW_PRESS) {
                        if (KeyUtils.isKeyPressed(key)) {
                            module.toggle();
                        }
                    }
                }
            }
        }
    }
}
