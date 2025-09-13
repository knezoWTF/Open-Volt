package com.volt.mixin;

import com.mojang.authlib.GameProfile;
import com.volt.Volt;
import com.volt.event.impl.player.PreMotionEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    protected abstract void sendSprintingPacket();

    @Shadow
    private boolean lastSneaking;

    @Shadow
    protected abstract boolean isCamera();

    @Shadow
    private double lastX;

    @Shadow
    private double lastBaseY;

    @Shadow
    private double lastZ;

    @Shadow
    private float lastYaw;

    @Shadow private float lastPitch;

    @Shadow
    private int ticksSinceLastPositionPacketSent;

    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Shadow
    private boolean lastOnGround;

    @Shadow
    private boolean autoJumpEnabled;
    @Shadow
    @Final
    protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void sendMovementPackets() {
        PreMotionEvent event = new PreMotionEvent(
                this.getX(),
                this.getBoundingBox().minY,
                this.getZ(),
                this.getYaw(),
                this.getPitch(),
                this.lastYaw,
                this.lastPitch,
                this.isOnGround(),
                this.isSprinting(),
                this.isSneaking()
        );


        this.sendSprintingPacket();
        this.sendSneakingPacket();

        if (this.isCamera()) {
            Volt.INSTANCE.getVoltEventBus().post(event);
            event.setPitchChanged(event.getPitch() != getPitch());

            double dx = event.getPosX() - this.lastX;
            double dy = event.getPosY() - this.lastBaseY;
            double dz = event.getPosZ() - this.lastZ;
            double dyaw = event.getYaw() - this.lastYaw;
            double dpitch = event.getPitch() - this.lastPitch;

            ++this.ticksSinceLastPositionPacketSent;

            boolean moved = MathHelper.squaredMagnitude(dx, dy, dz) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean rotated = dyaw != 0.0 || dpitch != 0.0;

            if (moved && rotated) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                        event.getPosX(), event.getPosY(), event.getPosZ(),
                        event.getYaw(), event.getPitch(),
                        event.isOnGround()
                ));
            } else if (moved) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        event.getPosX(), event.getPosY(), event.getPosZ(),
                        event.isOnGround()
                ));
            } else if (rotated) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        event.getYaw(), event.getPitch(),
                        event.isOnGround()
                ));
            } else if (this.lastOnGround != event.isOnGround()) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(
                        event.isOnGround()
                ));
            }

            if (moved) {
                this.lastX = event.getPosX();
                this.lastBaseY = event.getPosY();
                this.lastZ = event.getPosZ();
                this.ticksSinceLastPositionPacketSent = 0;
            }

            if (rotated) {
                this.lastYaw = event.getYaw();
                this.lastPitch = event.getPitch();
            }

            this.lastOnGround = event.isOnGround();
            this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
        }
    }

    private void sendSneakingPacket() {
        boolean sneaking = this.isSneaking();
        if (sneaking != this.lastSneaking) {
            ClientCommandC2SPacket.Mode mode = sneaking ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket((ClientPlayerEntity)(Object)this, mode));
            this.lastSneaking = sneaking;
        }
    }
}

