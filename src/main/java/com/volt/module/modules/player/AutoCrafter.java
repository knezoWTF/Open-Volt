package com.volt.module.modules.player;


import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.List;

public final class AutoCrafter extends Module {

    private static final List<Recipe> RECIPES = Arrays.asList(
            new Recipe(Items.DIAMOND_SWORD, Items.DIAMOND, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.DIAMOND_PICKAXE, Items.DIAMOND, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.DIAMOND_AXE, Items.DIAMOND, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.DIAMOND_SHOVEL, Items.DIAMOND, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.IRON_SWORD, Items.IRON_INGOT, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.IRON_PICKAXE, Items.IRON_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.IRON_AXE, Items.IRON_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.IRON_SHOVEL, Items.IRON_INGOT, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.GOLDEN_SWORD, Items.GOLD_INGOT, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.GOLDEN_PICKAXE, Items.GOLD_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.GOLDEN_AXE, Items.GOLD_INGOT, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.GOLDEN_SHOVEL, Items.GOLD_INGOT, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.WOODEN_SWORD, Items.OAK_PLANKS, 2, Items.STICK, 1,
                    new int[]{-1, 1, -1, -1, 1, -1, -1, 2, -1}),
            new Recipe(Items.WOODEN_PICKAXE, Items.OAK_PLANKS, 3, Items.STICK, 2,
                    new int[]{1, 1, 1, -1, 2, -1, -1, 2, -1}),
            new Recipe(Items.WOODEN_AXE, Items.OAK_PLANKS, 3, Items.STICK, 2,
                    new int[]{1, 1, -1, 1, 2, -1, -1, 2, -1}),
            new Recipe(Items.WOODEN_SHOVEL, Items.OAK_PLANKS, 1, Items.STICK, 2,
                    new int[]{-1, 1, -1, -1, 2, -1, -1, 2, -1}),

            new Recipe(Items.DIAMOND_HELMET, Items.DIAMOND, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.DIAMOND_CHESTPLATE, Items.DIAMOND, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.DIAMOND_LEGGINGS, Items.DIAMOND, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.DIAMOND_BOOTS, Items.DIAMOND, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.IRON_HELMET, Items.IRON_INGOT, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.IRON_CHESTPLATE, Items.IRON_INGOT, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.IRON_LEGGINGS, Items.IRON_INGOT, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.IRON_BOOTS, Items.IRON_INGOT, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.GOLDEN_HELMET, Items.GOLD_INGOT, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.GOLDEN_CHESTPLATE, Items.GOLD_INGOT, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.GOLDEN_LEGGINGS, Items.GOLD_INGOT, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.GOLDEN_BOOTS, Items.GOLD_INGOT, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.LEATHER_HELMET, Items.LEATHER, 5,
                    new int[]{1, 1, 1, 1, -1, 1, -1, -1, -1}),
            new Recipe(Items.LEATHER_CHESTPLATE, Items.LEATHER, 8,
                    new int[]{1, -1, 1, 1, 1, 1, 1, 1, 1}),
            new Recipe(Items.LEATHER_LEGGINGS, Items.LEATHER, 7,
                    new int[]{1, 1, 1, 1, -1, 1, 1, -1, 1}),
            new Recipe(Items.LEATHER_BOOTS, Items.LEATHER, 4,
                    new int[]{-1, -1, -1, 1, -1, 1, 1, -1, 1}),

            new Recipe(Items.GOLDEN_APPLE, Items.GOLD_INGOT, 8, Items.APPLE, 1,
                    new int[]{1, 1, 1, 1, 2, 1, 1, 1, 1})
    );
    private final BooleanSetting craftSwords = new BooleanSetting("Swords", true);
    private final BooleanSetting craftPickaxes = new BooleanSetting("Pickaxes", true);
    private final BooleanSetting craftAxes = new BooleanSetting("Axes", true);
    private final BooleanSetting craftShovels = new BooleanSetting("Shovels", true);
    private final BooleanSetting craftHelmets = new BooleanSetting("Helmets", true);
    private final BooleanSetting craftChestplates = new BooleanSetting("Chestplates", true);
    private final BooleanSetting craftLeggings = new BooleanSetting("Leggings", true);
    private final BooleanSetting craftBoots = new BooleanSetting("Boots", true);
    private final BooleanSetting craftApples = new BooleanSetting("Golden Apples", true);
    private final NumberSetting craftDelay = new NumberSetting("Craft Delay", 0, 500, 100, 10);
    private final TimerUtil craftTimer = new TimerUtil();
    private boolean isCrafting = false;

