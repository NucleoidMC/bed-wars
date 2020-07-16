package net.gegy1000.bedwars.game.upgrade;

import net.gegy1000.gl.shop.Cost;
import net.minecraft.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class UpgradeType<T extends Upgrade> {
    public static final UpgradeType<ArmorUpgrade> ARMOR = new UpgradeType<ArmorUpgrade>()
            .addLevel(ArmorUpgrade.LEATHER)
            .addLevel(ArmorUpgrade.IRON)
            .addLevel(ArmorUpgrade.DIAMOND);

    public static final UpgradeType<ItemUpgrade> SWORD = new UpgradeType<ItemUpgrade>()
            .addLevel(new ItemUpgrade(Items.WOODEN_SWORD, Cost.no()))
            .addLevel(new ItemUpgrade(Items.STONE_SWORD, Cost.ofIron(12)))
            .addLevel(new ItemUpgrade(Items.IRON_SWORD, Cost.ofGold(6)))
            .addLevel(new ItemUpgrade(Items.DIAMOND_SWORD, Cost.ofEmeralds(3)))
            .setDowngradeOnDeath();

    public static final UpgradeType<ItemUpgrade> PICKAXE = new UpgradeType<ItemUpgrade>()
            .addLevel(new ItemUpgrade(Items.WOODEN_PICKAXE, Cost.ofIron(10)))
            .addLevel(new ItemUpgrade(Items.STONE_PICKAXE, Cost.ofIron(10)))
            .addLevel(new ItemUpgrade(Items.IRON_PICKAXE, Cost.ofGold(6)))
            .addLevel(new ItemUpgrade(Items.DIAMOND_PICKAXE, Cost.ofGold(10)))
            .setDowngradeOnDeath()
            .setCanRemove();

    public static final UpgradeType<ItemUpgrade> AXE = new UpgradeType<ItemUpgrade>()
            .addLevel(new ItemUpgrade(Items.WOODEN_AXE, Cost.ofIron(8)))
            .addLevel(new ItemUpgrade(Items.STONE_AXE, Cost.ofIron(8)))
            .addLevel(new ItemUpgrade(Items.IRON_AXE, Cost.ofGold(4)))
            .addLevel(new ItemUpgrade(Items.DIAMOND_AXE, Cost.ofGold(8)))
            .setDowngradeOnDeath()
            .setCanRemove();

    public static final UpgradeType<ItemUpgrade> SHEARS = new UpgradeType<ItemUpgrade>()
            .addLevel(new ItemUpgrade(Items.SHEARS, Cost.ofIron(40)));

    private final List<T> levels = new ArrayList<>();
    private boolean downgradeOnDeath;
    private boolean canRemove;

    public UpgradeType<T> addLevel(T level) {
        this.levels.add(level);
        return this;
    }

    public UpgradeType<T> setDowngradeOnDeath() {
        this.downgradeOnDeath = true;
        return this;
    }

    public UpgradeType<T> setCanRemove() {
        this.canRemove = true;
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

    public boolean shouldDowngradeOnDeath() {
        return this.downgradeOnDeath;
    }

    public boolean canRemove() {
        return this.canRemove;
    }
}
