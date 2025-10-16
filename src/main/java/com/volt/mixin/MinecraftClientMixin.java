package com.volt.mixin;

import com.volt.IMinecraft;
import com.volt.Volt;
import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.world.WorldChangeEvent;
import com.volt.gui.ClickGui;
import com.volt.module.modules.client.ClickGUIModule;
import com.volt.module.modules.client.Client;
import com.volt.profiles.ProfileManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements IMinecraft {

    @Shadow
    public ClientWorld world;
    @Shadow
    public HitResult crosshairTarget;
    @Shadow
    public ClientPlayerEntity player;
    @Shadow
    public ClientPlayerInteractionManager interactionManager;
    @Shadow
    @Final
    private RenderTickCounter.Dynamic renderTickCounter;

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    public void setTitle(CallbackInfoReturnable<String> cir) {
        if (Volt.INSTANCE == null || Volt.mc == null) return;

        var optionalClientModule = Volt.INSTANCE.getModuleManager().getModule(Client.class);
        if (optionalClientModule.isPresent()) {
            Client client = optionalClientModule.get();
            if (client.isEnabled() && client.getTitle()) {
                cir.setReturnValue("Volt 1.21.1");
            }
        }
    }

    @Inject(method = "run", at = @At("HEAD"))
    public void runInject(CallbackInfo ci) {
        if (Volt.INSTANCE != null) {
            ProfileManager profileManager = Volt.INSTANCE.getProfileManager();
            profileManager.loadProfile("default");
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (Volt.INSTANCE == null || Volt.mc == null) return;

        if (world != null) {
            Volt.INSTANCE.getVoltEventBus().post(new TickEvent());
        }

        var optionalClickGuiModule = Volt.INSTANCE.getModuleManager().getModule(ClickGUIModule.class);
        if (optionalClickGuiModule.isPresent()) {
            ClickGUIModule clickGuiModule = optionalClickGuiModule.get();
            if (clickGuiModule.isEnabled() && !(Volt.mc.currentScreen instanceof ClickGui) && world != null) {
                Volt.mc.setScreen(new ClickGui());
            }
        }
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void stopInject(CallbackInfo ci) {
        if (Volt.INSTANCE != null) {
            ProfileManager profileManager = Volt.INSTANCE.getProfileManager();
            profileManager.saveProfile("default", true);
        }
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    public void onWorldChangeInject(ClientWorld newWorld, CallbackInfo ci) {
        if (Volt.INSTANCE != null && Volt.mc != null) {
            Volt.INSTANCE.getVoltEventBus().post(new WorldChangeEvent(newWorld));
        }
    }
}
