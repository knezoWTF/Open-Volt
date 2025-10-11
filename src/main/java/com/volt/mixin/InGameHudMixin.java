package com.volt.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.Volt;
import com.volt.event.impl.render.EventRender2D;
import com.volt.utils.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        RenderUtils.unscaledProjection();
        RenderUtils.scaledProjection();
        RenderSystem.applyModelViewMatrix();

        Volt.INSTANCE.getVoltEventBus().post(new EventRender2D(context, context.getScaledWindowWidth(), context.getScaledWindowHeight()));
    }
}
