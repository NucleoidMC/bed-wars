package net.gegy1000.bedwars.game.active.modifiers;

import com.mojang.serialization.Codec;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.gl.game.modifier.GameModifier;
import net.minecraft.util.Identifier;

public final class BwGameModifiers {
    public static void register() {
        register("jump_boost", JumpBoostGameModifier.CODEC);
        register("lightning", LightningGameModifier.CODEC);
    }

    private static void register(String identifier, Codec<? extends GameModifier> modifier) {
        GameModifier.REGISTRY.register(new Identifier(BedWarsMod.ID, identifier), modifier);
    }
}
