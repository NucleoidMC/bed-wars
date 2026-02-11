package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import xyz.nucleoid.substrate.gen.GrassGen;
import xyz.nucleoid.substrate.gen.MapGen;
import xyz.nucleoid.substrate.gen.tree.SwampTreeGen;

import java.util.Random;

public final class SwampMapTheme implements MapTheme {
	public static final GrassGen SWAMP_GRASS = new GrassGen(new ShufflingList<BlockState>()
			.add(Blocks.SHORT_GRASS.defaultBlockState(), 12)
			.add(Blocks.BLUE_ORCHID.defaultBlockState(), 1), 24, 8, 4);

	public static final MapCodec<SwampMapTheme> CODEC = MapCodec.unit(new SwampMapTheme());

	@Override
	public BlockState topState() {
		return Blocks.GRASS_BLOCK.defaultBlockState();
	}

	@Override
	public BlockState middleState() {
		return Blocks.DIRT.defaultBlockState();
	}

	@Override
	public BlockState stoneState() {
		return Blocks.STONE.defaultBlockState();
	}

	@Override
	public BlockState teamIslandState(Random random, BlockState terracotta) {
		if (random.nextInt(4) < 3) {
			return Blocks.GRASS_BLOCK.defaultBlockState();
		}

		if (random.nextBoolean()) {
			return Blocks.COARSE_DIRT.defaultBlockState();
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
	public MapCodec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public ResourceKey<Biome> getFakingBiome() {
		return Biomes.SWAMP;
	}
}
