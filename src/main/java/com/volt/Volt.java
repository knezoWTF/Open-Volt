package com.volt;

import com.volt.command.CommandManager;
import com.volt.module.ModuleManager;
import com.volt.module.events.MouseModuleHandler;
import com.volt.profiles.ProfileManager;
import com.volt.utils.notification.NotificationManager;
import com.volt.utils.render.font.FontManager;
import io.github.racoondog.norbit.EventBus;
import lombok.Getter;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Getter
public final class Volt implements ClientModInitializer {
    public static final String CLIENT_VERSION = "v2.2";
    public static final boolean shouldUseMouseEvent = System.getProperty("os.name").toLowerCase().contains("windows");
    public static Volt INSTANCE;
    public static MinecraftClient mc;
    public final IEventBus VoltEventBus;
    public final ModuleManager moduleManager;
    public final FontManager fontManager;
    public final ProfileManager profileManager;
    public final CommandManager commandManager;
    public final MouseModuleHandler mouseModuleHandler;
    public final NotificationManager notificationManager;
    private final Logger logger = LoggerFactory.getLogger("Volt");

    public Volt() {
        INSTANCE = this;
        mc = MinecraftClient.getInstance();
        VoltEventBus = EventBus.threadSafe();
        VoltEventBus.registerLambdaFactory("com.volt", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        this.moduleManager = new ModuleManager();
        this.fontManager = new FontManager();
        this.profileManager = new ProfileManager();
        this.commandManager = new CommandManager();
        this.commandManager.init();
        this.mouseModuleHandler = new MouseModuleHandler();
        this.notificationManager = NotificationManager.getInstance();

        VoltEventBus.subscribe(mouseModuleHandler);
        VoltEventBus.subscribe(notificationManager);
    }

    @Override
    public void onInitializeClient() {
        // Double initialization prevention, it's already initializing in the constructor
    }
}