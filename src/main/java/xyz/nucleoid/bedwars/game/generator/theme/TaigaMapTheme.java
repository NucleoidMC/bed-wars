package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import xyz.nucleoid.bedwars.game.generator.gen.TaigaTreeGen;
import xyz.nucleoid.substrate.gen.GrassGen;
import xyz.nucleoid.substrate.gen.MapGen;

import java.util.Random;

public final class TaigaMapTheme implements MapTheme {
	public static final MapCodec<TaigaMapTheme> CODEC = MapCodec.unit(new TaigaMapTheme());
	private static final MapGen TAIGA_GRASS = new GrassGen(
			new ShufflingList<BlockState>()
					.add(Blocks.SHORT_GRASS.defaultBlockState(), 32)
					.add(Blocks.FERN.defaultBlockState(), 16)
					.add(Blocks.DANDELION.defaultBlockState(), 1)
					.add(Blocks.POPPY.defaultBlockState(), 1),
			16, 8, 4);

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
			return Blocks.COBBLESTONE.defaultBlockState();
		}

		return terracotta;
	}

	@Override
	public int treeAmt() {
		return 4;
	}

	@Override
	public MapGen tree() {
		return TaigaTreeGen.INSTANCE;
	}

	@Override
	public int grassAmt() {
		return 4;
	}

	@Override
	public MapGen grass() {
		return TAIGA_GRASS;
	}

	@Override
	public MapCodec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public ResourceKey<Biome> getFakingBiome() {
		return Biomes.TAIGA;
	}
}
