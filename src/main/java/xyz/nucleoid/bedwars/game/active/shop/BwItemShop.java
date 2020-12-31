package xyz.nucleoid.bedwars.game.active.shop;

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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public final class BwItemShop {
    public static ShopUi create(ServerPlayerEntity player, BwActive game) {
        //TODO User preferred items page
        return createBlocks(player, game);
        //FIXME Item sorting, through Plasmid Shop
    }

    private static void addNavbar(ShopBuilder shop, ServerPlayerEntity player, BwActive game, int pageIndex) {
        // Creates a page navigation bar at the top of the shop
        if(pageIndex==1){
            shop.add(ShopEntry.ofIcon(getEnchantGlint(new ItemStack(Blocks.END_STONE))).withName(new LiteralText("Blocks")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createBlocks(player, game));
            }));
        }
        else{
            shop.add(ShopEntry.ofIcon(new ItemStack(Blocks.END_STONE)).withName(new LiteralText("Blocks")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createBlocks(player, game));
            }));
        }
        if(pageIndex==2){
            shop.add(ShopEntry.ofIcon(getEnchantGlint(new ItemStack(Items.IRON_SWORD))).withName(new LiteralText("Melee Weapons")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createMelee(player, game));
            }));
        }
        else{
            shop.add(ShopEntry.ofIcon(Items.IRON_SWORD).withName(new LiteralText("Melee Weapons")).withCost(Cost.free())
                .onBuy(page -> {
                    player.closeHandledScreen();
                    player.openHandledScreen(BwItemShop.createMelee(player, game));
            }));
        }
        if(pageIndex==3){
            shop.add(ShopEntry.ofIcon(getEnchantGlint(new ItemStack(Items.IRON_CHESTPLATE))).withName(new LiteralText("Armor")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createArmor(player, game));
            }));
        }
        else{
            shop.add(ShopEntry.ofIcon(Items.IRON_CHESTPLATE).withName(new LiteralText("Armor")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createArmor(player, game));
            }));
        }
        if(pageIndex==4){
            shop.add(ShopEntry.ofIcon(getEnchantGlint(new ItemStack(Items.IRON_PICKAXE))).withName(new LiteralText("Tools")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createTools(player, game));
            }));
        }
        else{
            shop.add(ShopEntry.ofIcon(Items.IRON_PICKAXE).withName(new LiteralText("Tools")).withCost(Cost.free())
                .onBuy(page -> {
                    player.closeHandledScreen();
                    player.openHandledScreen(BwItemShop.createTools(player, game));
            }));
        }
        if(pageIndex==5){
            shop.add(ShopEntry.ofIcon(getEnchantGlint(new ItemStack(Items.BOW))).withName(new LiteralText("Archery")).withCost(Cost.free()).onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createArchery(player, game));
            }));
        }
        else{
            shop.add(ShopEntry.ofIcon(Items.BOW).withName(new LiteralText("Archery")).withCost(Cost.free()).onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createArchery(player, game));
            }));
        }
        if(pageIndex==6){
            shop.add(ShopEntry.ofIcon(getEnchantGlint(new ItemStack(Items.TNT))).withName(new LiteralText("Utilities and Potions")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createUtils(player, game));
            }));
        }
        else{
            shop.add(ShopEntry.ofIcon(Items.TNT).withName(new LiteralText("Utilities and Potions")).withCost(Cost.free())
            .onBuy(page -> {
                player.closeHandledScreen();
                player.openHandledScreen(BwItemShop.createUtils(player, game));
            }));
        }
    }

    private static <T extends Upgrade> void addUpgrade(ShopBuilder shop, PlayerUpgrades upgrades, UpgradeType<T> type,
            Text name) {
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
    private static ItemStack getEnchantGlint(ItemStack base){
        base.addEnchantment(Enchantments.LURE, 1);
        CompoundTag tag = base.getOrCreateTag();
        tag.putInt("HideFlags", 1);
        return base;
    }

    private static ShopUi createBlocks(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Blocks"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                // Navigation
                addNavbar(shop, player, game, 1);
                // Blocks
                shop.addItem(new ItemStack(Items.IRON_SWORD, 0), Cost.free());
                DyeColor color = participant.team.getDye();
                shop.addItem(new ItemStack(ColoredBlocks.wool(color), 16), Cost.ofIron(4));
                shop.addItem(new ItemStack(ColoredBlocks.terracotta(color), 16), Cost.ofIron(4));

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
                // Navigation
                addNavbar(shop, player, game, 2);
                // Weapons
                PlayerUpgrades upgrades = participant.upgrades;
                addUpgrade(shop, upgrades, UpgradeType.SWORD, new LiteralText("Upgrade Sword"));
                shop.addItem(ItemStackBuilder.of(Items.STICK).addEnchantment(Enchantments.KNOCKBACK, 1)
                        .addLore(new LiteralText("Haha, target go zoom")).build(), Cost.ofGold(10));
                ItemStack trident = ItemStackBuilder.of(Items.TRIDENT).setUnbreakable()
                        .addEnchantment(Enchantments.LOYALTY, 1).build();

                shop.addItem(trident, Cost.ofEmeralds(6));
            }
        });
    }

    private static ShopUi createArmor(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Armor"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                // Navigation
                addNavbar(shop, player, game, 3);
                // Armor
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
                // Navigation
                addNavbar(shop, player, game, 4);
                // Tool upgrades
                PlayerUpgrades upgrades = participant.upgrades;
                addUpgrade(shop, upgrades, UpgradeType.PICKAXE, new LiteralText("Upgrade Pickaxe"));
                addUpgrade(shop, upgrades, UpgradeType.AXE, new LiteralText("Upgrade Axe"));
                addUpgrade(shop, upgrades, UpgradeType.SHEARS, new LiteralText("Add Shears"));
            }
        });
    }

    private static ShopUi createArchery(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Archery"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                // Navigation
                addNavbar(shop, player, game, 5);
                // Bows and arrows
                shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().build(), Cost.ofGold(12));
                shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(Enchantments.POWER, 2).build(),
                        Cost.ofGold(24));
                shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(Enchantments.PUNCH, 1).build(),
                        Cost.ofEmeralds(6));
                shop.addItem(new ItemStack(Items.ARROW, 8), Cost.ofGold(2));
            }
        });
    }

    private static ShopUi createUtils(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new LiteralText("Utilities and Potions"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                // Navigation
                addNavbar(shop, player, game, 6);
                // Potions
                shop.addItem(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.STRONG_LEAPING),
                        Cost.ofEmeralds(1));
                shop.addItem(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.STRONG_SWIFTNESS),
                        Cost.ofEmeralds(1));
                shop.addItem(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY),
                        Cost.ofEmeralds(2));
                shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
                shop.addItem(new ItemStack(Items.FIRE_CHARGE).setCustomName(new LiteralText("Fireball")),
                        Cost.ofIron(40));
                shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
                shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
                shop.addItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24));
                shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));
                shop.addItem(new ItemStack(BwItems.CHORUS_FRUIT).setCustomName(new LiteralText("Chorus Fruit")),
                        Cost.ofGold(8));
                shop.addItem(new ItemStack(BwItems.BRIDGE_EGG), Cost.ofEmeralds(2));
                shop.addItem(new ItemStack(BwItems.MOVING_CLOUD), Cost.ofEmeralds(2));
            }
        });
    }
}
