package net.gegy1000.bedwars.game.active.upgrade;

import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.bedwars.game.active.BwParticipant;
import net.gegy1000.plasmid.shop.Cost;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Upgrade {
    void applyTo(BwActive game, ServerPlayerEntity player, BwParticipant participant);

    void removeFrom(BwActive game, ServerPlayerEntity player);

    Item getIcon();

    Cost getCost();
}
