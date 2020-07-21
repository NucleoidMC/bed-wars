package net.gegy1000.gl.game.event;

import net.gegy1000.gl.game.Game;

public interface GameCloseListener {
    EventType<GameCloseListener> EVENT = EventType.create(GameCloseListener.class, listeners -> {
        return game -> {
            for (GameCloseListener listener : listeners) {
                listener.close(game);
            }
        };
    });

    void close(Game game);
}
