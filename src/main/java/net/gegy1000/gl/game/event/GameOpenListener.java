package net.gegy1000.gl.game.event;

import net.gegy1000.gl.game.Game;

public interface GameOpenListener {
    EventType<GameOpenListener> EVENT = EventType.create(GameOpenListener.class, listeners -> {
        return game -> {
            for (GameOpenListener listener : listeners) {
                listener.open(game);
            }
        };
    });

    void open(Game game);
}
