package xyz.nucleoid.bedwars.game.active;

import net.minecraft.core.Holder;
import xyz.nucleoid.bedwars.game.BwMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class BwMapLogic {
    private final BwActive game;

    BwMapLogic(BwActive game) {
        this.game = game;
    }

    public void tick() {
        ServerLevel world = this.game.world;

        for (BwItemGenerator generator : this.game.map.getItemGenerators()) {
            generator.tick(world, this.game);
        }

        if (world.getGameTime() % 20 == 0) {
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
                    this.tickTeamEffect(team, MobEffects.HASTE, 1);
                }
            });
        }
    }

    private boolean tickTrap(BwActive.TeamState teamState) {
        ServerLevel world = this.game.world;
        BwMap.TeamRegions regions = this.game.map.getTeamRegions(teamState.team.key());

        if (regions.base() != null) {
            List<Player> entities = world.getEntities(EntityType.PLAYER, regions.base().asBox(), player -> {
                // Filter out creative mode and spectator mode players
                if (player.getAbilities().mayfly) {
                    return false;
                }

                BwParticipant participant = this.game.participantBy(player);
                return participant != null && !participant.team.equals(teamState.team) && !participant.eliminated;
            });

            if (!entities.isEmpty()) {
                for (Player player : entities) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 5, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 5, 1));
                }
                return true;
            }
        }

        return false;
    }

    private void tickHealPool(BwActive.TeamState teamState) {
        ServerLevel world = this.game.world;
        BwMap.TeamRegions regions = this.game.map.getTeamRegions(teamState.team.key());

        if (regions.base() != null) {
            AABB box = regions.base().asBox();

            List<Player> entities = world.getEntities(EntityType.PLAYER, box, player -> {
                BwParticipant participant = this.game.participantBy(player);
                return participant != null && participant.team.equals(teamState.team);
            });

            for (Player player : entities) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 2, 1, false, false));
            }
        }
    }

    private void tickTeamEffect(BwActive.TeamState teamState, Holder<MobEffect> effect, int amplifier) {
        this.game.participantsFor(teamState.team.key()).forEach(participant -> {
            ServerPlayer player = participant.player();
            if (player == null) {
                return;
            }

            player.addEffect(new MobEffectInstance(effect, 20 * 2, amplifier, false, false));
        });
    }
}
