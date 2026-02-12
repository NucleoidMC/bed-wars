package xyz.nucleoid.bedwars.game.active.shop;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayerView;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.ChatFormatting;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.bedwars.game.active.upgrade.PlayerUpgrades;
import xyz.nucleoid.bedwars.game.active.upgrade.Upgrade;
import xyz.nucleoid.bedwars.game.active.upgrade.UpgradeType;
import xyz.nucleoid.plasmid.api.shop.Cost;
import xyz.nucleoid.plasmid.api.shop.ShopEntry;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;
import xyz.nucleoid.plasmid.api.util.Guis;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BwItemShop extends LayeredGui {
    private static final Component MAX_LEVEL_TEXT = Component.translatable("text.bedwars.shop.max_level").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
    private static final int SHOP_X = 1;
    private static final int SHOP_Y = 2;

    private final BwParticipant participant;
    private LayerView currentShop;

    private BwItemShop(ServerPlayer player, BwParticipant participant) {
        super(MenuType.GENERIC_9x5, player, false);
        this.setTitle(Component.translatable("text.bedwars.shop.type.item"));
        this.participant = participant;
        List<GuiElementInterface> navbar = new ArrayList<>();
        DyeColor color = participant.team.config().blockDyeColor();

        this.addNavigationEntry(player.level().getServer(), ColoredBlocks.wool(color).asItem(), "blocks", true, navbar, this::createBlocks);
        this.addNavigationEntry(player.level().getServer(), Items.IRON_SWORD, "weapons", false, navbar, this::createWeapons);
        this.addNavigationEntry(player.level().getServer(), Items.IRON_CHESTPLATE, "armor", false, navbar, this::createArmor);
        this.addNavigationEntry(player.level().getServer(), Items.IRON_PICKAXE, "tools", false, navbar, this::createTools);
        this.addNavigationEntry(player.level().getServer(), Items.FIRE_CHARGE, "utils", false, navbar, this::createUtils);

        Layer navbar1 = Guis.createSelectorLayer(1, 9, navbar);
        this.addLayer(navbar1, 0, 0);
    }

    public static void open(ServerPlayer player, BwActive game) {
        BwParticipant participant = game.participantBy(player);
        if (participant != null) {
            new BwItemShop(player, participant).open();
        }
    }

    private static <T extends Upgrade> void addUpgrade(Consumer<GuiElementInterface> items, PlayerUpgrades upgrades, UpgradeType<T> type, String upgradeName) {

        items.accept(ShopEntry.ofIcon((player, entry) -> {
            int level = upgrades.getLevel(type);
            T levelUp = type.forLevel(level + 1);

            boolean canBuy = entry.canBuy(player);

            var style = Style.EMPTY.withItalic(false).withColor(canBuy && levelUp != null ? ChatFormatting.BLUE : ChatFormatting.RED);
            var name = Component.translatable("text.bedwars.shop.upgrade." + upgradeName).setStyle(style);

            if (levelUp == null) {
                name.append(Component.literal(" (").append(MAX_LEVEL_TEXT).append(")").setStyle(MAX_LEVEL_TEXT.getStyle()));
            } else if (entry.getCost(player) != null) {
                var costText = entry.getCost(player).getDisplay();
                costText = Component.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
                name.append(costText);
            }

            return ItemStackBuilder.of(levelUp != null ? levelUp.getIcon() : type.forLevel(level).getIcon()).set(DataComponents.CUSTOM_NAME, name.withStyle(x -> x.withItalic(x.isItalic()))).build();
        })
                .withCost((p, e) -> {
                    int level = upgrades.getLevel(type);
                    T levelUp = type.forLevel(level + 1);
                    return levelUp != null ? levelUp.getCost() : type.forLevel(level).getCost();
                })
                .onBuyCheck((p, e) -> type.forLevel(upgrades.getLevel(type) + 1) != null && e.getCost(p).takeItems(p))
                .onBuy(p -> upgrades.applyLevel(type, upgrades.getLevel(type) + 1)));
    }

    private static ItemStack createPotion(MobEffectInstance effect, Component name) {
        var stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.CUSTOM_NAME, name.copy().withStyle(x -> x.withItalic(x.isItalic())));
        stack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withEffectAdded(effect));
        return stack;
    }

    private void addNavigationEntry(MinecraftServer server, Item icon, String name, boolean defaultSelected, List<GuiElementInterface> navbar, Consumer<Consumer<GuiElementInterface>> adder) {
        addNavigationEntry(server, icon, name, defaultSelected, navbar, (s, consumer) -> adder.accept(consumer));
    }
    private void addNavigationEntry(MinecraftServer server, Item icon, String name, boolean defaultSelected, List<GuiElementInterface> navbar, BiConsumer<MinecraftServer, Consumer<GuiElementInterface>> adder) {
        List<GuiElementInterface> items = new ArrayList<>();
        adder.accept(server, items::add);

        Layer layer = Guis.createSelectorLayer(3, 7, items);

        var builder = ItemStackBuilder.of(Items.STONE)
                .set(DataComponents.ITEM_MODEL, icon.components().get(DataComponents.ITEM_MODEL))
                .setName(Component.translatable("text.bedwars.shop.category." + name)
                        .setStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.YELLOW)));
        var normal = builder.build();
        var selected = builder.addEnchantment(server, Enchantments.LOYALTY, 0).build();

        navbar.add(new NavbarItem(normal, selected, layer));

        if (defaultSelected) {
            this.currentShop = this.addLayer(layer, SHOP_X, SHOP_Y);
        }
    }

    private void createBlocks(Consumer<GuiElementInterface> items) {
        DyeColor color = participant.team.config().blockDyeColor();
        items.accept(ShopEntry.buyItem(new ItemStack(ColoredBlocks.wool(color), 16), Cost.ofIron(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(ColoredBlocks.terracotta(color), 16), Cost.ofIron(16)));

        ItemStack glass = ItemStackBuilder.of(ColoredBlocks.glass(color))
                .setName(Component.translatable("item.bedwars.shatterproof_glass")).setCount(4).build();

        items.accept(ShopEntry.buyItem(glass, Cost.ofIron(12)));
        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.OAK_PLANKS, 16), Cost.ofGold(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.END_STONE, 12), Cost.ofIron(24)));
        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.OBSIDIAN, 4), Cost.ofEmeralds(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.COBWEB, 4), Cost.ofGold(8)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.SLIME_BLOCK, 4), Cost.ofGold(8)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.LADDER, 16), Cost.ofGold(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.SCAFFOLDING, 8), Cost.ofGold(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.TORCH, 8), Cost.ofGold(1)));
    }

    private void createWeapons(MinecraftServer server, Consumer<GuiElementInterface> items) {
        PlayerUpgrades upgrades = participant.upgrades;
        addUpgrade(items, upgrades, UpgradeType.SWORD, "sword");
        addUpgrade(items, upgrades, UpgradeType.SPEAR, "spear");


        ItemStack knockbackRod = ItemStackBuilder.of(Items.BREEZE_ROD)
                .addEnchantment(server, Enchantments.KNOCKBACK, 1)
                .addLore(Component.translatable("item.bedwars.knockback_stick.description"))
                .build();

        items.accept(ShopEntry.buyItem(knockbackRod, Cost.ofGold(10)));

        ItemStack trident = ItemStackBuilder.of(Items.TRIDENT)
                .setUnbreakable()
                .addEnchantment(server, Enchantments.LOYALTY, 1)
                .build();
        items.accept(ShopEntry.buyItem(trident, Cost.ofEmeralds(6)));

        ItemStack mace = ItemStackBuilder.of(Items.MACE)
                .setUnbreakable()
                .addEnchantment(server, Enchantments.WIND_BURST, 1)
                .build();
        items.accept(ShopEntry.buyItem(mace, Cost.ofEmeralds(8)));

        ItemStack fishing_rod = ItemStackBuilder.of(Items.FISHING_ROD)
                .setUnbreakable()
                .build();
        items.accept(ShopEntry.buyItem(fishing_rod, Cost.ofIron(12)));
        items.accept(ShopEntry.buyItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().build(), Cost.ofGold(12)));
        items.accept(ShopEntry.buyItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(server, Enchantments.POWER, 2).build(), Cost.ofGold(24)));
        items.accept(ShopEntry.buyItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(server, Enchantments.PUNCH, 1).build(), Cost.ofEmeralds(6)));
        ItemStack crossbow = ItemStackBuilder.of(Items.CROSSBOW)
                .setUnbreakable()
                .addEnchantment(server, Enchantments.MULTISHOT, 1)
                .build();
        items.accept(ShopEntry.buyItem(crossbow, Cost.ofGold(24)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.ARROW, 8), Cost.ofGold(2)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.SPECTRAL_ARROW, 4), Cost.ofEmeralds(2)));
    }

    private void createArmor(Consumer<GuiElementInterface> items) {
        PlayerUpgrades upgrades = participant.upgrades;
        addUpgrade(items, upgrades, UpgradeType.ARMOR, "armor");
        items.accept(ShopEntry.buyItem(ItemStackBuilder.of(Items.SHIELD).setUnbreakable().build(), Cost.ofGold(10)));
    }

    private void createTools(Consumer<GuiElementInterface> items) {
        PlayerUpgrades upgrades = participant.upgrades;
        addUpgrade(items, upgrades, UpgradeType.PICKAXE, "pickaxe");
        addUpgrade(items, upgrades, UpgradeType.AXE, "axe");
        addUpgrade(items, upgrades, UpgradeType.SHEARS, "shears");
    }

    private void createUtils(Consumer<GuiElementInterface> items) {

        MobEffectInstance jumpBoostEffect = new MobEffectInstance(MobEffects.JUMP_BOOST, 600, 5);
        items.accept(ShopEntry.buyItem(createPotion(jumpBoostEffect, Component.translatable("item.minecraft.potion.effect.leaping")), Cost.ofEmeralds(1)));

        MobEffectInstance swiftnessEffect = new MobEffectInstance(MobEffects.SPEED, 600, 2);
        items.accept(ShopEntry.buyItem(createPotion(swiftnessEffect, Component.translatable("item.minecraft.potion.effect.swiftness")), Cost.ofEmeralds(1)));

        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.TNT), Cost.ofGold(8)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.FIRE_CHARGE)/*.setCustomName(Text.translatable(EntityType.FIREBALL.getTranslationKey()))*/, Cost.ofIron(40)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.POWDER_SNOW_BUCKET), Cost.ofIron(20)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.WIND_CHARGE), Cost.ofGold(12)));
        items.accept(ShopEntry.buyItem(new ItemStack(BwItems.CHORUS_FRUIT), Cost.ofGold(8)));
        // TODO: collision needs fixing
        // items.accept(ShopEntry.buyItem(new ItemStack(BwItems.BRIDGE_EGG), Cost.ofEmeralds(2)));
        items.accept(ShopEntry.buyItem(new ItemStack(BwItems.MOVING_CLOUD), Cost.ofEmeralds(1)));
    }

    private class NavbarItem implements GuiElementInterface {
        private final ItemStack normal;
        private final ItemStack selected;
        private final Layer layer;

        public NavbarItem(ItemStack normal, ItemStack selected, Layer layer) {
            this.normal = normal;
            this.selected = selected;
            this.layer = layer;
        }

        @Override
        public ItemStack getItemStack() {
            return BwItemShop.this.currentShop.getLayer() == this.layer ? selected : normal;
        }

        @Override
        public ClickCallback getGuiCallback() {
            return (x, y, z, gui) -> {
                BwItemShop.this.removeLayer(BwItemShop.this.currentShop);
                BwItemShop.this.currentShop = BwItemShop.this.addLayer(layer, SHOP_X, SHOP_Y);
            };
        }
    }
}
