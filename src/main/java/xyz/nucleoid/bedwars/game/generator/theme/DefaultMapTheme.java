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

public final class DefaultMapTheme implements MapTheme {
	public static final Codec<DefaultMapTheme> CODEC = Codec.unit(new DefaultMapTheme());

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
		return 2;
	}

	@Override
	public MapGen tree() {
		return PoplarTreeGen.INSTANCE;
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
	public Codec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public RegistryKey<Biome> getFakingBiome() {
		return BiomeKeys.PLAINS;
	}
}
