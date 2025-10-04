package com.volt.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.Volt;
import com.volt.event.impl.render.EventRender3D;
import com.volt.utils.font.util.RendererUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderChunkDebugInfo", at = @At("HEAD"))
    public void renderChunkDebugInfoInject(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Camera camera, CallbackInfo ci) {
        if (Volt.INSTANCE == null) return;

        RendererUtils.lastProjMat.set(RenderSystem.getProjectionMatrix());
        RendererUtils.lastModMat.set(RenderSystem.getModelViewMatrix());
        RendererUtils.lastWorldSpaceMatrix.identity();

        Volt.INSTANCE.getVoltEventBus().post(new EventRender3D(matrices));
    }
}
