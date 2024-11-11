package xyz.nucleoid.bedwars.game.active.modifiers;

import xyz.nucleoid.bedwars.BedWars;
import net.minecraft.util.Identifier;

public final class BwGameTriggers {
    public static final GameTrigger GAME_RUNNING = GameTrigger.tickable();
    public static final GameTrigger BED_BROKEN = GameTrigger.oneshot();
    public static final GameTrigger PLAYER_DEATH = GameTrigger.oneshot();
    public static final GameTrigger FINAL_DEATH = GameTrigger.oneshot();

    public static void register() {
        register("game_running", GAME_RUNNING);
        register("bed_broken", BED_BROKEN);
        register("player_death", PLAYER_DEATH);
        register("final_death", FINAL_DEATH);
    }

    private static void register(String identifier, GameTrigger trigger) {
        GameTrigger.REGISTRY.register(Identifier.of(BedWars.ID, identifier), trigger);
    }
}
