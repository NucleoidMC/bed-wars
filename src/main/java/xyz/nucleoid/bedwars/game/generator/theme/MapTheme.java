package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import xyz.nucleoid.plasmid.registry.TinyRegistry;
import xyz.nucleoid.substrate.biome.BaseBiomeGen;
import xyz.nucleoid.substrate.gen.MapGen;

import java.util.Random;
import java.util.function.Function;

public interface MapTheme extends BaseBiomeGen {
	TinyRegistry<Codec<? extends MapTheme>> REGISTRY = TinyRegistry.create();
	MapCodec<MapTheme> CODEC = REGISTRY.dispatchMap(MapTheme::getCodec, Function.identity());

	BlockState topState();

	BlockState middleState();

	BlockState stoneState();

	BlockState teamIslandState(Random random, BlockState terracotta);

	int treeAmt();

	MapGen tree();

	int grassAmt();

	MapGen grass();

	Codec<? extends MapTheme> getCodec();
}
