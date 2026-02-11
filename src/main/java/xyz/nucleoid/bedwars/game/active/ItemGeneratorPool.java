package xyz.nucleoid.bedwars.game.active;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.behavior.ShufflingList;

public final class ItemGeneratorPool {
    private final ShufflingList<ItemStack> pool = new ShufflingList<>();
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
