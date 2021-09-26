package xyz.nucleoid.bedwars.game.active;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.WeightedList;

public final class ItemGeneratorPool {
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

    public ItemStack sample() {
        return this.pool.shuffle().stream().findFirst().get().copy();
    }

    public long getSpawnInterval() {
        return this.spawnInterval;
    }
}
