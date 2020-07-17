package net.gegy1000.bedwars.game.active;

import com.google.common.collect.Multimap;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.BwConfig;
import net.gegy1000.bedwars.game.BwMap;
import net.gegy1000.bedwars.game.BwPhase;
import net.gegy1000.bedwars.game.BwPlayerTracker;
import net.gegy1000.bedwars.game.PlayerQueue;
import net.gegy1000.bedwars.game.active.modifiers.BwGameTriggers;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.JoinResult;
import net.gegy1000.gl.game.modifier.GameModifier;
import net.gegy1000.gl.game.modifier.GameTrigger;
import net.gegy1000.gl.logic.combat.OldCombat;
import net.gegy1000.gl.util.ItemUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BwActive implements BwPhase {
    public static final long RESPAWN_TICKS = 20 * 5;

    public final BwMap map;
    public final BwPlayerTracker playerTracker;
    public final BwConfig config;

    private final Map<UUID, BwParticipant> participants = new HashMap<>();
    private final Map<GameTeam, TeamState> teams = new HashMap<>();

    public final BwScoreboard scoreboard;
    public final BwBroadcast broadcast;
    public final BwTeamLogic teamLogic;
    public final BwKillLogic killLogic;
    public final BwWinStateLogic winStateLogic;
    public final BwMapLogic mapLogic;
    public final BwPlayerLogic playerLogic;

    private long lastWinCheck;

    private BwActive(
            BwMap map,
            BwConfig config,
            BwPlayerTracker playerTracker
    ) {
        this.map = map;
        this.playerTracker = playerTracker;
        this.config = config;

        this.scoreboard = BwScoreboard.create(this);
        for (GameTeam team : config.getTeams()) {
            this.scoreboard.addTeam(team);
        }

        this.broadcast = new BwBroadcast(this);
        this.teamLogic = new BwTeamLogic(this);
        this.killLogic = new BwKillLogic(this);
        this.winStateLogic = new BwWinStateLogic(this);
        this.mapLogic = new BwMapLogic(this);
        this.playerLogic = new BwPlayerLogic(this);
    }

    static BwActive open(BwMap map, BwConfig config, BwPlayerTracker playerTracker, PlayerQueue playerQueue) {
        BwActive active = new BwActive(map, config, playerTracker);
        active.allocateTeams(config, playerQueue);

        return active;
    }

    private void allocateTeams(BwConfig config, PlayerQueue playerQueue) {
        Multimap<GameTeam, ServerPlayerEntity> allocated = playerQueue.allocatePlayers(config.getTeams());
        allocated.forEach((team, player) -> {
            this.participants.put(player.getUuid(), new BwParticipant(this, player, team));
        });

        for (GameTeam team : config.getTeams()) {
            List<BwParticipant> participants = this.participantsFor(team).collect(Collectors.toList());

            if (!participants.isEmpty()) {
                TeamState teamState = new TeamState(team);
                participants.forEach(participant -> {
                    teamState.players.add(participant.playerId);
                });

                this.teams.put(team, teamState);
            }
        }
    }

    @Override
    public void start() {
        this.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) {
                return;
            }

            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.spawnPlayer(player, spawn);
            } else {
                BedWarsMod.LOGGER.warn("No spawn for player {}", participant.playerId);
                this.playerTracker.spawnAtCenter(player, GameMode.SPECTATOR);
            }
        });

        this.map.spawnShopkeepers(this.config);
        this.triggerModifiers(BwGameTriggers.GAME_RUNNING);
    }

    @Override
    public JoinResult offerPlayer(ServerPlayerEntity player) {
        this.playerTracker.joinPlayer(player);
        this.playerTracker.spawnAtCenter(player, GameMode.SPECTATOR);

        return JoinResult.OK;
    }

    @Override
    public void stop() {
        this.scoreboard.close();
    }

    @Nullable
    public BwWinStateLogic.WinResult tick() {
        ServerWorld world = this.map.getWorld();
        long time = world.getTime();

        if (time - this.lastWinCheck > 20) {
            BwWinStateLogic.WinResult winResult = this.winStateLogic.checkWinResult();
            if (winResult != null) {
                this.broadcast.broadcastGameOver(winResult);
                return winResult;
            }

            this.lastWinCheck = time;
        }

        this.mapLogic.tick();

        this.scoreboard.tick();
        this.playerLogic.tick();

        // Tick modifiers
        for (GameModifier modifier : this.config.getModifiers()) {
            if (modifier.getTrigger().tickable) {
                modifier.tick(this);
            }
        }

        return null;
    }

    public ItemStack createArmor(ItemStack stack) {
        return ItemUtil.unbreakable(stack);
    }

    public ItemStack createTool(ItemStack stack) {
        stack = ItemUtil.unbreakable(stack);
        if (this.config.getCombatConfig().isOldMechanics()) {
            stack = OldCombat.applyTo(stack);
        }

        return stack;
    }

    public void triggerModifiers(GameTrigger type) {
        for (GameModifier modifier : this.config.getModifiers()) {
            if (modifier.getTrigger() == type) {
                modifier.init(this);
            }
        }
    }

    @Nullable
    public BwParticipant getParticipant(PlayerEntity player) {
        return this.participants.get(player.getUuid());
    }

    @Nullable
    public GameTeam getTeam(UUID id) {
        BwParticipant participant = this.participants.get(id);
        if (participant != null) {
            return participant.team;
        }
        return null;
    }

    @Nullable
    public BwParticipant getParticipant(UUID id) {
        return this.participants.get(id);
    }

    public boolean isParticipant(PlayerEntity player) {
        return this.participants.containsKey(player.getUuid());
    }

    public Stream<BwParticipant> participantsFor(GameTeam team) {
        return this.participants.values().stream().filter(participant -> participant.team == team);
    }

    public Stream<BwParticipant> participants() {
        return this.participants.values().stream();
    }

    public Stream<ServerPlayerEntity> players() {
        return this.participants().map(BwParticipant::player).filter(Objects::nonNull);
    }

    public Stream<TeamState> teams() {
        return this.teams.values().stream();
    }

    public int getTeamCount() {
        return this.teams.size();
    }

    @Nullable
    public TeamState getTeam(GameTeam team) {
        return this.teams.get(team);
    }

    public static class TeamState {
        public static final int MAX_SHARPNESS = 3;
        public static final int MAX_PROTECTION = 3;

        final Set<UUID> players = new HashSet<>();
        final GameTeam team;
        boolean hasBed = true;
        boolean eliminated;

        public boolean trapSet;
        public boolean healPool;
        public boolean hasteEnabled;
        public int swordSharpness;
        public int armorProtection;

        TeamState(GameTeam team) {
            this.team = team;
        }
    }
}
