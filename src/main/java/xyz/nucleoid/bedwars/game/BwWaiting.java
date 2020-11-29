package xyz.nucleoid.bedwars.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.TeamSelectionLobby;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public final class BwWaiting {
    private final GameSpace gameSpace;
    private final BwMap map;
    private final BwConfig config;

    private final BwSpawnLogic spawnLogic;

    private final TeamSelectionLobby teamSelection;

    private BwWaiting(GameSpace gameSpace, BwMap map, BwConfig config, TeamSelectionLobby teamSelection) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.teamSelection = teamSelection;

        this.spawnLogic = new BwSpawnLogic(gameSpace.getWorld(), map);
    }

    public static GameOpenProcedure open(GameOpenContext<BwConfig> context) {
        BwConfig config = context.getConfig();
        BwMap map = new BwMapBuilder(config)
                .create(context.getServer());

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.getChunkGenerator())
                .setDimensionType(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, config.dimension))
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            GameWaitingLobby.applyTo(game, config.players);

            TeamSelectionLobby teamSelection = TeamSelectionLobby.applyTo(game, config.teams);
            BwWaiting waiting = new BwWaiting(game.getSpace(), map, config, teamSelection);

            game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);

            game.on(RequestStartListener.EVENT, waiting::requestStart);

            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
            game.on(PlayerDamageListener.EVENT, waiting::onPlayerDamage);
        });
    }

    private StartResult requestStart() {
        Multimap<GameTeam, ServerPlayerEntity> players = HashMultimap.create();
        this.teamSelection.allocate(players::put);

        BwActive.open(this.gameSpace, this.map, this.config, players);

        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnAtCenter(player);
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnLogic.respawnPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnAtCenter(player);
        return ActionResult.FAIL;
    }
}
