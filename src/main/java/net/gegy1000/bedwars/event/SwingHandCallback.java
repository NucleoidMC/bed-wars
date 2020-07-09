package net.gegy1000.bedwars.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public interface SwingHandCallback {
    Event<SwingHandCallback> EVENT = EventFactory.createArrayBacked(SwingHandCallback.class, listeners -> {
        return (player, hand) -> {
            for (SwingHandCallback listener : listeners) {
                listener.onSwingHand(player, hand);
            }
        };
    });

    void onSwingHand(ServerPlayerEntity player, Hand hand);
}
