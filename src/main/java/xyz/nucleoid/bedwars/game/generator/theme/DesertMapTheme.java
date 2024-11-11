package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import xyz.nucleoid.substrate.gen.CactusGen;
import xyz.nucleoid.substrate.gen.MapGen;
import xyz.nucleoid.substrate.gen.tree.DeadTreeGen;

import java.util.Random;

public final class DesertMapTheme implements MapTheme {
	public static final MapCodec<DesertMapTheme> CODEC = MapCodec.unit(new DesertMapTheme());

	@Override
	public BlockState topState() {
		return Blocks.SAND.getDefaultState();
	}

	@Override
	public BlockState middleState() {
		return Blocks.SANDSTONE.getDefaultState();
	}

	@Override
	public BlockState stoneState() {
		return Blocks.SANDSTONE.getDefaultState();
	}

	@Override
	public BlockState teamIslandState(Random random, BlockState terracotta) {
		if (random.nextInt(4) < 3) {
			return Blocks.SANDSTONE.getDefaultState();
		}

		if (random.nextBoolean()) {
			return Blocks.COARSE_DIRT.getDefaultState();
		}

		return terracotta;
	}

	@Override
	public int treeAmt() {
		return 1;
	}

	@Override
	public MapGen tree() {
		return DeadTreeGen.INSTANCE;
	}

	@Override
	public int grassAmt() {
		return 4;
	}

	@Override
	public MapGen grass() {
		return CactusGen.INSTANCE;
	}

	@Override
	public MapCodec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public RegistryKey<Biome> getFakingBiome() {
		return BiomeKeys.DESERT;
	}
}
