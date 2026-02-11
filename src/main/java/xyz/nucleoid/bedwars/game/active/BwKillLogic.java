package xyz.nucleoid.bedwars.game.active;

import com.google.common.collect.Sets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.bedwars.game.active.upgrade.UpgradeType;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

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

    public void onPlayerDeath(BwParticipant participant, ServerPlayer player, DamageSource source) {
        if (!this.game.config.keepInventory()) {
            this.applyDowngrades(participant);
        }

        BwParticipant killerParticipant = this.getAttackerParticipant(participant, source);
        ServerPlayer killerPlayer = killerParticipant != null ? killerParticipant.player() : null;

        if (killerPlayer != null) {
            this.transferResources(player, killerPlayer);
        }

        BwMap.TeamSpawn spawn = this.game.teamLogic.tryRespawn(participant);
        this.game.broadcast.broadcastDeath(player, killerPlayer, spawn == null);

        // Run death modifiers
        this.game.triggerModifiers(BwGameTriggers.PLAYER_DEATH);

        if (spawn != null) {
            this.game.spawnLogic.respawnPlayer(player, GameType.SPECTATOR);
            this.game.spawnLogic.spawnAtCenter(player);

            this.game.playerLogic.startRespawning(player, spawn);
        } else {
            this.onFinalDeath(participant, player);
        }
    }

    private BwParticipant getAttackerParticipant(BwParticipant participant, DamageSource source) {
        BwParticipant attackerParticipant = null;
        Entity attacker = source.getEntity();
        if (attacker instanceof ServerPlayer) {
            attackerParticipant = this.game.participantBy(PlayerRef.of((Player) attacker));
        }

        if (attackerParticipant == null) {
            AttackRecord lastAttack = participant.lastAttack;
            if (lastAttack != null && lastAttack.isValid(this.game.world.getGameTime())) {
                attackerParticipant = this.game.participantBy(lastAttack.player);
            }
        }

        return attackerParticipant;
    }

    private void applyDowngrades(BwParticipant participant) {
        participant.upgrades.tryDowngrade(UpgradeType.SWORD);
        participant.upgrades.tryDowngrade(UpgradeType.SPEAR);
        participant.upgrades.tryDowngrade(UpgradeType.PICKAXE);
        participant.upgrades.tryDowngrade(UpgradeType.AXE);
    }

    private void transferResources(ServerPlayer player, ServerPlayer killerPlayer) {
        Collection<ItemStack> resources = this.takeResources(player);
        for (ItemStack resource : resources) {
            killerPlayer.getInventory().placeItemBackInInventory(resource);
        }
    }

    private Collection<ItemStack> takeResources(ServerPlayer fromPlayer) {
        List<ItemStack> resources = new ArrayList<>();

        Inventory inventory = fromPlayer.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (RESOURCE_ITEMS.contains(stack.getItem())) {
                ItemStack removed = inventory.removeItemNoUpdate(slot);
                if (!removed.isEmpty()) {
                    resources.add(removed);
                }
            }
        }

        return resources;
    }

    private void onFinalDeath(BwParticipant participant, ServerPlayer player) {
        this.dropEnderChest(player, participant);

        this.game.spawnLogic.resetPlayer(player, GameType.SPECTATOR);
        this.game.spawnLogic.spawnAtCenter(player);

        this.game.winStateLogic.eliminatePlayer(participant);

        // Run final death modifiers
        this.game.triggerModifiers(BwGameTriggers.FINAL_DEATH);
    }

    private void dropEnderChest(ServerPlayer player, BwParticipant participant) {
        ServerLevel world = this.game.world;
        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();

        BwMap.TeamRegions teamRegions = this.game.map.getTeamRegions(participant.team.key());
        if (teamRegions.spawn() != null) {
            Vec3 dropSpawn = teamRegions.spawn().center();

            for (int slot = 0; slot < enderChest.getContainerSize(); slot++) {
                ItemStack stack = enderChest.removeItemNoUpdate(slot);
                if (!stack.isEmpty()) {
                    world.addFreshEntity(new ItemEntity(world, dropSpawn.x, dropSpawn.y + 0.5, dropSpawn.z, stack));
                }
            }
        }

        enderChest.clearContent();
    }
}
