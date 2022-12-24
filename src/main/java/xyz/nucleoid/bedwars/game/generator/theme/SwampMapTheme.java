package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import xyz.nucleoid.substrate.gen.GrassGen;
import xyz.nucleoid.substrate.gen.MapGen;
import xyz.nucleoid.substrate.gen.tree.SwampTreeGen;

import java.util.Random;

public final class SwampMapTheme implements MapTheme {
	public static final GrassGen SWAMP_GRASS = new GrassGen(new WeightedList<BlockState>()
			.add(Blocks.GRASS.getDefaultState(), 12)
			.add(Blocks.BLUE_ORCHID.getDefaultState(), 1), 24, 8, 4);

	public static final Codec<SwampMapTheme> CODEC = Codec.unit(new SwampMapTheme());

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
			return Blocks.COARSE_DIRT.getDefaultState();
		}

		return terracotta;
	}

	@Override
	public int treeAmt() {
		return 2;
	}

	@Override
	public MapGen tree() {
		return SwampTreeGen.INSTANCE;
	}

	@Override
	public int grassAmt() {
		return 3;
	}

	@Override
	public MapGen grass() {
		return SWAMP_GRASS;
	}

	@Override
	public Codec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public RegistryKey<Biome> getFakingBiome() {
		return BiomeKeys.SWAMP;
	}
}
