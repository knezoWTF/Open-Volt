package com.volt.utils.mc;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public final class EnchantmentUtil {

    public static boolean hasEnchantment(ItemStack stack, World world, RegistryEntry<Enchantment> entry) {
        if (stack == null || world == null || entry == null) return false;
        ItemEnchantmentsComponent ench = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (ench == null) return false;
        return ench.getLevel(entry) > 0;
    }

    public static boolean hasEnchantment(ItemStack stack, World world, Enchantment enchantment) {
        if (stack == null || world == null || enchantment == null) return false;
        Registry<Enchantment> reg = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> entry = reg.getEntry(enchantment);
        return hasEnchantment(stack, world, entry);
    }

    public static boolean hasEnchantment(ItemStack stack, World world, RegistryKey<Enchantment> enchantmentKey) {
        if (stack == null || world == null || enchantmentKey == null) return false;
        Registry<Enchantment> reg = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        var opt = reg.getEntry(enchantmentKey);
        return opt.filter(enchantmentReference -> hasEnchantment(stack, world, enchantmentReference)).isPresent();
    }
}