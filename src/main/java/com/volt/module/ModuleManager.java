package com.volt.module;

import com.volt.module.modules.client.ClickGUIModule;
import com.volt.module.modules.client.Client;
import com.volt.module.modules.client.Debugger;
import com.volt.module.modules.client.Panic;
import com.volt.module.modules.combat.*;
import com.volt.module.modules.misc.*;
import com.volt.module.modules.movement.AutoFirework;
import com.volt.module.modules.movement.AutoHeadHitter;
import com.volt.module.modules.movement.Sprint;
import com.volt.module.modules.player.*;
import com.volt.module.modules.render.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public final class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        addModules();
    }

    public List<Module> getEnabledModules() {
        return modules.stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Module> getModulesInCategory(Category category) {
        return modules.stream()
                .filter(module -> module.getModuleCategory() == category)
                .collect(Collectors.toUnmodifiableList());
    }

    public <T extends Module> Optional<T> getModule(Class<T> moduleClass) {
        return modules.stream()
                .filter(module -> module.getClass().equals(moduleClass))
                .map(moduleClass::cast)
                .findFirst();
    }

    private void addModules() {
        // Combat
        add(
                new AutoMace(), new TotemHit(), new TriggerBot(), new Velocity(),
                new ShieldBreaker(), new ThrowPot(), new ElytraHotSwap(),
                new AntiMiss(), new WTap(), new STap(),     
                new AimAssist(), new SwordHotSwap(), new AutoCrystal(), new SwordSwap()
        );
        // Movement
        add(new Sprint(), new AutoFirework(), new AutoHeadHitter());

        // Player
        add(
                new AutoExtinguish(), new AutoTool(), new AutoWeb(), new AutoRefill(),
                new AutoDrain(), new AutoCrafter(), new FastPlace(), new FastEXP(),
                new Eagle(), new TrapSave(), new PingSpoof(), new AutoDoubleHand(),
                new FastMine()
        );

        // Render
        add(
                new ContainerSlots(), new FullBright(), new HUD(), new PlayerESP(),
                new SwingSpeed(), new OreESP(), new Trajectory(), new FpsCounter(),
                new SkeletonESP(), new EntityESP()
        );

        // Misc
        add(
                new CartKey(), new HoverTotem(), new MiddleClickFriend(),
                new PearlKey(), new WindChargeKey(), new Teams(), new FakePlayer()
        );

        // Client
        add(new ClickGUIModule(), new Client(), new Panic(), new Debugger());
    }

    private void add(Module... mods) {
        modules.addAll(Arrays.asList(mods));
    }
}
