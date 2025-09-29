package com.volt.render.feature;

import com.volt.Volt;
import com.volt.module.modules.render.Cape;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.util.Optional;

public final class CapeRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public CapeRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (Volt.INSTANCE == null) return;
        Optional<Cape> optional = Volt.INSTANCE.getModuleManager().getModule(Cape.class);
        if (optional.isEmpty()) return;
        Cape module = optional.get();
        if (!module.isEnabled()) return;
        if (!module.shouldRender(player)) return;
        if (player.isInvisible()) return;
        matrices.push();
        double dx = MathHelper.lerp(tickDelta, player.prevCapeX, player.capeX) - MathHelper.lerp(tickDelta, player.prevX, player.getX());
        double dy = MathHelper.lerp(tickDelta, player.prevCapeY, player.capeY) - MathHelper.lerp(tickDelta, player.prevY, player.getY());
        double dz = MathHelper.lerp(tickDelta, player.prevCapeZ, player.capeZ) - MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
        float bodyYaw = player.prevBodyYaw + (player.bodyYaw - player.prevBodyYaw) * tickDelta;
        float yawSin = MathHelper.sin(bodyYaw * 0.017453292f);
        float yawCos = -MathHelper.cos(bodyYaw * 0.017453292f);
        float heightOffset = MathHelper.clamp((float) dy * 10.0f, -6.0f, 32.0f);
        float forwardSwing = MathHelper.clamp((float) (dx * yawSin + dz * yawCos) * 100.0f, 0.0f, 150.0f);
        float sidewaysSwing = MathHelper.clamp((float) (dx * yawCos - dz * yawSin) * 100.0f, -20.0f, 20.0f);
        if (forwardSwing < 0.0f) forwardSwing = 0.0f;
        float stride = MathHelper.lerp(tickDelta, player.prevStrideDistance, player.strideDistance);
        heightOffset += MathHelper.sin(stride * 6.0f) * 32.0f * stride;
        if (player.isInSneakingPose()) heightOffset += 25.0f;
        matrices.translate(0.0f, 0.0f, 0.125f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0f + forwardSwing / 2.0f + heightOffset));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sidewaysSwing / 2.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - sidewaysSwing / 2.0f));
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(module.getSelectedTexture()));
        PlayerEntityModel<AbstractClientPlayerEntity> model = getContextModel();
        model.renderCape(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
