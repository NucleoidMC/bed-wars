package net.gegy1000.bedwars.game.active;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.WeightedList;

import java.util.Random;

public final class ItemGeneratorPool {
    public static final ItemGeneratorPool TEAM_LVL_1 = new ItemGeneratorPool()
            .add(new ItemStack(Items.IRON_INGOT, 1), 10)
            .add(new ItemStack(Items.GOLD_INGOT, 1), 3)
            .spawnInterval(30);

    public static final ItemGeneratorPool TEAM_LVL_2 = new ItemGeneratorPool()
            .add(new ItemStack(Items.IRON_INGOT, 1), 10)
            .add(new ItemStack(Items.GOLD_INGOT, 1), 3)
            .add(new ItemStack(Items.IRON_INGOT, 2), 8)
            .add(new ItemStack(Items.GOLD_INGOT, 2), 2)
            .spawnInterval(26);

    public static final ItemGeneratorPool TEAM_LVL_3 = new ItemGeneratorPool()
            .add(new ItemStack(Items.IRON_INGOT, 2), 14)
            .add(new ItemStack(Items.GOLD_INGOT, 1), 3)
            .add(new ItemStack(Items.EMERALD, 1), 1)
            .spawnInterval(22);

    public static final ItemGeneratorPool DIAMOND = new ItemGeneratorPool()
            .add(new ItemStack(Items.DIAMOND, 1), 1)
            .spawnInterval(20 * 40);

    public static final ItemGeneratorPool EMERALD = new ItemGeneratorPool()
            .add(new ItemStack(Items.EMERALD, 1), 1)
            .spawnInterval(20 * 80);

    private final WeightedList<ItemStack> pool = new WeightedList<>();
    private long spawnInterval = 10;

    public ItemGeneratorPool add(ItemStack stack, int weight) {
        this.pool.add(stack, weight);
        return this;
    }

    public ItemGeneratorPool spawnInterval(long spawnInterval) {
        this.spawnInterval = spawnInterval;
        return this;
    }

    public ItemStack sample(Random random) {
        return this.pool.pickRandom(random).copy();
    }

    public long getSpawnInterval() {
        return this.spawnInterval;
    }
}
