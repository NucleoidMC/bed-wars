package xyz.nucleoid.bedwars.game.active;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import xyz.nucleoid.bedwars.game.config.BwConfig;

public class ItemGeneratorPools {
    public final ItemGeneratorPool teamLvl1;
    public final ItemGeneratorPool teamLvl2;
    public final ItemGeneratorPool teamLvl3;

    public final ItemGeneratorPool DIAMOND;

    public final ItemGeneratorPool EMERALD;

    public ItemGeneratorPools(BwConfig config) {
        var generatorConfig = config.generatorConfig();

        teamLvl1 = new ItemGeneratorPool()
                .add(new ItemStack(Items.IRON_INGOT, 1), generatorConfig.level1().ironSpawnRate())
                .add(new ItemStack(Items.GOLD_INGOT, 1), generatorConfig.level1().goldSpawnRate())
                .spawnInterval(generatorConfig.level1().spawnIntervalTicks());

        teamLvl2 = new ItemGeneratorPool()
                .add(new ItemStack(Items.IRON_INGOT, 1), generatorConfig.level2().ironSpawnRate())
                .add(new ItemStack(Items.GOLD_INGOT, 1), generatorConfig.level2().goldSpawnRate())
                .add(new ItemStack(Items.IRON_INGOT, 2), generatorConfig.level2().ironSpawnRate() / 3)
                .spawnInterval(generatorConfig.level2().spawnIntervalTicks());

        teamLvl3 = new ItemGeneratorPool()
                .add(new ItemStack(Items.IRON_INGOT, 1), generatorConfig.level3().ironSpawnRate())
                .add(new ItemStack(Items.IRON_INGOT, 2), generatorConfig.level3().ironSpawnRate() / 2)
                .add(new ItemStack(Items.GOLD_INGOT, 1), generatorConfig.level3().goldSpawnRate())
                .add(new ItemStack(Items.GOLD_INGOT, 2), generatorConfig.level3().goldSpawnRate() / 3)
                .add(new ItemStack(Items.EMERALD, 1), 1)
                .spawnInterval(generatorConfig.level3().spawnIntervalTicks());

        DIAMOND = new ItemGeneratorPool()
                .add(new ItemStack(Items.DIAMOND, 1), generatorConfig.diamond())
                .spawnInterval(generatorConfig.diamondSpawnInterval());

        EMERALD = new ItemGeneratorPool()
                .add(new ItemStack(Items.EMERALD, 1), generatorConfig.emerald())
                .spawnInterval(generatorConfig.emeraldSpawnInterval());
    }
}
