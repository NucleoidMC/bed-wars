package net.gegy1000.bedwars.game;

import net.gegy1000.gl.game.JoinResult;
import net.minecraft.server.network.ServerPlayerEntity;

public interface BwPhase {
    JoinResult offerPlayer(ServerPlayerEntity player);

    default void start() {
    }

    default void stop() {
    }
}
