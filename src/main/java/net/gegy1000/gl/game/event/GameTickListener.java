package net.gegy1000.gl.game.event;

import net.gegy1000.gl.game.Game;

public interface GameTickListener {
    EventType<GameTickListener> EVENT = EventType.create(GameTickListener.class, listeners -> {
        return game -> {
            for (GameTickListener listener : listeners) {
                listener.tick(game);
            }
        };
    });

    void tick(Game game);
}
