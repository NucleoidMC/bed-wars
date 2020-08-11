package xyz.nucleoid.bedwars.game.active;

import com.google.common.collect.Sets;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.bedwars.game.active.upgrade.UpgradeType;
import xyz.nucleoid.plasmid.util.PlayerRef;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

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

    public void onPlayerDeath(BwParticipant participant, ServerPlayerEntity player, DamageSource source) {
        if (!this.game.config.keepInventory) {
            this.applyDowngrades(participant);
        }

        BwParticipant killerParticipant = this.getKillerParticipant(player.world.getTime(), participant, source);
        ServerPlayerEntity killerPlayer = killerParticipant != null ? killerParticipant.player() : null;

        if (killerPlayer != null) {
            this.transferResources(player, killerPlayer);
        }

        BwMap.TeamSpawn spawn = this.game.teamLogic.tryRespawn(participant);
        this.game.broadcast.broadcastDeath(player, killerPlayer, source, spawn == null);

        // Run death modifiers
        this.game.triggerModifiers(BwGameTriggers.PLAYER_DEATH);

        if (spawn != null) {
            this.game.playerLogic.respawnOnTimer(player, spawn);
        } else {
            this.onFinalDeath(participant, player);
        }

        this.game.scoreboard.markDirty();
    }

    private BwParticipant getKillerParticipant(long time, BwParticipant participant, DamageSource source) {
        BwParticipant attackerParticipant = null;
        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity) {
            attackerParticipant = this.game.getParticipant(PlayerRef.of((PlayerEntity) attacker));
        }

        if (attackerParticipant == null) {
            AttackRecord lastAttack = participant.lastAttack;
            if (lastAttack != null && lastAttack.isValid(time)) {
                attackerParticipant = this.game.getParticipant(lastAttack.player);
            }
        }

        return attackerParticipant;
    }

    private void applyDowngrades(BwParticipant participant) {
        participant.upgrades.tryDowngrade(UpgradeType.SWORD);
        participant.upgrades.tryDowngrade(UpgradeType.PICKAXE);
        participant.upgrades.tryDowngrade(UpgradeType.AXE);
    }

    private void transferResources(ServerPlayerEntity player, ServerPlayerEntity killerPlayer) {
        Collection<ItemStack> resources = this.takeResources(player);
        for (ItemStack resource : resources) {
            killerPlayer.inventory.offerOrDrop(this.game.world, resource);
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

    private void onFinalDeath(BwParticipant participant, ServerPlayerEntity player) {
        this.dropEnderChest(player, participant);

        this.game.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.game.spawnLogic.spawnAtCenter(player);

        this.game.winStateLogic.eliminatePlayer(participant);

        // Run final death modifiers
        this.game.triggerModifiers(BwGameTriggers.FINAL_DEATH);
    }

    private void dropEnderChest(ServerPlayerEntity player, BwParticipant participant) {
        ServerWorld world = this.game.world;
        EnderChestInventory enderChest = player.getEnderChestInventory();

        BwMap.TeamRegions teamRegions = this.game.map.getTeamRegions(participant.team);
        if (teamRegions.spawn != null) {
            Vec3d dropSpawn = teamRegions.spawn.getCenter();

            for (int slot = 0; slot < enderChest.size(); slot++) {
                ItemStack stack = enderChest.removeStack(slot);
                if (!stack.isEmpty()) {
                    world.spawnEntity(new ItemEntity(world, dropSpawn.x, dropSpawn.y + 0.5, dropSpawn.z, stack));
                }
            }
        }

        enderChest.clear();
    }
}
