package xyz.nucleoid.bedwars.game.active.shop;

import fr.catcore.server.translations.api.LocalizableText;
import fr.catcore.server.translations.api.LocalizationTarget;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.bedwars.game.active.upgrade.PlayerUpgrades;
import xyz.nucleoid.bedwars.game.active.upgrade.Upgrade;
import xyz.nucleoid.bedwars.game.active.upgrade.UpgradeType;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopBuilder;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
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
        return ShopUi.create(LocalizableText.asLocalizedFor(
                new TranslatableText("text.bedwars.game.gui.item_shop"), (LocalizationTarget) player),
                shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                DyeColor color = participant.team.getDye();
                shop.addItem(new ItemStack(ColoredBlocks.wool(color), 16), Cost.ofIron(4));
                shop.addItem(new ItemStack(ColoredBlocks.terracotta(color), 16), Cost.ofIron(16));

                ItemStack glass = ItemStackBuilder.of(ColoredBlocks.glass(color))
                        .setName(LocalizableText.asLocalizedFor(new TranslatableText("text.bedwars.game.item.shatterproof_glass"),
                                (LocalizationTarget) player))
                        .setCount(4)
                        .build();

                shop.addItem(glass, Cost.ofIron(12));
            }

            shop.addItem(new ItemStack(Blocks.OAK_PLANKS, 16), Cost.ofGold(4));
            shop.addItem(new ItemStack(Blocks.END_STONE, 12), Cost.ofIron(24));
            shop.addItem(new ItemStack(Blocks.SAND, 4), Cost.ofGold(4));
            shop.addItem(new ItemStack(Blocks.OBSIDIAN, 4), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.COBWEB, 4), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.SCAFFOLDING, 8), Cost.ofGold(4));

            shop.addItem(ItemStackBuilder.of(Items.SHIELD).setUnbreakable().build(), Cost.ofGold(10));
            shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().build(), Cost.ofGold(20));
            shop.addItem(new ItemStack(Items.ARROW, 4), Cost.ofGold(2));
            shop.addItem(
                    ItemStackBuilder.of(Items.STICK)
                            .addEnchantment(Enchantments.KNOCKBACK, 1)
                            .addLore(LocalizableText.asLocalizedFor(new TranslatableText("text.bedwars.game.item.knockback_stick.lore"),
                                    (LocalizationTarget) player))
                            .build(),
                    Cost.ofGold(10)
            );

            ItemStack trident = ItemStackBuilder.of(Items.TRIDENT)
                    .setUnbreakable()
                    .addEnchantment(Enchantments.LOYALTY, 1)
                    .build();

            shop.addItem(trident, Cost.ofEmeralds(6));

            shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.FIRE_CHARGE), Cost.ofIron(50));
            shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
            shop.addItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24));
            shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));
            shop.addItem(new ItemStack(BwItems.CHORUS_FRUIT, 4), Cost.ofGold(8));
            shop.addItem(new ItemStack(BwItems.BRIDGE_EGG), Cost.ofEmeralds(2));

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
            shop.add(ShopEntry.ofIcon(nextUpgrade.getIcon())
                    .withName(name)
                    .withCost(nextUpgrade.getCost())
                    .onBuy(p -> {
                        upgrades.applyLevel(type, nextLevel);
                    }));
        } else {
            T currentUpgrade = type.forLevel(currentLevel);
            if (currentUpgrade != null) {
                shop.add(ShopEntry.ofIcon(currentUpgrade.getIcon()).withName(name));
            }
        }
    }
}
