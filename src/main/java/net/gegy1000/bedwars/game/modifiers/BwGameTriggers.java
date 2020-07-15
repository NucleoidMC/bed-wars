package net.gegy1000.bedwars.game.modifiers;

import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.gl.game.modifier.GameTrigger;
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
        GameTrigger.REGISTRY.register(new Identifier(BedWarsMod.ID, identifier), trigger);
    }
}
