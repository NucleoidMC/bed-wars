package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;
import xyz.nucleoid.substrate.biome.BaseBiomeGen;
import xyz.nucleoid.substrate.gen.MapGen;

import java.util.Random;
import java.util.function.Function;

public interface MapTheme extends BaseBiomeGen {
	TinyRegistry<MapCodec<? extends MapTheme>> REGISTRY = TinyRegistry.create();
	MapCodec<MapTheme> CODEC = REGISTRY.dispatchMap(MapTheme::getCodec, Function.identity());

	BlockState topState();

	BlockState middleState();

	BlockState stoneState();

	BlockState teamIslandState(Random random, BlockState terracotta);

	int treeAmt();

	MapGen tree();

	int grassAmt();

	MapGen grass();

	MapCodec<? extends MapTheme> getCodec();
}
