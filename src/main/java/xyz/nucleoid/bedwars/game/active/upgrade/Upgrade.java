package xyz.nucleoid.bedwars.game.active.upgrade;

import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.api.shop.Cost;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerPlayer;

public interface Upgrade {
    void applyTo(BwActive game, ServerPlayer player, BwParticipant participant);

    void removeFrom(BwActive game, ServerPlayer player);

    Item getIcon();

    Cost getCost();
}
