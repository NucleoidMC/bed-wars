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

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveConstants.COMPRESSION_TYPE;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.fix.ItemSpawnEggFix;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.resource.ResourceManager.Empty;
import net.minecraft.server.command.EffectCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public final class BwItemShop {
    public static ShopUi create(ServerPlayerEntity player, BwActive game, int pageIndex) {
        if(pageIndex == 0){
            //TODO quick buy
            return old(player, game);
        }
        //FIXME Item sorting, through Plasmid Shop
        else if(pageIndex == 1){
            // Blocks UI
            return ShopUi.create(new LiteralText("Blocks"), shop -> {
                BwParticipant participant = game.getParticipant(player);
                if (participant != null) {
                    // Navigation
                    addNavbar(shop, player, game);
                    //Blocks
                    shop.addItem(new ItemStack(Items.IRON_SWORD, 0), Cost.free());
                    DyeColor color = participant.team.getDye();
                    shop.addItem(new ItemStack(ColoredBlocks.wool(color), 16), Cost.ofIron(4));
                    shop.addItem(new ItemStack(ColoredBlocks.terracotta(color), 16), Cost.ofIron(16));
    
                    ItemStack glass = ItemStackBuilder.of(ColoredBlocks.glass(color))
                            .setName(new LiteralText("Shatterproof Glass"))
                            .setCount(4)
                            .build();
    
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
        else if(pageIndex == 2){
            return ShopUi.create(new LiteralText("Melee Weapons"), shop -> {
                BwParticipant participant = game.getParticipant(player);
                if (participant != null) {
                    // Navigation
                    addNavbar(shop, player, game);
                    // Weapons
                    PlayerUpgrades upgrades = participant.upgrades;
                    addUpgrade(shop, upgrades, UpgradeType.SWORD, new LiteralText("Upgrade Sword"));
                    shop.addItem(ItemStackBuilder.of(Items.STICK)
                    .addEnchantment(Enchantments.KNOCKBACK, 1)
                    .addLore(new LiteralText("Haha, target go zoom"))
                    .build(), Cost.ofGold(5)); 
                    ItemStack trident = ItemStackBuilder.of(Items.TRIDENT)
                    .setUnbreakable()
                    .addEnchantment(Enchantments.LOYALTY, 1)
                    .build();

                    shop.addItem(trident, Cost.ofEmeralds(6));               
                }
            });
        }
        else if(pageIndex == 3){
            return ShopUi.create(new LiteralText("Armor"), shop -> {
                BwParticipant participant = game.getParticipant(player);
                if (participant != null) {
                    // Navigation
                    addNavbar(shop, player, game);
                    //Armor
                    PlayerUpgrades upgrades = participant.upgrades;
                    addUpgrade(shop, upgrades, UpgradeType.ARMOR, new LiteralText("Upgrade Armor"));
                    shop.addItem(ItemStackBuilder.of(Items.SHIELD).setUnbreakable().build(), Cost.ofGold(10));
                }
            });
        }
        else if(pageIndex == 4){
            return ShopUi.create(new LiteralText("Tools"), shop -> {
                BwParticipant participant = game.getParticipant(player);
                if(participant != null){
                    // Navigation
                    addNavbar(shop, player, game);
                    // Tool upgrades
                    PlayerUpgrades upgrades = participant.upgrades;
                    addUpgrade(shop, upgrades, UpgradeType.PICKAXE, new LiteralText("Upgrade Pickaxe"));
                    addUpgrade(shop, upgrades, UpgradeType.AXE, new LiteralText("Upgrade Axe"));
                    addUpgrade(shop, upgrades, UpgradeType.SHEARS, new LiteralText("Add Shears"));
                }
            });
        }
        else if(pageIndex == 5){
            return ShopUi.create(new LiteralText("Archery"), shop -> {
                BwParticipant participant = game.getParticipant(player);
                if(participant != null){
                    // Navigation
                    addNavbar(shop, player, game);
                    // Bows and arrows
                    shop.addItem(new ItemStack(Items.BOW), Cost.ofGold(12));
                    shop.addItem(ItemStackBuilder.of(Items.BOW).addEnchantment(Enchantments.POWER, 2).build(), Cost.ofGold(24));
                    shop.addItem(ItemStackBuilder.of(Items.BOW).addEnchantment(Enchantments.PUNCH, 1).build(), Cost.ofEmeralds(6));
                    shop.addItem(new ItemStack(Items.ARROW, 8), Cost.ofGold(2));
                }
            });
        }
        else if(pageIndex == 6){
            return ShopUi.create(new LiteralText("Utilitiesand Potions"), shop -> {
                BwParticipant participant = game.getParticipant(player);
                if(participant != null){
                    // Navigation
                    addNavbar(shop, player, game);
                    // Potions
                    shop.addItem(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.STRONG_LEAPING), Cost.ofEmeralds(1));
                    shop.addItem(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.STRONG_SWIFTNESS), Cost.ofEmeralds(1));
                    shop.addItem(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), Cost.ofEmeralds(2));
                    shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
                    shop.addItem(new ItemStack(Items.FIRE_CHARGE).setCustomName(new LiteralText("Fireball")), Cost.ofIron(40));
                    shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
                    shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
                    shop.addItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24));
                    shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));
                    shop.addItem(new ItemStack(BwItems.CHORUS_FRUIT).setCustomName(new LiteralText("Chorus Fruit")), Cost.ofGold(8));
                    shop.addItem(new ItemStack(BwItems.BRIDGE_EGG), Cost.ofEmeralds(2));
                    shop.addItem(new ItemStack(BwItems.MOVING_CLOUD), Cost.ofEmeralds(2));
                }
            });
        }
        return old(player, game);
    }

    private static void addNavbar(ShopBuilder shop, ServerPlayerEntity player, BwActive game) {
        //Creates a page navigation bar at the top of the shop
        shop.add(ShopEntry.ofIcon(Blocks.END_STONE).withName(new LiteralText("Blocks")).withCost(Cost.free()).onBuy(page -> {
            player.closeHandledScreen();
            player.openHandledScreen(BwItemShop.create(player, game, 1));
        }));
        shop.add(ShopEntry.ofIcon(Items.GOLDEN_SWORD).withName(new LiteralText("Melee Weapons")).withCost(Cost.free()).onBuy(page -> {
            player.closeHandledScreen();
            player.openHandledScreen(BwItemShop.create(player, game, 2));
        }));
        shop.add(ShopEntry.ofIcon(Items.IRON_CHESTPLATE).withName(new LiteralText("Armor")).withCost(Cost.free()).onBuy(page -> {
            player.closeHandledScreen();
            player.openHandledScreen(BwItemShop.create(player, game, 3));
        }));
        shop.add(ShopEntry.ofIcon(Items.IRON_PICKAXE).withName(new LiteralText("Tools")).withCost(Cost.free()).onBuy(page -> {
            player.closeHandledScreen();
            player.openHandledScreen(BwItemShop.create(player, game, 4));
        }));
        shop.add(ShopEntry.ofIcon(Items.BOW).withName(new LiteralText("Archery")).withCost(Cost.free()).onBuy(page -> {
            player.closeHandledScreen();
            player.openHandledScreen(BwItemShop.create(player, game, 5));
        }));
        shop.add(ShopEntry.ofIcon(Items.TNT).withName(new LiteralText("Utilities and Potions")).withCost(Cost.free()).onBuy(page -> {
            player.closeHandledScreen();
            player.openHandledScreen(BwItemShop.create(player, game, 6));
        }));
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
    private static ShopUi old(ServerPlayerEntity player, BwActive game){
        return ShopUi.create(new LiteralText("Item Shop"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant != null) {
                DyeColor color = participant.team.getDye();
                shop.addItem(new ItemStack(ColoredBlocks.wool(color), 16), Cost.ofIron(4));
                shop.addItem(new ItemStack(ColoredBlocks.terracotta(color), 16), Cost.ofIron(16));

                ItemStack glass = ItemStackBuilder.of(ColoredBlocks.glass(color))
                        .setName(new LiteralText("Shatterproof Glass"))
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

            shop.addItem(new ItemStack(Items.TORCH, 8), Cost.ofGold(1));

            shop.addItem(ItemStackBuilder.of(Items.SHIELD).setUnbreakable().build(), Cost.ofGold(10));
            shop.addItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().build(), Cost.ofGold(20));
            shop.addItem(new ItemStack(Items.ARROW, 8), Cost.ofGold(2));
            shop.addItem(
                    ItemStackBuilder.of(Items.STICK)
                            .addEnchantment(Enchantments.KNOCKBACK, 1)
                            .addLore(new LiteralText("Haha, target go zoom"))
                            .build(),
                    Cost.ofGold(7)
            );

            ItemStack trident = ItemStackBuilder.of(Items.TRIDENT)
                    .setUnbreakable()
                    .addEnchantment(Enchantments.LOYALTY, 1)
                    .build();

            shop.addItem(trident, Cost.ofEmeralds(6));

            shop.addItem(new ItemStack(Blocks.TNT), Cost.ofGold(8));
            shop.addItem(new ItemStack(Items.FIRE_CHARGE).setCustomName(new LiteralText("Fireball")), Cost.ofIron(40));
            shop.addItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4));
            shop.addItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10));
            shop.addItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24));
            shop.addItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3));
            shop.addItem(new ItemStack(BwItems.CHORUS_FRUIT, 4), Cost.ofGold(8));
            shop.addItem(new ItemStack(BwItems.BRIDGE_EGG), Cost.ofEmeralds(2));
            shop.addItem(new ItemStack(BwItems.MOVING_CLOUD), Cost.ofEmeralds(2));

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
    //TODO add to the plasmid colored blocks library
    public static Block glassPane(DyeColor color) {
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
