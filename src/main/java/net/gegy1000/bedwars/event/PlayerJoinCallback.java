package net.gegy1000.bedwars.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerJoinCallback {
    Event<PlayerJoinCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinCallback.class, listeners -> {
        return player -> {
            for (PlayerJoinCallback listener : listeners) {
                listener.onJoin(player);
            }
        };
    });

    void onJoin(ServerPlayerEntity player);
}
