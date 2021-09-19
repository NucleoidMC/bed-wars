package xyz.nucleoid.bedwars.game.active;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.custom.MovingCloud;
import xyz.nucleoid.bedwars.game.BwConfig;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.BwSpawnLogic;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.bedwars.game.active.modifiers.GameModifier;
import xyz.nucleoid.bedwars.game.active.modifiers.GameTrigger;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.OldCombat;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.game.common.team.TeamChat;
import xyz.nucleoid.plasmid.game.common.team.TeamManager;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;
import xyz.nucleoid.stimuli.event.world.ExplosionDetonatedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public final class BwActive {
    public static final int RESPAWN_TIME_SECONDS = 5;
    public static final long RESPAWN_TICKS = 20 * RESPAWN_TIME_SECONDS;
    public static final long CLOSE_TICKS = 10 * 20;

    public final ServerWorld world;
    public final GameSpace gameSpace;

    public final BwMap map;
    public final BwConfig config;

    private final Map<PlayerRef, BwParticipant> participants = new Object2ObjectOpenHashMap<>();
    private final Map<GameTeamKey, TeamState> teamStates = new Reference2ObjectOpenHashMap<>();

    private final TeamManager teams;

    public final BwSidebar sidebar;
    public final BwBroadcast broadcast;
    public final BwTeamLogic teamLogic;
    public final BwKillLogic killLogic;
    public final BwWinStateLogic winStateLogic;
    public final BwMapLogic mapLogic;
    public final BwPlayerLogic playerLogic;
    public final BwSpawnLogic spawnLogic;
    private final BwBedDestruction bedDestruction;
    private final BwInteractions interactions;

    private long startTime;

    private long lastWinCheck;

    private GameTeam winningTeam;
    private long closeTime;

    final List<MovingCloud> movingClouds = new ArrayList<>();

    private BwActive(ServerWorld world, GameActivity activity, BwMap map, BwConfig config, TeamManager teams, GlobalWidgets widgets) {
        this.world = world;
        this.gameSpace = activity.getGameSpace();

        this.map = map;
        this.config = config;

        this.teams = teams;

        this.sidebar = BwSidebar.create(this, widgets);

        this.broadcast = new BwBroadcast(this);
        this.teamLogic = new BwTeamLogic(this);
        this.killLogic = new BwKillLogic(this);
        this.winStateLogic = new BwWinStateLogic(this);
        this.mapLogic = new BwMapLogic(this);
        this.playerLogic = new BwPlayerLogic(this);
        this.spawnLogic = new BwSpawnLogic(this.world, map);
        this.bedDestruction = new BwBedDestruction(widgets);
        this.interactions = new BwInteractions(this);
    }

    public static void open(ServerWorld world, GameSpace gameSpace, BwMap map, BwConfig config, Multimap<GameTeamKey, ServerPlayerEntity> players) {
        gameSpace.setActivity(activity -> {
            TeamManager teamManager = TeamManager.addTo(activity);
            GlobalWidgets widgets = GlobalWidgets.addTo(activity);

            TeamChat.addTo(activity, teamManager);

            BwActive active = new BwActive(world, activity, map, config, teamManager, widgets);
            active.addTeams(config.teams());
            active.addPlayers(players);

            activity.deny(GameRuleType.PORTALS);
            activity.allow(GameRuleType.PVP);
            activity.allow(GameRuleType.UNSTABLE_TNT);
            activity.deny(GameRuleType.CRAFTING);
            activity.allow(GameRuleType.FALL_DAMAGE);
            activity.deny(GameRuleType.HUNGER);
            activity.allow(GameRuleType.TRIDENTS_LOYAL_IN_VOID);
            activity.allow(BedWars.BLAST_PROOF_GLASS_RULE);
            activity.allow(BedWars.LEAVES_DROP_GOLDEN_APPLES);
            activity.allow(BedWars.FAST_TREE_GROWTH);

            activity.listen(GameActivityEvents.ENABLE, active::onEnable);
            activity.listen(GamePlayerEvents.OFFER, active::offerPlayer);
            activity.listen(GameActivityEvents.TICK, active::tick);

            activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
            activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);

            active.interactions.addTo(activity);

            activity.listen(ExplosionDetonatedEvent.EVENT, (explosion, particles) -> {
                explosion.getAffectedBlocks().removeIf(map::isProtectedBlock);
            });
        });
    }

    private void addTeams(GameTeamList teams) {
        for (var team : teams) {
            var config = GameTeamConfig.builder(team.config())
                    .setCollision(AbstractTeam.CollisionRule.NEVER)
                    .setFriendlyFire(false)
                    .build();

            var newTeam = new GameTeam(team.key(), config);
            this.teams.addTeam(newTeam);

            this.teamStates.put(team.key(), new TeamState(newTeam));
        }
    }

    private void addPlayers(Multimap<GameTeamKey, ServerPlayerEntity> players) {
        players.forEach((teamKey, player) -> {
            var teamConfig = this.teams.getTeamConfig(teamKey);
            var participant = new BwParticipant(this, player, new GameTeam(teamKey, teamConfig));
            this.participants.put(participant.ref, participant);

            this.teams.addPlayerTo(player, teamKey);
        });
    }

    private void onEnable() {
        this.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) {
                return;
            }

            this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);

            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.spawnPlayer(player, spawn);
            } else {
                BedWars.LOGGER.warn("No spawn for player {}", participant.ref);
                this.spawnLogic.spawnAtCenter(player);
            }
        });

        this.map.spawnShopkeepers(this.world, this, this.config);
        this.triggerModifiers(BwGameTriggers.GAME_RUNNING);

        this.startTime = this.world.getTime();
    }

    private PlayerOfferResult offerPlayer(PlayerOffer offer) {
        var player = offer.player();

        return offer.accept(this.world, this.map.getCenterSpawn())
                .and(() -> {
                    this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
                    BwParticipant participant = this.participantBy(player);
                    if (participant != null) {
                        this.rejoinParticipant(player, participant);
                    }
                });
    }

    private void rejoinParticipant(ServerPlayerEntity player, BwParticipant participant) {
        BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
        if (spawn != null) {
            this.playerLogic.startRespawning(player, spawn);
        }
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity attackedPlayer, DamageSource source, float amount) {
        if (source == DamageSource.OUT_OF_WORLD && attackedPlayer.isSpectator()) {
            return ActionResult.FAIL;
        }

        BwParticipant attackedParticipant = this.participantBy(attackedPlayer);
        if (attackedParticipant == null) {
            return ActionResult.PASS;
        }

        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity attackerPlayer) {
            BwParticipant attackerParticipant = this.participantBy(attackerPlayer);

            if (attackerParticipant != null) {
                if (attackerParticipant.team == attackedParticipant.team) {
                    return ActionResult.FAIL;
                }

                attackedParticipant.lastAttack = AttackRecord.fromAttacker(attackerPlayer);
            }
        }

        return ActionResult.PASS;
    }

    private void tick() {
        if (this.winningTeam != null) {
            if (this.tickClosing()) {
                this.gameSpace.close(GameCloseReason.FINISHED);
            }
            return;
        }

        long time = this.world.getTime();

        PlayerSet players = this.gameSpace.getPlayers();

        if (time % 20 == 0) {
            long gameTime = time - this.startTime;

            if (this.bedDestruction.update(gameTime)) {
                for (GameTeam team : this.teams) {
                    this.teamLogic.removeBed(team.key());
                }

                players.sendMessage(new TranslatableText("text.bedwars.all_beds_destroyed").formatted(Formatting.RED));
                players.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN);
            }

            for (ServerPlayerEntity player : players) {
                if (!player.isSpectator() && !this.map.isLegalAt(player.getBlockPos())) {
                    player.damage(DamageSource.OUT_OF_WORLD, 10000.0F);
                }
            }
        }

        this.movingClouds.removeIf(MovingCloud::tick);

        BwWinStateLogic.WinResult winResult = this.tickActive();
        if (winResult != null) {
            this.winningTeam = winResult.team();
            this.closeTime = this.world.getTime() + CLOSE_TICKS;
        }
    }

    @Nullable
    private BwWinStateLogic.WinResult tickActive() {
        long time = this.world.getTime();

        if (time - this.lastWinCheck > 20) {
            BwWinStateLogic.WinResult winResult = this.winStateLogic.checkWinResult();
            if (winResult != null) {
                this.broadcast.broadcastGameOver(winResult);
                return winResult;
            }

            this.lastWinCheck = time;
        }

        this.mapLogic.tick();

        this.sidebar.tick();
        this.playerLogic.tick();

        // Tick modifiers
        for (GameModifier modifier : this.config.modifiers()) {
            if (modifier.getTrigger().tickable) {
                modifier.tick(this);
            }
        }

        return null;
    }

    private boolean tickClosing() {
        if (this.winningTeam != null) {
            this.spawnFireworks(this.winningTeam);
        }

        return this.world.getTime() >= this.closeTime;
    }

    private void spawnFireworks(GameTeam team) {
        Random random = this.world.random;

        if (random.nextInt(18) == 0) {
            List<ServerPlayerEntity> players = Lists.newArrayList(this.players());
            ServerPlayerEntity player = players.get(random.nextInt(players.size()));

            int flight = random.nextInt(3);
            FireworkItem.Type type = random.nextInt(4) == 0 ? FireworkItem.Type.STAR : FireworkItem.Type.BURST;
            FireworkRocketEntity firework = new FireworkRocketEntity(
                    this.world,
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    team.config().createFirework(flight, type)
            );

            this.world.spawnEntity(firework);
        }
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        BwParticipant participant = this.participantBy(player);

        // TODO: cancel if cause is own player
        if (participant != null) {
            this.killLogic.onPlayerDeath(participant, player, source);
            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

    public ItemStack createArmor(ItemStack stack) {
        return ItemStackBuilder.of(stack).setUnbreakable().build();
    }

    public ItemStack createTool(ItemStack stack) {
        stack = ItemStackBuilder.of(stack).setUnbreakable().build();
        if (this.config.combat().oldMechanics()) {
            stack = OldCombat.applyTo(stack);
        }

        return stack;
    }

    public void triggerModifiers(GameTrigger type) {
        for (GameModifier modifier : this.config.modifiers()) {
            if (modifier.getTrigger() == type) {
                modifier.init(this);
            }
        }
    }

    @Nullable
    public BwParticipant participantBy(PlayerEntity player) {
        return this.participants.get(PlayerRef.of(player));
    }

    @Nullable
    public BwParticipant participantBy(PlayerRef player) {
        return this.participants.get(player);
    }

    @Nullable
    public GameTeam teamFor(PlayerRef player) {
        var participant = this.participants.get(player);
        return participant!= null ? participant.team : null;
    }

    public boolean isParticipant(PlayerEntity player) {
        return this.participants.containsKey(PlayerRef.of(player));
    }

    public Stream<BwParticipant> participantsFor(GameTeamKey team) {
        return this.participants.values().stream().filter(participant -> participant.team.key() == team);
    }

    public PlayerSet playersFor(GameTeamKey team) {
        return this.teams.playersIn(team);
    }

    public PlayerSet players() {
        return this.gameSpace.getPlayers();
    }

    public Stream<BwParticipant> participants() {
        return this.participants.values().stream();
    }

    public TeamManager teams() {
        return this.teams;
    }

    public Stream<TeamState> teamsStates() {
        return this.teamStates.values().stream();
    }

    public int getTeamCount() {
        return this.teamStates.size();
    }

    @Nullable
    public TeamState teamState(GameTeamKey team) {
        return this.teamStates.get(team);
    }

    public static class TeamState {
        public static final int MAX_SHARPNESS = 3;
        public static final int MAX_PROTECTION = 3;

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
