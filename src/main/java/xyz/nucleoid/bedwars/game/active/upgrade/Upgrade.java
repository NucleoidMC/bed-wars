package xyz.nucleoid.bedwars.game.active.upgrade;

import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.api.shop.Cost;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Upgrade {
    void applyTo(BwActive game, ServerPlayerEntity player, BwParticipant participant);

    void removeFrom(BwActive game, ServerPlayerEntity player);

    Item getIcon();

    Cost getCost();
}
