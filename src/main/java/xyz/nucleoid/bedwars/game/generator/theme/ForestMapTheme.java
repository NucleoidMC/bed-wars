package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import xyz.nucleoid.substrate.gen.GrassGen;
import xyz.nucleoid.substrate.gen.MapGen;
import xyz.nucleoid.substrate.gen.tree.PoplarTreeGen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public final class ForestMapTheme implements MapTheme {
	public static final Codec<ForestMapTheme> CODEC = Codec.unit(new ForestMapTheme());

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
	public int treeAmt() {
		return 6;
	}

	@Override
	public MapGen tree() {
		return PoplarTreeGen.INSTANCE;
	}

	@Override
	public int grassAmt() {
		return 8;
	}

	@Override
	public MapGen grass() {
		return GrassGen.INSTANCE;
	}

	@Override
	public Codec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public RegistryKey<Biome> getFakingBiome() {
		return BiomeKeys.FOREST;
	}
}