    public AutoCrafter() {
        super("Auto Crafter", "Automatically crafts items when crafting table is open", -1, Category.PLAYER);
        this.addSettings(craftSwords, craftPickaxes, craftAxes, craftShovels,
                craftHelmets, craftChestplates, craftLeggings, craftBoots,
                craftApples, craftDelay);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {

        if (!isValidState()) return;
        if (!craftTimer.hasElapsedTime(craftDelay.getValueInt())) return;

        if (isCrafting) {
            collectResult();
        } else {
            if (hasAnythingEnabled()) {
                startCrafting();
            }
        }
    }

    private boolean isValidState() {
        return mc.player != null && mc.world != null && mc.currentScreen instanceof CraftingScreen;
    }

    private boolean hasAnythingEnabled() {
        return craftSwords.getValue() || craftPickaxes.getValue() || craftAxes.getValue() ||
                craftShovels.getValue() || craftHelmets.getValue() || craftChestplates.getValue() ||
                craftLeggings.getValue() || craftBoots.getValue() || craftApples.getValue();
    }

    private void startCrafting() {
        Recipe selectedRecipe = findCraftableRecipe();
        if (selectedRecipe != null) {
            craftRecipe(selectedRecipe);
        }
    }

    private Recipe findCraftableRecipe() {
        if (craftApples.getValue()) {
            Recipe appleRecipe = getRecipeByResult();
            if (appleRecipe != null && canCraftRecipe(appleRecipe)) {
                return appleRecipe;
            }
        }

        for (Recipe recipe : RECIPES) {
            if (recipe.result == Items.GOLDEN_APPLE) continue;

            if (isRecipeEnabled(recipe) && canCraftRecipe(recipe) && shouldCraftItem(recipe.result)) {
                if (craftApples.getValue() && usesGold(recipe)) continue;

                return recipe;
            }
        }

        return null;
    }

    private boolean isRecipeEnabled(Recipe recipe) {
        Item result = recipe.result;

        if (isSword(result)) return craftSwords.getValue();
        if (isPickaxe(result)) return craftPickaxes.getValue();
        if (isAxe(result)) return craftAxes.getValue();
        if (isShovel(result)) return craftShovels.getValue();

        if (isHelmet(result)) return craftHelmets.getValue();
        if (isChestplate(result)) return craftChestplates.getValue();
        if (isLeggings(result)) return craftLeggings.getValue();
        if (isBoots(result)) return craftBoots.getValue();

        return false;
    }

    private boolean canCraftRecipe(Recipe recipe) {
        if (getItemCount(recipe.material1) < recipe.count1) return false;
        return recipe.material2 == null || getItemCount(recipe.material2) >= recipe.count2;
    }

    private boolean shouldCraftItem(Item item) {
        if (item == Items.GOLDEN_APPLE) return true;

        return !hasItemInInventory(item);
    }

    private void craftRecipe(Recipe recipe) {
        clearCraftingGrid();
        placeRecipeItems(recipe);
        isCrafting = true;
        craftTimer.reset();
    }

    private void placeRecipeItems(Recipe recipe) {
        for (int i = 0; i < recipe.pattern.length; i++) {
            int materialType = recipe.pattern[i];
            if (materialType == -1) continue;

            Item material = (materialType == 1) ? recipe.material1 : recipe.material2;
            if (material != null) {
                placeItemInSlot(material, i + 1);
            }
        }
    }

    private void placeItemInSlot(Item item, int craftingSlot) {
        int sourceSlot = findLargestStack(item);
        if (sourceSlot != -1) {
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    sourceSlot,
                    1,
                    SlotActionType.PICKUP,
                    mc.player
            );
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    craftingSlot,
                    1,
                    SlotActionType.PICKUP,
                    mc.player
            );
        }
    }

    private void clearCraftingGrid() {
        for (int i = 1; i <= 9; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i,
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                );
            }
        }
    }

    private void collectResult() {
        ItemStack result = mc.player.currentScreenHandler.getSlot(0).getStack();
        if (!result.isEmpty()) {
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    0,
                    0,
                    SlotActionType.QUICK_MOVE,
                    mc.player
            );
        }

        isCrafting = false;
        craftTimer.reset();
    }


    private int findLargestStack(Item item) {
        int bestSlot = -1;
        int largestStack = 0;

        for (int i = 1; i < mc.player.currentScreenHandler.slots.size(); i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() > largestStack) {
                largestStack = stack.getCount();
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private int getItemCount(Item item) {
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean hasItemInInventory(Item item) {
        return getItemCount(item) > 0;
    }

    private Recipe getRecipeByResult() {
        return RECIPES.stream().filter(r -> r.result == Items.GOLDEN_APPLE).findFirst().orElse(null);
    }

    private boolean usesGold(Recipe recipe) {
        return recipe.material1 == Items.GOLD_INGOT || recipe.material2 == Items.GOLD_INGOT;
    }

    private boolean isHelmet(Item item) {
        return item == Items.DIAMOND_HELMET || item == Items.IRON_HELMET ||
                item == Items.GOLDEN_HELMET || item == Items.LEATHER_HELMET;
    }

    private boolean isChestplate(Item item) {
        return item == Items.DIAMOND_CHESTPLATE || item == Items.IRON_CHESTPLATE ||
                item == Items.GOLDEN_CHESTPLATE || item == Items.LEATHER_CHESTPLATE;
    }

    private boolean isLeggings(Item item) {
        return item == Items.DIAMOND_LEGGINGS || item == Items.IRON_LEGGINGS ||
                item == Items.GOLDEN_LEGGINGS || item == Items.LEATHER_LEGGINGS;
    }

    private boolean isBoots(Item item) {
        return item == Items.DIAMOND_BOOTS || item == Items.IRON_BOOTS ||
                item == Items.GOLDEN_BOOTS || item == Items.LEATHER_BOOTS;
    }

    private boolean isSword(Item item) {
        return item == Items.DIAMOND_SWORD || item == Items.IRON_SWORD ||
                item == Items.GOLDEN_SWORD || item == Items.WOODEN_SWORD;
    }

    private boolean isPickaxe(Item item) {
        return item == Items.DIAMOND_PICKAXE || item == Items.IRON_PICKAXE ||
                item == Items.GOLDEN_PICKAXE || item == Items.WOODEN_PICKAXE;
    }

    private boolean isAxe(Item item) {
        return item == Items.DIAMOND_AXE || item == Items.IRON_AXE ||
                item == Items.GOLDEN_AXE || item == Items.WOODEN_AXE;
    }

    private boolean isShovel(Item item) {
        return item == Items.DIAMOND_SHOVEL || item == Items.IRON_SHOVEL ||
                item == Items.GOLDEN_SHOVEL || item == Items.WOODEN_SHOVEL;
    }

    @Override
    public void onEnable() {
        resetState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        resetState();
        super.onDisable();
    }

    private void resetState() {
        isCrafting = false;
        craftTimer.reset();
    }

    private record Recipe(Item result, Item material1, int count1, Item material2, int count2, int[] pattern) {
            Recipe(Item result, Item material1, int count1, int[] pattern) {
                this(result, material1, count1, null, 0, pattern);
            }

    }
} 