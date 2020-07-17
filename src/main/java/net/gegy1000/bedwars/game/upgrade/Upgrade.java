package net.gegy1000.bedwars.game.upgrade;

import net.gegy1000.bedwars.game.BwActive;
import net.gegy1000.bedwars.game.BwParticipant;
import net.gegy1000.gl.shop.Cost;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Upgrade {
    void applyTo(BwActive game, ServerPlayerEntity player, BwParticipant participant);

    void removeFrom(BwActive game, ServerPlayerEntity player);

    Item getIcon();

    Cost getCost();
}
