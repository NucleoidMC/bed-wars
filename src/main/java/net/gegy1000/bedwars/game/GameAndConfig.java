package net.gegy1000.bedwars.game;

public final class GameAndConfig<T extends Game> {
    private final T game;
    private final ConfiguredGame<T, ?> configured;

    public GameAndConfig(T game, ConfiguredGame<T, ?> configured) {
        this.game = game;
        this.configured = configured;
    }

    public T getGame() {
        return this.game;
    }

    public ConfiguredGame<T, ?> getConfigured() {
        return this.configured;
    }
}
