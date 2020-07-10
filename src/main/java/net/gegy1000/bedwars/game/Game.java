package net.gegy1000.bedwars.game;

import java.util.concurrent.CompletableFuture;

public interface Game {
    boolean isActive();

    CompletableFuture<Void> stop();
}
