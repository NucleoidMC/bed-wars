package xyz.nucleoid.bedwars.game.active.shop;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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

import java.util.function.BiFunction;

public final class BwItemShop {
    private static final Cost NAVIGATION_BAR_COST = Cost.free().display(new LiteralText("select"));

    public static ShopUi create(ServerPlayerEntity player, BwActive game) {
        // At some point add a user customised menu
        return createBlocks(player, game);
    }

    private static void addNavbar(ShopBuilder shop, ServerPlayerEntity player, BwActive game, int pageIndex) {
        addNavigationEntry(shop, player, game, Items.END_STONE, new LiteralText("Blocks"), pageIndex == 1, BwItemShop::createBlocks);
        addNavigationEntry(shop, player, game, Items.IRON_SWORD, new LiteralText("Melee Weapons"), pageIndex == 2, BwItemShop::createMelee);
        addNavigationEntry(shop, player, game, Items.IRON_CHESTPLATE, new LiteralText("Armor"), pageIndex == 3, BwItemShop::createArmor);
        addNavigationEntry(shop, player, game, Items.IRON_PICKAXE, new LiteralText("Tools"), pageIndex == 4, BwItemShop::createTools);
        addNavigationEntry(shop, player, game, Items.BOW, new LiteralText("Archery"), pageIndex == 5, BwItemShop::createArchery);
        addNavigationEntry(shop, player, game, Items.POTION, new LiteralText("Utilities and Potions"), pageIndex == 6, BwItemShop::createUtils);
    }

    private static void addNavigationEntry(ShopBuilder shop, ServerPlayerEntity player, BwActive game, ItemConvertible icon, Text name, boolean selected, BiFunction<ServerPlayerEntity, BwActive, ShopUi> open) {
        ItemStack iconStack = new ItemStack(icon);
        if (selected) {
            iconStack = addEnchantGlint(iconStack);
        }

        shop.add(ShopEntry.ofIcon(iconStack).withName(name).withCost(NAVIGATION_BAR_COST).onBuy(page -> {
            player.closeHandledScreen();
            player.openHandledScreen(open.apply(player, game));
        }));
    }

    private static <T extends Upgrade> void addUpgrade(ShopBuilder shop, PlayerUpgrades upgrades, UpgradeType<T> type, Text name) {
        int currentLevel = upgrades.getLevel(type);
        int nextLevel = currentLevel + 1;

        T nextUpgrade = type.forLevel(nextLevel);
        if (nextUpgrade != null) {
            shop.add(ShopEntry.ofIcon(nextUpgrade.getIcon()).withName(name).withCost(nextUpgrade.getCost()).onBuy(p -> {
                upgrades.applyLevel(type, nextLevel);
            }));
        } else {
            T currentUpgrade = type.forLevel(currentLevel);
            if (currentUpgrade != null) {
                shop.add(ShopEntry.ofIcon(currentUpgrade.getIcon()).withName(name));
            }
        }
    }

    // Method for returning an ItemStack with NBT for an enchant glint
    private static ItemStack addEnchantGlint(ItemStack stack) {
        stack.addEnchantment(Enchantments.LURE, 1);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("HideFlags", 1);
        return stack;
    }

