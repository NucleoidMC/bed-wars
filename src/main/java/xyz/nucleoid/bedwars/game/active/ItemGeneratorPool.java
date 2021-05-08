package xyz.nucleoid.bedwars.game.active;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.WeightedList;

import java.util.Random;

public final class ItemGeneratorPool {
    public static final ItemGeneratorPool TEAM_LVL_1 = new ItemGeneratorPool()
            .add(new ItemStack(Items.IRON_INGOT, 2), 10)
            .add(new ItemStack(Items.GOLD_INGOT, 1), 2)
            .spawnInterval(34);

    public static final ItemGeneratorPool TEAM_LVL_2 = new ItemGeneratorPool()
            .add(new ItemStack(Items.IRON_INGOT, 2), 10)
            .add(new ItemStack(Items.IRON_INGOT, 4), 3)
            .add(new ItemStack(Items.GOLD_INGOT, 1), 3)
            .spawnInterval(30);

    public static final ItemGeneratorPool TEAM_LVL_3 = new ItemGeneratorPool()
            .add(new ItemStack(Items.IRON_INGOT, 2), 36)
            .add(new ItemStack(Items.IRON_INGOT, 4), 18)
            .add(new ItemStack(Items.GOLD_INGOT, 1), 9)
            .add(new ItemStack(Items.GOLD_INGOT, 2), 3)
            .add(new ItemStack(Items.EMERALD, 1), 1)
            .spawnInterval(26);

    public static final ItemGeneratorPool DIAMOND = new ItemGeneratorPool()
            .add(new ItemStack(Items.DIAMOND, 1), 1)
            .spawnInterval(20 * 45);

    public static final ItemGeneratorPool EMERALD = new ItemGeneratorPool()
            .add(new ItemStack(Items.EMERALD, 1), 1)
            .spawnInterval(20 * 90);

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
