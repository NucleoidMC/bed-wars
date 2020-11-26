package xyz.nucleoid.bedwars.game.generator.theme;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.plasmid.registry.TinyRegistry;
import xyz.nucleoid.substrate.biome.BaseBiomeGen;
import xyz.nucleoid.substrate.gen.MapGen;

import net.minecraft.block.BlockState;

public interface MapTheme extends BaseBiomeGen {
	TinyRegistry<Codec<? extends MapTheme>> REGISTRY = TinyRegistry.newStable();
	MapCodec<MapTheme> CODEC = REGISTRY.dispatchMap(MapTheme::getCodec, Function.identity());

	BlockState topState();

	BlockState middleState();

	BlockState stoneState();

	int treeAmt();

	MapGen tree();

	int grassAmt();

	MapGen grass();

	Codec<? extends MapTheme> getCodec();
}