    private static ShopUi createBlocks(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Blocks"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                // Navigation
                addNavbar(shop, player, game, 1);
                // Blocks

                DyeColor color = participant.team.getDye();
                shop.addItem(new ItemStack(ColoredBlocks.wool(color), 16), Cost.ofIron(4));
                shop.addItem(new ItemStack(ColoredBlocks.terracotta(color), 16), Cost.ofIron(16));

                ItemStack glass = ItemStackBuilder.of(ColoredBlocks.glass(color))
                        .setName(new LiteralText("Shatterproof Glass")).setCount(4).build();

                shop.addItem(glass, Cost.ofIron(12));
                shop.addItem(new ItemStack(Blocks.OAK_PLANKS, 16), Cost.ofGold(4));
                shop.addItem(new ItemStack(Blocks.END_STONE, 12), Cost.ofIron(24));
                shop.addItem(new ItemStack(Blocks.OBSIDIAN, 4), Cost.ofEmeralds(4));
                shop.addItem(new ItemStack(Items.COBWEB, 4), Cost.ofGold(8));
                shop.addItem(new ItemStack(Items.SCAFFOLDING, 8), Cost.ofGold(4));
                shop.addItem(new ItemStack(Items.TORCH, 8), Cost.ofGold(1));
            }
        });
    }

    private static ShopUi createMelee(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Melee Weapons"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                addNavbar(shop, player, game, 2);

                PlayerUpgrades upgrades = participant.upgrades;
                addUpgrade(shop, upgrades, UpgradeType.SWORD, new LiteralText("Upgrade Sword"));

                ItemStack knockbackStick = ItemStackBuilder.of(Items.STICK)
                        .addEnchantment(Enchantments.KNOCKBACK, 1)
                        .addLore(new LiteralText("Haha, target go zoom"))
                        .build();
                shop.addItem(knockbackStick, Cost.ofGold(10));

                ItemStack trident = ItemStackBuilder.of(Items.TRIDENT)
                        .setUnbreakable()
                        .addEnchantment(Enchantments.LOYALTY, 1)
                        .build();
                shop.addItem(trident, Cost.ofEmeralds(6));
            }
        });
    }

    private static ShopUi createArmor(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Armor"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                addNavbar(shop, player, game, 3);

                PlayerUpgrades upgrades = participant.upgrades;
                addUpgrade(shop, upgrades, UpgradeType.ARMOR, new LiteralText("Upgrade Armor"));
                shop.addItem(ItemStackBuilder.of(Items.SHIELD).setUnbreakable().build(), Cost.ofGold(10));
            }
        });
    }

    private static ShopUi createTools(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Tools"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                addNavbar(shop, player, game, 4);

                PlayerUpgrades upgrades = participant.upgrades;
                addUpgrade(shop, upgrades, UpgradeType.PICKAXE, new LiteralText("Upgrade Pickaxe"));
                addUpgrade(shop, upgrades, UpgradeType.AXE, new LiteralText("Upgrade Axe"));
                addUpgrade(shop, upgrades, UpgradeType.SHEARS, new LiteralText("Add Shears"));
            }
        });
    }

    private static ShopUi createArchery(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Archery"), shop -> {
            addNavbar(shop, player, game, 5);

            shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().build(), Cost.ofGold(12));
            shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(Enchantments.POWER, 2).build(), Cost.ofGold(24));
            shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(Enchantments.PUNCH, 1).build(), Cost.ofEmeralds(6));
            shop.addItem(new ItemStack(Items.ARROW, 8), Cost.ofGold(2));
        });
    }

    private static ShopUi createUtils(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Utilities and Potions"), shop -> {
            addNavbar(shop, player, game, 6);

            StatusEffectInstance jumpBoostEffect = new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 5);
            shop.addItem(createPotion(jumpBoostEffect).setCustomName(new LiteralText("Potion of Leaping")), Cost.ofEmeralds(1));

            StatusEffectInstance swiftnessEffect = new StatusEffectInstance(StatusEffects.SPEED, 600, 2);
            shop.addItem(createPotion(swiftnessEffect).setCustomName(new LiteralText("Potion of Swiftness")), Cost.ofEmeralds(1));

            shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.FIRE_CHARGE).setCustomName(new LiteralText("Fireball")), Cost.ofIron(40));
            shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
            shop.addItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24));
            shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));
            shop.addItem(new ItemStack(BwItems.CHORUS_FRUIT).setCustomName(new LiteralText("Chorus Fruit")), Cost.ofGold(8));
            shop.addItem(new ItemStack(BwItems.BRIDGE_EGG), Cost.ofEmeralds(2));
            shop.addItem(new ItemStack(BwItems.MOVING_CLOUD), Cost.ofEmeralds(1));
        });
    }

    private static ItemStack createPotion(StatusEffectInstance effect) {
        return PotionUtil.setCustomPotionEffects(new ItemStack(Items.POTION), ImmutableList.of(effect));
    }
}
