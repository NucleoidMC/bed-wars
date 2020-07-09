package net.gegy1000.bedwars;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ItemUtil {
    public static ItemStack unbreakable(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("Unbreakable", true);
        return stack;
    }

    public static ItemStack createFirework(int color, int flight, FireworkItem.Type type) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET, 1);

        ItemStack star = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag explosion = star.getOrCreateSubTag("Explosion");

        explosion.putIntArray("Colors", new int[] { color });
        explosion.putByte("Type", (byte) type.getId());

        CompoundTag fireworks = rocket.getOrCreateSubTag("Fireworks");

        ListTag explosions = new ListTag();
        explosions.add(explosion);
        fireworks.put("Explosions", explosions);

        fireworks.putByte("Flight", (byte) flight);

        return rocket;
    }

    public static ItemStack dye(ItemStack stack, int color) {
        Item item = stack.getItem();
        if (item instanceof DyeableItem) {
            ((DyeableItem) item).setColor(stack, color);
        }
        return stack;
    }

    public static int getEnchantLevel(ItemStack stack, Enchantment enchantment) {
        Identifier id = Registry.ENCHANTMENT.getId(enchantment);
        if (id == null) return 0;

        String idString = id.toString();

        ListTag enchantmentList = stack.getEnchantments();
        for (int i = 0; i < enchantmentList.size(); i++) {
            CompoundTag enchantmentTag = enchantmentList.getCompound(i);
            if (enchantmentTag.getString("id").equals(idString)) {
                return enchantmentTag.getShort("lvl");
            }
        }

        return 0;
    }

    public static void removeEnchant(ItemStack stack, Enchantment enchantment) {
        Identifier id = Registry.ENCHANTMENT.getId(enchantment);
        if (id == null) return;

        String idString = id.toString();
        ListTag enchantmentList = stack.getEnchantments();
        for (int i = 0; i < enchantmentList.size(); i++) {
            CompoundTag enchantmentTag = enchantmentList.getCompound(i);
            if (enchantmentTag.getString("id").equals(idString)) {
                enchantmentList.remove(i);
                return;
            }
        }
    }

    public static Block coloredWool(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_WOOL;
            case MAGENTA: return Blocks.MAGENTA_WOOL;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_WOOL;
            case YELLOW: return Blocks.YELLOW_WOOL;
            case LIME: return Blocks.LIME_WOOL;
            case PINK: return Blocks.PINK_WOOL;
            case GRAY: return Blocks.GRAY_WOOL;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_WOOL;
            case CYAN: return Blocks.CYAN_WOOL;
            case PURPLE: return Blocks.PURPLE_WOOL;
            case BLUE: return Blocks.BLUE_WOOL;
            case BROWN: return Blocks.BROWN_WOOL;
            case GREEN: return Blocks.GREEN_WOOL;
            case RED: return Blocks.RED_WOOL;
            case BLACK: return Blocks.BLACK_WOOL;
            default:
            case WHITE: return Blocks.WHITE_WOOL;
        }
    }

    public static Block coloredTerracotta(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_TERRACOTTA;
            case MAGENTA: return Blocks.MAGENTA_TERRACOTTA;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_TERRACOTTA;
            case YELLOW: return Blocks.YELLOW_TERRACOTTA;
            case LIME: return Blocks.LIME_TERRACOTTA;
            case PINK: return Blocks.PINK_TERRACOTTA;
            case GRAY: return Blocks.GRAY_TERRACOTTA;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_TERRACOTTA;
            case CYAN: return Blocks.CYAN_TERRACOTTA;
            case PURPLE: return Blocks.PURPLE_TERRACOTTA;
            case BLUE: return Blocks.BLUE_TERRACOTTA;
            case BROWN: return Blocks.BROWN_TERRACOTTA;
            case GREEN: return Blocks.GREEN_TERRACOTTA;
            case RED: return Blocks.RED_TERRACOTTA;
            case BLACK: return Blocks.BLACK_TERRACOTTA;
            case WHITE: return Blocks.WHITE_TERRACOTTA;
            default: return Blocks.TERRACOTTA;
        }
    }

    public static Block coloredGlass(DyeColor color) {
        switch (color) {
            case ORANGE: return Blocks.ORANGE_STAINED_GLASS;
            case MAGENTA: return Blocks.MAGENTA_STAINED_GLASS;
            case LIGHT_BLUE: return Blocks.LIGHT_BLUE_STAINED_GLASS;
            case YELLOW: return Blocks.YELLOW_STAINED_GLASS;
            case LIME: return Blocks.LIME_STAINED_GLASS;
            case PINK: return Blocks.PINK_STAINED_GLASS;
            case GRAY: return Blocks.GRAY_STAINED_GLASS;
            case LIGHT_GRAY: return Blocks.LIGHT_GRAY_STAINED_GLASS;
            case CYAN: return Blocks.CYAN_STAINED_GLASS;
            case PURPLE: return Blocks.PURPLE_STAINED_GLASS;
            case BLUE: return Blocks.BLUE_STAINED_GLASS;
            case BROWN: return Blocks.BROWN_STAINED_GLASS;
            case GREEN: return Blocks.GREEN_STAINED_GLASS;
            case RED: return Blocks.RED_STAINED_GLASS;
            case BLACK: return Blocks.BLACK_STAINED_GLASS;
            case WHITE: return Blocks.WHITE_STAINED_GLASS;
            default: return Blocks.GLASS;
        }
    }
}
