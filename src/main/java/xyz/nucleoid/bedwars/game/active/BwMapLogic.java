package xyz.nucleoid.bedwars.game.active;

import net.minecraft.registry.entry.RegistryEntry;
import xyz.nucleoid.bedwars.game.BwMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;

public final class BwMapLogic {
    private final BwActive game;

    BwMapLogic(BwActive game) {
        this.game = game;
    }

    public void tick() {
        ServerWorld world = this.game.world;

        for (BwItemGenerator generator : this.game.map.getItemGenerators()) {
            generator.tick(world, this.game);
        }

        if (world.getTime() % 20 == 0) {
            this.game.teamsStates().forEach(team -> {
                if (team.trapSet) {
                    if (this.tickTrap(team)) {
                        this.game.broadcast.broadcastTrapSetOff(team.team);
                        team.trapSet = false;
                    }
                }

                if (team.healPool) {
                    this.tickHealPool(team);
                }

                if (team.hasteEnabled) {
                    this.tickTeamEffect(team, StatusEffects.HASTE, 1);
                }
            });
        }
    }

    private boolean tickTrap(BwActive.TeamState teamState) {
        ServerWorld world = this.game.world;
        BwMap.TeamRegions regions = this.game.map.getTeamRegions(teamState.team.key());

        if (regions.base() != null) {
            List<PlayerEntity> entities = world.getEntitiesByType(EntityType.PLAYER, regions.base().asBox(), player -> {
                // Filter out creative mode and spectator mode players
                if (player.getAbilities().allowFlying) {
                    return false;
                }

                BwParticipant participant = this.game.participantBy(player);
                return participant != null && !participant.team.equals(teamState.team) && !participant.eliminated;
            });

            if (!entities.isEmpty()) {
                for (PlayerEntity player : entities) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 5, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 5, 1));
                }
                return true;
            }
        }

        return false;
    }

    private void tickHealPool(BwActive.TeamState teamState) {
        ServerWorld world = this.game.world;
        BwMap.TeamRegions regions = this.game.map.getTeamRegions(teamState.team.key());

        if (regions.base() != null) {
            Box box = regions.base().asBox();

            List<PlayerEntity> entities = world.getEntitiesByType(EntityType.PLAYER, box, player -> {
                BwParticipant participant = this.game.participantBy(player);
                return participant != null && participant.team.equals(teamState.team);
            });

            for (PlayerEntity player : entities) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 2, 1, false, false));
            }
        }
    }

    private void tickTeamEffect(BwActive.TeamState teamState, RegistryEntry<StatusEffect> effect, int amplifier) {
        this.game.participantsFor(teamState.team.key()).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) {
                return;
            }

            player.addStatusEffect(new StatusEffectInstance(effect, 20 * 2, amplifier, false, false));
        });
    }
}
