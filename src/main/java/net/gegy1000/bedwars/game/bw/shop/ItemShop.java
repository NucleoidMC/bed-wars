package net.gegy1000.bedwars.game.bw.shop;

import net.gegy1000.bedwars.ItemUtil;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.ArmorLevel;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.game.bw.BwState;
import net.gegy1000.bedwars.shop.Cost;
import net.gegy1000.bedwars.shop.ShopUi;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.DyeColor;

public final class ItemShop {
    public static ShopUi create(ServerPlayerEntity player) {
        return ShopUi.create(new LiteralText("Item Shop"), shop -> {
            BedWars bedWars = GameManager.activeFor(BedWars.GAME);
            if (bedWars == null) return;

            BwState.Participant participant = bedWars.state.getParticipant(player);
            if (participant != null) {
                DyeColor color = participant.team.getColor();
                shop.addItem(new ItemStack(wool(color), 16), Cost.ofIron(4));
                shop.addItem(new ItemStack(terracotta(color), 16), Cost.ofIron(16));
                ItemStack glass = new ItemStack(glass(color), 4);
                glass.setCustomName(new LiteralText("Blast Proof Glass"));
                shop.addItem(glass, Cost.ofIron(12));
            }

            shop.addItem(new ItemStack(Blocks.OAK_PLANKS, 16), Cost.ofGold(4));
            shop.addItem(new ItemStack(Blocks.END_STONE, 12), Cost.ofIron(24));
            shop.addItem(new ItemStack(Blocks.SAND, 4), Cost.ofGold(4));
            shop.addItem(new ItemStack(Blocks.OBSIDIAN, 4), Cost.ofEmeralds(4));

            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.STONE_SWORD)), Cost.ofIron(20));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.IRON_SWORD)), Cost.ofGold(6));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.DIAMOND_SWORD)), Cost.ofEmeralds(3));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.BOW)), Cost.ofGold(20));
            shop.addItem(new ItemStack(Items.ARROW, 2), Cost.ofGold(1));

            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.SHEARS)), Cost.ofIron(40));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.WOODEN_AXE)), Cost.ofIron(10));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.WOODEN_PICKAXE)), Cost.ofIron(10));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.IRON_HOE)), Cost.ofGold(4));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.IRON_AXE)), Cost.ofGold(8));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.IRON_PICKAXE)), Cost.ofGold(8));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.DIAMOND_HOE)), Cost.ofGold(8));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.DIAMOND_AXE)), Cost.ofGold(12));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.DIAMOND_PICKAXE)), Cost.ofGold(12));

            shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.FIRE_CHARGE), Cost.ofIron(50));
            shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
            shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));

            if (participant != null) {
                ArmorLevel armorLevel = participant.armorLevel;

                Cost ironArmorCost = armorLevel.isUpgradeTo(ArmorLevel.IRON) ? Cost.ofGold(12) : Cost.no();
                shop.add(Items.IRON_CHESTPLATE, ironArmorCost, new LiteralText("Upgrade to Iron Armor"), () -> {
                    bedWars.playerLogic.upgradeArmorTo(participant, ArmorLevel.IRON);
                });

                Cost diamondArmorCost = armorLevel.isUpgradeTo(ArmorLevel.DIAMOND) ? Cost.ofEmeralds(6) : Cost.no();
                shop.add(Items.DIAMOND_CHESTPLATE, diamondArmorCost, new LiteralText("Upgrade to Diamond Armor"), () -> {
                    bedWars.playerLogic.upgradeArmorTo(participant, ArmorLevel.DIAMOND);
                });
            }
        });
    }

    private static Block wool(DyeColor color) {
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

    private static Block terracotta(DyeColor color) {
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
            default:
            case WHITE: return Blocks.WHITE_TERRACOTTA;
        }
    }

    private static Block glass(DyeColor color) {
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
            default:
            case WHITE: return Blocks.WHITE_STAINED_GLASS;
        }
    }
}
