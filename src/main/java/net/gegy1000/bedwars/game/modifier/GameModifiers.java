package net.gegy1000.bedwars.game.modifier;

import com.mojang.serialization.Codec;
import net.gegy1000.bedwars.BedWarsMod;
import net.minecraft.util.Identifier;

public final class GameModifiers {
    public static void register() {
        register("jump_boost", JumpBoostGameModifier.CODEC);
        register("lightning", LightningGameModifier.CODEC);
    }

    private static void register(String identifier, Codec<? extends GameModifier> modifier) {
        GameModifier.REGISTRY.register(new Identifier(BedWarsMod.ID, identifier), modifier);
    }
}
