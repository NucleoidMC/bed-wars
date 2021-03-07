package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
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

	private static void register(String identifier, Codec<? extends MapTheme> modifier) {
		MapTheme.REGISTRY.register(new Identifier(BedWars.ID, identifier), modifier);
	}
}
