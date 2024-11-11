package xyz.nucleoid.bedwars.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.config.BwConfig;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.api.game.common.team.TeamSelectionLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public final class BwWaiting {
    private final ServerWorld world;
    private final GameSpace gameSpace;
    private final BwMap map;
    private final BwConfig config;

    private final BwSpawnLogic spawnLogic;

    private final TeamSelectionLobby teamSelection;

    private BwWaiting(ServerWorld world, GameSpace gameSpace, BwMap map, BwConfig config, TeamSelectionLobby teamSelection) {
        this.world = world;
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.teamSelection = teamSelection;

        this.spawnLogic = new BwSpawnLogic(world, map);
    }

    public static GameOpenProcedure open(GameOpenContext<BwConfig> context) {
        BwConfig config = context.config();
        BwMap map = new BwMapBuilder(config)
                .create(context.server());

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.getChunkGenerator())
                .setDimensionType(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, config.dimension()));

        return context.openWithWorld(worldConfig, (activity, world) -> {
            GameWaitingLobby.addTo(activity, config.players());

            TeamSelectionLobby teamSelection = TeamSelectionLobby.addTo(activity, config.teams());

            BwWaiting waiting = new BwWaiting(world, activity.getGameSpace(), map, config, teamSelection);

            activity.allow(GameRuleType.INTERACTION);

            activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);

            activity.listen(GamePlayerEvents.ACCEPT, waiting::onPlayerOffer);
            activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
            activity.listen(PlayerDamageEvent.EVENT, waiting::onPlayerDamage);
        });
    }

    private JoinAcceptorResult onPlayerOffer(JoinAcceptor offer) {
        return offer.teleport(this.world, this.map.getCenterSpawn())
                .thenRunForEach((player, intent) -> this.spawnLogic.respawnPlayer(player, GameMode.ADVENTURE));
    }

    private GameResult requestStart() {
        Multimap<GameTeamKey, ServerPlayerEntity> players = HashMultimap.create();
        this.teamSelection.allocate(this.gameSpace.getPlayers(), players::put);

        BwActive.open(this.world, this.gameSpace, this.map, this.config, players);

        return GameResult.ok();
    }

    private EventResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        return EventResult.DENY;
    }

    private EventResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnLogic.respawnPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnAtCenter(player);
        return EventResult.DENY;
    }
}
