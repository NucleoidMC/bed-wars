package xyz.nucleoid.bedwars.game.active.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.bedwars.BedWars;
import net.minecraft.util.Identifier;

public final class BwGameModifiers {
    public static void register() {
        register("jump_boost", JumpBoostGameModifier.CODEC);
        register("lightning", LightningGameModifier.CODEC);
    }

    private static void register(String identifier, MapCodec<? extends GameModifier> modifier) {
        GameModifier.REGISTRY.register(Identifier.of(BedWars.ID, identifier), modifier);
    }
}
