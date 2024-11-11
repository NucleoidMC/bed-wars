package xyz.nucleoid.bedwars.game.active.modifiers;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;

public final class GameTrigger {
    public static final TinyRegistry<GameTrigger> REGISTRY = TinyRegistry.create();
    public static final Codec<GameTrigger> CODEC = Identifier.CODEC.xmap(REGISTRY::get, REGISTRY::getIdentifier);

    public final boolean tickable;

    private GameTrigger(boolean tickable) {
        this.tickable = tickable;
    }

    public static GameTrigger oneshot() {
        return new GameTrigger(false);
    }

    public static GameTrigger tickable() {
        return new GameTrigger(false);
    }
}
