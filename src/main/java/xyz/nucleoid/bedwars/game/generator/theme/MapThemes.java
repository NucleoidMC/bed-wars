package xyz.nucleoid.bedwars.game.generator.theme;

import com.mojang.serialization.Codec;
import xyz.nucleoid.bedwars.BedWars;

import net.minecraft.util.Identifier;

public final class MapThemes {
	public static void register() {
		register("default", DefaultMapTheme.CODEC);
		register("desert", DesertMapTheme.CODEC);
	}

	private static void register(String identifier, Codec<? extends MapTheme> modifier) {
		MapTheme.REGISTRY.register(new Identifier(BedWars.ID, identifier), modifier);
	}
}
