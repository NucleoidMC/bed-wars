package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;
import xyz.nucleoid.bedwars.BedWars;

public final class MapThemes {
	public static void register() {
		register("plains", PlainsMapTheme.CODEC);
		register("desert", DesertMapTheme.CODEC);
		register("forest", ForestMapTheme.CODEC);
		register("aspen_forest", AspenForestMapTheme.CODEC);
		register("taiga", TaigaMapTheme.CODEC);
		register("swamp", SwampMapTheme.CODEC);
	}

	private static void register(String identifier, MapCodec<? extends MapTheme> modifier) {
		MapTheme.REGISTRY.register(Identifier.of(BedWars.ID, identifier), modifier);
	}
}
