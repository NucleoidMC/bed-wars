package net.gegy1000.gl.game;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.CompletableFuture;

public interface Game {
    StartResult requestStart();

    JoinResult offerPlayer(ServerPlayerEntity player);

    boolean isClosed();

    CompletableFuture<Void> stop();
}
