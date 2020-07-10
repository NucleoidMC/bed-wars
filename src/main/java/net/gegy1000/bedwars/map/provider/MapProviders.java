package net.gegy1000.bedwars.map.provider;

import com.mojang.serialization.Codec;
import net.gegy1000.bedwars.BedWarsMod;
import net.minecraft.util.Identifier;

public final class MapProviders {
    public static void register() {
        register("path", PathMapProvider.CODEC);
        register("random", RandomMapProvider.codec());
    }

    private static <T extends MapProvider<?>> void register(String key, Codec<T> codec) {
        MapProvider.REGISTRY.register(new Identifier(BedWarsMod.ID, key), codec);
    }
}
