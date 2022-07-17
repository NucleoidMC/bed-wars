package xyz.nucleoid.bedwars.game.active.shop;

import com.google.common.collect.ImmutableList;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayerView;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.bedwars.game.active.upgrade.PlayerUpgrades;
import xyz.nucleoid.bedwars.game.active.upgrade.Upgrade;
import xyz.nucleoid.bedwars.game.active.upgrade.UpgradeType;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.Guis;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BwItemShop extends LayeredGui {
    private static final Text MAX_LEVEL_TEXT = Text.translatable("text.bedwars.shop.max_level").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
    private static final int SHOP_X = 1;
    private static final int SHOP_Y = 2;

    private final BwParticipant participant;
    private LayerView currentShop;

    private BwItemShop(ServerPlayerEntity player, BwParticipant participant) {
        super(ScreenHandlerType.GENERIC_9X5, player, false);
        this.setTitle(Text.translatable("text.bedwars.shop.type.item"));
        this.participant = participant;
        List<GuiElementInterface> navbar = new ArrayList<>();

        this.addNavigationEntry(Items.END_STONE, "blocks", true, navbar, this::createBlocks);
        this.addNavigationEntry(Items.DIAMOND_SWORD, "weapons", false, navbar, this::createWeapons);
        this.addNavigationEntry(Items.IRON_CHESTPLATE, "armor", false, navbar, this::createArmor);
        this.addNavigationEntry(Items.STONE_PICKAXE, "tools", false, navbar, this::createTools);
        this.addNavigationEntry(Items.POTION, "utils", false, navbar, this::createUtils);

        Layer navbar1 = Guis.createSelectorLayer(1, 9, navbar);
        this.addLayer(navbar1, 0, 0);
    }

    public static void open(ServerPlayerEntity player, BwActive game) {
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

            var style = Style.EMPTY.withItalic(false).withColor(canBuy && levelUp != null ? Formatting.BLUE : Formatting.RED);
            var name = Text.translatable("text.bedwars.shop.upgrade." + upgradeName).setStyle(style);

            if (levelUp == null) {
                name.append(Text.literal(" (").append(MAX_LEVEL_TEXT).append(")").setStyle(MAX_LEVEL_TEXT.getStyle()));
            } else if (entry.getCost(player) != null) {
                var costText = entry.getCost(player).getDisplay();
                costText = Text.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
                name.append(costText);
            }

            return ItemStackBuilder.of(levelUp != null ? levelUp.getIcon() : type.forLevel(level).getIcon()).setName(name).build();
        })
                .withCost((p, e) -> {
                    int level = upgrades.getLevel(type);
                    T levelUp = type.forLevel(level + 1);
                    return levelUp != null ? levelUp.getCost() : type.forLevel(level).getCost();
                })
                .onBuyCheck((p, e) -> type.forLevel(upgrades.getLevel(type) + 1) != null && e.getCost(p).takeItems(p))
                .onBuy(p -> upgrades.applyLevel(type, upgrades.getLevel(type) + 1)));
    }

    private static ItemStack createPotion(StatusEffectInstance effect) {
        return PotionUtil.setCustomPotionEffects(new ItemStack(Items.POTION), ImmutableList.of(effect));
    }

    private void addNavigationEntry(Item icon, String name, boolean defaultSelected, List<GuiElementInterface> navbar, Consumer<Consumer<GuiElementInterface>> adder) {
        List<GuiElementInterface> items = new ArrayList<>();
        adder.accept(items::add);

        Layer layer = Guis.createSelectorLayer(3, 7, items);

        var builder = ItemStackBuilder.of(icon)
                .setName(Text.translatable("text.bedwars.shop.category." + name)
                        .setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.YELLOW)))
                .hideFlags();
        var normal = builder.build();
        var selected = builder.addEnchantment(Enchantments.LOYALTY, 0).build();

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
                .setName(Text.translatable("item.bedwars.shatterproof_glass")).setCount(4).build();

        items.accept(ShopEntry.buyItem(glass, Cost.ofIron(12)));
        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.OAK_PLANKS, 16), Cost.ofGold(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.END_STONE, 12), Cost.ofIron(24)));
        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.OBSIDIAN, 4), Cost.ofEmeralds(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.COBWEB, 4), Cost.ofGold(8)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.SCAFFOLDING, 8), Cost.ofGold(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.TORCH, 8), Cost.ofGold(1)));
    }

    private void createWeapons(Consumer<GuiElementInterface> items) {
        PlayerUpgrades upgrades = participant.upgrades;
        addUpgrade(items, upgrades, UpgradeType.SWORD, "sword");


        ItemStack knockbackStick = ItemStackBuilder.of(Items.STICK)
                .addEnchantment(Enchantments.KNOCKBACK, 1)
                .addLore(Text.translatable("item.bedwars.knockback_stick.description"))
                .build();

        items.accept(ShopEntry.buyItem(knockbackStick, Cost.ofGold(10)));

        ItemStack trident = ItemStackBuilder.of(Items.TRIDENT)
                .setUnbreakable()
                .addEnchantment(Enchantments.LOYALTY, 1)
                .build();
        items.accept(ShopEntry.buyItem(trident, Cost.ofEmeralds(6)));

        items.accept(ShopEntry.buyItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().build(), Cost.ofGold(12)));
        items.accept(ShopEntry.buyItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(Enchantments.POWER, 2).build(), Cost.ofGold(24)));
        items.accept(ShopEntry.buyItem(ItemStackBuilder.of(Items.BOW).setUnbreakable().addEnchantment(Enchantments.PUNCH, 1).build(), Cost.ofEmeralds(6)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.ARROW, 8), Cost.ofGold(2)));
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

        StatusEffectInstance jumpBoostEffect = new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 5);
        items.accept(ShopEntry.buyItem(createPotion(jumpBoostEffect).setCustomName(Text.translatable("item.minecraft.potion.effect.leaping")), Cost.ofEmeralds(1)));

        StatusEffectInstance swiftnessEffect = new StatusEffectInstance(StatusEffects.SPEED, 600, 2);
        items.accept(ShopEntry.buyItem(createPotion(swiftnessEffect).setCustomName(Text.translatable("item.minecraft.potion.effect.swiftness")), Cost.ofEmeralds(1)));

        items.accept(ShopEntry.buyItem(new ItemStack(Blocks.TNT), Cost.ofGold(8)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.FIRE_CHARGE).setCustomName(Text.translatable(EntityType.FIREBALL.getTranslationKey())), Cost.ofIron(40)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.ENDER_PEARL), Cost.ofEmeralds(4)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.WATER_BUCKET), Cost.ofGold(10)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.LAVA_BUCKET), Cost.ofGold(24)));
        items.accept(ShopEntry.buyItem(new ItemStack(Items.GOLDEN_APPLE), Cost.ofGold(3)));
        items.accept(ShopEntry.buyItem(new ItemStack(BwItems.CHORUS_FRUIT).setCustomName(Text.translatable(Items.CHORUS_FRUIT.getTranslationKey())), Cost.ofGold(8)));
        items.accept(ShopEntry.buyItem(new ItemStack(BwItems.BRIDGE_EGG), Cost.ofEmeralds(2)));
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
