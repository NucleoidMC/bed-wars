package xyz.nucleoid.bedwars.game.active;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public final class BwTeamLogic {
    private final BwActive game;

    BwTeamLogic(BwActive game) {
        this.game = game;
    }

    public void applyEnchantments(GameTeam team) {
        this.game.participantsFor(team).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player != null) {
                this.game.playerLogic.applyEnchantments(player, participant);
            }
        });
    }

    public boolean canRespawn(BwParticipant participant) {
        return this.tryRespawn(participant) != null;
    }

    @Nullable
    public BwMap.TeamSpawn tryRespawn(BwParticipant participant) {
        BwActive.TeamState teamState = this.game.getTeam(participant.team);
        if (teamState != null && teamState.hasBed) {
            return this.game.map.getTeamSpawn(participant.team);
        }

        return null;
    }

    public void onBedBroken(ServerPlayerEntity player, BlockPos pos) {
        GameTeam destroyerTeam = null;

        var participant = this.game.getParticipant(player);
        if (participant != null && !participant.eliminated) {
            destroyerTeam = participant.team;
        }

        var bed = this.findBed(pos);
        if (bed == null || bed.team.equals(destroyerTeam)) {
            return;
        }

        if (this.removeBed(bed.team)) {
            this.game.broadcast.broadcastBedBroken(player, bed.team, destroyerTeam);
        }
    }

    public boolean removeBed(GameTeam team) {
        BwActive.TeamState teamState = this.game.getTeam(team);
        if (teamState != null && teamState.hasBed) {
            teamState.hasBed = false;

            var bed = this.game.map.getTeamRegions(teamState.team).bed();

            var world = this.game.world;
            for (BlockPos pos : bed) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.SKIP_DROPS);
            }

            this.game.triggerModifiers(BwGameTriggers.BED_BROKEN);

            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private Bed findBed(BlockPos pos) {
        var teams = this.game.config.teams();
        for (var team : teams.map().keySet()) {
            BwMap.TeamRegions teamRegions = this.game.map.getTeamRegions(team);
            BlockBounds bed = teamRegions.bed();
            if (bed != null && bed.contains(pos)) {
                return new Bed(team, bed);
            }
        }
        return null;
    }

    private static class Bed {
        final GameTeam team;
        final BlockBounds bounds;

        Bed(GameTeam team, BlockBounds bounds) {
            this.team = team;
            this.bounds = bounds;
        }
    }
}
