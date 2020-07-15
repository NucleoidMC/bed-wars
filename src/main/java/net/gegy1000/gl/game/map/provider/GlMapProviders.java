package net.gegy1000.gl.game.map.provider;

import com.mojang.serialization.Codec;
import net.gegy1000.gl.GameLib;
import net.minecraft.util.Identifier;

public final class GlMapProviders {
    public static void register() {
        register("path", PathMapProvider.CODEC);
        register("random", RandomMapProvider.codec());
    }

    private static <T extends MapProvider<?>> void register(String key, Codec<T> codec) {
        MapProvider.REGISTRY.register(new Identifier(GameLib.ID, key), codec);
    }
}
