package com.volt.module.modules.misc;

import com.volt.Volt;
import com.volt.module.Category;
import com.volt.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Objects;

public class Teams extends Module {

    public Teams() {
        super("Teams", "Stops you from targeting teammates", Category.MISC);
    }

    public static boolean isTeammate(Entity entity) {
        Teams teamsModule = Volt.INSTANCE.getModuleManager().getModule(Teams.class).get();
        if (teamsModule == null || !teamsModule.isEnabled()) {
            return false;
        }

        if (Objects.isNull(entity) || Objects.isNull(entity.getName())) {
            return true;
        }

        if (!(entity instanceof LivingEntity)) {
            return true;
        }

        return MinecraftClient.getInstance().player.isTeammate(entity);
    }
}