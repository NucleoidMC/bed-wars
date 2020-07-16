package net.gegy1000.bedwars.game.upgrade;

import net.gegy1000.bedwars.game.BedWars;
import net.gegy1000.bedwars.game.BwState;
import net.gegy1000.gl.shop.Cost;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Upgrade {
    void applyTo(BedWars game, ServerPlayerEntity player, BwState.Participant participant);

    void removeFrom(BedWars game, ServerPlayerEntity player);

    Item getIcon();

    Cost getCost();
}
