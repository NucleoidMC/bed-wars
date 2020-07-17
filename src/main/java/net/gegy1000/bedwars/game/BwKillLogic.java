package net.gegy1000.bedwars.game;

import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class BwKillLogic {
    private static final Set<Item> RESOURCE_ITEMS = Sets.newHashSet(
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.DIAMOND,
            Items.EMERALD
    );

    private final BwActive game;

    BwKillLogic(BwActive game) {
        this.game = game;
    }

    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        BwParticipant attackerParticipant = null;
        Entity attacker = source.getAttacker();
        if (attacker != null && attacker instanceof ServerPlayerEntity) {
            attackerParticipant = this.game.getParticipant(attacker.getUuid());
        }

        Collection<ItemStack> resources = this.takeResources(player);
        if (attackerParticipant != null) {
            ServerPlayerEntity attackerPlayer = (ServerPlayerEntity) attacker;
            for (ItemStack resource : resources) {
                attackerPlayer.inventory.offerOrDrop(this.game.map.getWorld(), resource);
            }
        }
    }

    private Collection<ItemStack> takeResources(ServerPlayerEntity fromPlayer) {
        List<ItemStack> resources = new ArrayList<>();

        PlayerInventory inventory = fromPlayer.inventory;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (RESOURCE_ITEMS.contains(stack.getItem())) {
                ItemStack removed = inventory.removeStack(slot);
                if (!removed.isEmpty()) {
                    resources.add(removed);
                }
            }
        }

        return resources;
    }
}
