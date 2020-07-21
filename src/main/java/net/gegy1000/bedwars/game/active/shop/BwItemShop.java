package net.gegy1000.bedwars.game.active.shop;

import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.bedwars.game.active.BwParticipant;
import net.gegy1000.bedwars.game.active.upgrade.PlayerUpgrades;
import net.gegy1000.bedwars.game.active.upgrade.Upgrade;
import net.gegy1000.bedwars.game.active.upgrade.UpgradeType;
import net.gegy1000.gl.shop.Cost;
import net.gegy1000.gl.shop.ShopBuilder;
import net.gegy1000.gl.shop.ShopUi;
import net.gegy1000.gl.util.ColoredBlocks;
import net.gegy1000.gl.util.ItemUtil;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public final class BwItemShop {
    //TODO: reorganize shop layout
    public static ShopUi create(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Item Shop"), shop -> {
            BwParticipant participant = game.getParticipant(player);
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

            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.SHIELD)), Cost.ofGold(10));
            shop.addItem(ItemUtil.unbreakable(new ItemStack(Items.BOW)), Cost.ofGold(20));
            shop.addItem(new ItemStack(Items.ARROW, 2), Cost.ofGold(1));

            ItemStack trident = ItemUtil.unbreakable(new ItemStack(Items.TRIDENT));
            trident.addEnchantment(Enchantments.LOYALTY, 1);
            shop.addItem(trident, Cost.ofEmeralds(6));

            shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.FIRE_CHARGE), Cost.ofIron(50));
            shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
            shop.addItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24));
            shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));
            shop.addItem(BwCustomItems.BW_CHORUS_FRUIT.applyTo(new ItemStack(Items.CHORUS_FRUIT, 4)), Cost.ofGold(8));
            shop.addItem(BwCustomItems.BRIDGE_EGG.applyTo(new ItemStack(Items.EGG)), Cost.ofEmeralds(2));

            if (participant != null) {
                PlayerUpgrades upgrades = participant.upgrades;

                addUpgrade(shop, upgrades, UpgradeType.SWORD, new LiteralText("Upgrade Sword"));
                addUpgrade(shop, upgrades, UpgradeType.PICKAXE, new LiteralText("Upgrade Pickaxe"));
                addUpgrade(shop, upgrades, UpgradeType.AXE, new LiteralText("Upgrade Axe"));
                addUpgrade(shop, upgrades, UpgradeType.SHEARS, new LiteralText("Add Shears"));

                addUpgrade(shop, upgrades, UpgradeType.ARMOR, new LiteralText("Upgrade Armor"));
            }
        });
    }

    private static <T extends Upgrade> void addUpgrade(
            ShopBuilder shop,
            PlayerUpgrades upgrades, UpgradeType<T> type,
            Text name
    ) {
        int currentLevel = upgrades.getLevel(type);
        int nextLevel = currentLevel + 1;

        T nextUpgrade = type.forLevel(nextLevel);
        if (nextUpgrade != null) {
            shop.add(nextUpgrade.getIcon(), nextUpgrade.getCost(), name, () -> {
                upgrades.applyLevel(type, nextLevel);
            });
        } else {
            T currentUpgrade = type.forLevel(currentLevel);
            if (currentUpgrade != null) {
                shop.add(currentUpgrade.getIcon(), Cost.no(), name, () -> {});
            }
        }
    }
}
