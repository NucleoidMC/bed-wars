package xyz.nucleoid.bedwars.game.generator.theme;

import java.util.Random;

import com.mojang.serialization.Codec;
import xyz.nucleoid.substrate.gen.GrassGen;
import xyz.nucleoid.substrate.gen.MapGen;
import xyz.nucleoid.substrate.gen.tree.PoplarTreeGen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public final class TaigaMapTheme implements MapTheme {
	public static final Codec<TaigaMapTheme> CODEC = Codec.unit(new TaigaMapTheme());
	private static final MapGen TAIGA_TREE = new PoplarTreeGen(Blocks.SPRUCE_LOG.getDefaultState(), Blocks.SPRUCE_LEAVES.getDefaultState().with(Properties.DISTANCE_1_7, 1));
	private static final MapGen TAIGA_GRASS = new GrassGen(
			new WeightedList<BlockState>()
					.add(Blocks.GRASS.getDefaultState(), 32)
					.add(Blocks.FERN.getDefaultState(), 16)
					.add(Blocks.DANDELION.getDefaultState(), 1)
					.add(Blocks.POPPY.getDefaultState(), 1),
			16, 8, 4);

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
		return 4;
	}

	@Override
	public MapGen tree() {
		return TAIGA_TREE;
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
	public Codec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public RegistryKey<Biome> getFakingBiome() {
		return BiomeKeys.TAIGA;
	}
}
