package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import xyz.nucleoid.substrate.gen.CactusGen;
import xyz.nucleoid.substrate.gen.MapGen;
import xyz.nucleoid.substrate.gen.tree.DeadTreeGen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public final class DesertMapTheme implements MapTheme {
	public static final Codec<DesertMapTheme> CODEC = Codec.unit(new DesertMapTheme());

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
	public Codec<? extends MapTheme> getCodec() {
		return CODEC;
	}

	@Override
	public RegistryKey<Biome> getFakingBiome() {
		return BiomeKeys.DESERT;
	}
}
