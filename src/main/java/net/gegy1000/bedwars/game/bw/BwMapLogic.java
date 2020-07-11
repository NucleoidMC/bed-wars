package net.gegy1000.bedwars.game.bw;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

import java.util.List;

public final class BwMapLogic {
    private final BedWars game;

    BwMapLogic(BedWars game) {
        this.game = game;
    }

    public void tick() {
        this.game.map.generators().forEach(generator -> generator.tick(this.game.world, this.game.state));

        if (this.game.world.getTime() % 20 == 0) {
            this.game.state.teams().forEach(team -> {
                if (team.trapSet) {
                    if (this.tickTrap(team)) {
                        this.game.broadcast.broadcastTrapSetOff(team);
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

    private boolean tickTrap(BwState.TeamState teamState) {
        BwMap.TeamRegions regions = this.game.map.getTeamRegions(teamState.team);

        if (regions.base != null) {
            List<PlayerEntity> entities = this.game.world.getEntities(EntityType.PLAYER, regions.base.toBox(), player -> {
                BwState.Participant participant = this.game.state.getParticipant(player);
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

    private void tickHealPool(BwState.TeamState teamState) {
        BwMap.TeamRegions regions = this.game.map.getTeamRegions(teamState.team);
        if (regions.base != null) {
            Box box = regions.base.toBox();

            List<PlayerEntity> entities = this.game.world.getEntities(EntityType.PLAYER, box, player -> {
                BwState.Participant participant = this.game.state.getParticipant(player);
                return participant != null && participant.team.equals(teamState.team);
            });

            for (PlayerEntity player : entities) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 2, 1, false, false));
            }
        }
    }

    private void tickTeamEffect(BwState.TeamState teamState, StatusEffect effect, int amplifier) {
        this.game.state.participantsFor(teamState.team).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) {
                return;
            }

            player.addStatusEffect(new StatusEffectInstance(effect, 20 * 2, amplifier, false, false));
        });
    }
}
