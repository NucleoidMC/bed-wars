package net.gegy1000.bedwars.game.bw.shop;

import net.gegy1000.bedwars.custom.CustomItems;
import net.gegy1000.bedwars.util.ColoredBlocks;
import net.gegy1000.bedwars.util.ItemUtil;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.ArmorLevel;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.game.bw.BwState;
import net.gegy1000.bedwars.shop.Cost;
import net.gegy1000.bedwars.shop.ShopUi;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.DyeColor;

public final class ItemShop {

    //TODO: reorganize shop layout
    public static ShopUi create(ServerPlayerEntity player) {
        return ShopUi.create(new LiteralText("Item Shop"), shop -> {
            BedWars bedWars = GameManager.activeFor(BedWars.TYPE);
            if (bedWars == null) return;

            BwState.Participant participant = bedWars.state.getParticipant(player);
            if (participant != null) {
                DyeColor color = participant.team.getDye();
                shop.addItem(new ItemStack(ColoredBlocks.wool(color), 16), Cost.ofIron(4));
                shop.addItem(new ItemStack(ColoredBlocks.terracotta(color), 16), Cost.ofIron(16));
                ItemStack glass = new ItemStack(ColoredBlocks.glass(color), 4);
                glass.setCustomName(new LiteralText("Shatterproof Glass"));
                shop.addItem(glass, Cost.ofIron(12));
            }

            shop.addItem(new ItemStack(Blocks.OAK_PLANKS, 16), Cost.ofGold(4));
            shop.addItem(new ItemStack(Blocks.END_STONE, 12), Cost.ofIron(24));
            shop.addItem(new ItemStack(Blocks.SAND, 4), Cost.ofGold(4));
            shop.addItem(new ItemStack(Blocks.OBSIDIAN, 4), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.COBWEB, 4), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.SCAFFOLDING, 8), Cost.ofGold(4));

            shop.addItem(bedWars.createTool(new ItemStack(Items.STONE_SWORD)), Cost.ofIron(20));
            shop.addItem(bedWars.createTool(new ItemStack(Items.IRON_SWORD)), Cost.ofGold(6));
            shop.addItem(bedWars.createTool(new ItemStack(Items.DIAMOND_SWORD)), Cost.ofEmeralds(3));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.BOW)), Cost.ofGold(20));
            shop.addItem(new ItemStack(Items.ARROW, 2), Cost.ofGold(1));

            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.SHEARS)), Cost.ofIron(40));
            shop.addItem(bedWars.createTool(new ItemStack(Items.WOODEN_AXE)), Cost.ofIron(10));
            shop.addItem(bedWars.createTool(new ItemStack(Items.WOODEN_PICKAXE)), Cost.ofIron(10));
            shop.addItem(bedWars.createTool(new ItemStack(Items.IRON_AXE)), Cost.ofGold(8));
            shop.addItem(bedWars.createTool(new ItemStack(Items.IRON_PICKAXE)), Cost.ofGold(8));
            shop.addItem(bedWars.createTool(new ItemStack(Items.DIAMOND_AXE)), Cost.ofGold(12));
            shop.addItem(bedWars.createTool(new ItemStack(Items.DIAMOND_PICKAXE)), Cost.ofGold(12));

            shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.FIRE_CHARGE), Cost.ofIron(50));
            shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
            shop.addItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24));
            shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));
            shop.addItem(CustomItems.BRIDGE_EGG.applyTo(new ItemStack(Items.EGG)), Cost.ofEmeralds(2));

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
}
