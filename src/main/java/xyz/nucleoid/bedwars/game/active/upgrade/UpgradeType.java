package xyz.nucleoid.bedwars.game.active.upgrade;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.plasmid.api.shop.Cost;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class UpgradeType<T extends Upgrade> {
    public static final UpgradeType<ArmorUpgrade> ARMOR = new UpgradeType<ArmorUpgrade>()
            .addLevel(ArmorUpgrade.LEATHER)
            .addLevel(ArmorUpgrade.IRON)
            .addLevel(ArmorUpgrade.DIAMOND);

    public static final UpgradeType<WeaponUpgrade> SWORD = new UpgradeType<WeaponUpgrade>()
            .addLevel(new WeaponUpgrade(Items.WOODEN_SWORD, Cost.no()))
            .addLevel(new WeaponUpgrade(Items.STONE_SWORD, Cost.ofIron(12)))
            .addLevel(new WeaponUpgrade(Items.IRON_SWORD, Cost.ofGold(6)))
            .addLevel(new WeaponUpgrade(Items.DIAMOND_SWORD, Cost.ofEmeralds(3)));

    public static final UpgradeType<WeaponUpgrade> PICKAXE = new UpgradeType<WeaponUpgrade>()
            .addLevel(new WeaponUpgrade(Items.WOODEN_PICKAXE, Cost.ofIron(8)))
            .addLevel(new WeaponUpgrade(Items.STONE_PICKAXE, Cost.ofIron(12)))
            .addLevel(new WeaponUpgrade(Items.IRON_PICKAXE, Cost.ofGold(4)))
            .addLevel(new WeaponUpgrade(Items.DIAMOND_PICKAXE, (s) -> diamondTool(s, Items.DIAMOND_PICKAXE), Cost.ofGold(12)));

    public static final UpgradeType<WeaponUpgrade> AXE = new UpgradeType<WeaponUpgrade>()
            .addLevel(new WeaponUpgrade(Items.WOODEN_AXE, Cost.ofIron(8)))
            .addLevel(new WeaponUpgrade(Items.STONE_AXE, Cost.ofIron(12)))
            .addLevel(new WeaponUpgrade(Items.IRON_AXE, Cost.ofGold(4)))
            .addLevel(new WeaponUpgrade(Items.DIAMOND_AXE, (s) -> diamondTool(s, Items.DIAMOND_AXE), Cost.ofGold(8)));

    public static final UpgradeType<WeaponUpgrade> SHEARS = new UpgradeType<WeaponUpgrade>()
            .addLevel(new WeaponUpgrade(Items.SHEARS, Cost.ofIron(40)));

    private static ItemStack diamondTool(MinecraftServer server, Item item) {
        ItemStack stack = new ItemStack(item);
        stack.addEnchantment(server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY), 2);
        return stack;
    }

    private final List<T> levels = new ArrayList<>();

    public UpgradeType<T> addLevel(T level) {
        this.levels.add(level);
        return this;
    }

    @Nullable
    public T forLevel(int level) {
        if (!this.containsLevel(level)) {
            return null;
        }
        return this.levels.get(level);
    }

    public boolean containsLevel(int level) {
        return level >= 0 && level < this.levels.size();
    }
}
