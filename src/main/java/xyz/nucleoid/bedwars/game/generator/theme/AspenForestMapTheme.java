package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import xyz.nucleoid.substrate.gen.GrassGen;
import xyz.nucleoid.substrate.gen.MapGen;
import xyz.nucleoid.substrate.gen.tree.AspenTreeGen;

import java.util.Random;

public final class AspenForestMapTheme implements MapTheme {
	public static final MapCodec<AspenForestMapTheme> CODEC = MapCodec.unit(new AspenForestMapTheme());

	@Override
	public BlockState topState() {
		return Blocks.GRASS_BLOCK.getDefaultState();
	}

	@Override
	public BlockState middleState() {
		return Blocks.DIRT.getDefaultState();
	}

	@Override
	public BlockState stoneState() {
		return Blocks.STONE.getDefaultState();
	}

	@Override
	public BlockState teamIslandState(Random random, BlockState terracotta) {
		if (random.nextInt(4) < 3) {
			return Blocks.GRASS_BLOCK.getDefaultState();
		}

		if (random.nextBoolean()) {
			return Blocks.COBBLESTONE.getDefaultState();
		}

		return terracotta;
	}

	@Override
	public int treeAmt() {
		return 3;
	}

	@Override
	public MapGen tree() {
		return AspenTreeGen.INSTANCE;
	}

	@Override
	public int grassAmt() {
		return 4;
	}

	@Override
	public MapGen grass() {
		return GrassGen.INSTANCE;
	}

	@Override
	public MapCodec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public RegistryKey<Biome> getFakingBiome() {
		return BiomeKeys.BIRCH_FOREST;
	}
}
