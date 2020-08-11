package xyz.nucleoid.bedwars.game;

import com.google.common.collect.Multimap;
import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.custom.BwCustomItems;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapBuilder;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.TeamAllocator;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorldConfig;
import xyz.nucleoid.plasmid.item.CustomItem;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// TODO: there's a lot of common logic in waiting lobbies that can be extracted generically
public final class BwWaiting {
    private static final String TEAM_KEY = BedWars.ID + ":team";

    private final GameWorld gameWorld;
    private final BwMap map;
    private final BwConfig config;

    private final BwSpawnLogic spawnLogic;

    private final Map<UUID, GameTeam> requestedTeams = new HashMap<>();

    private BwWaiting(GameWorld gameWorld, BwMap map, BwConfig config) {
        this.gameWorld = gameWorld;
        this.map = map;
        this.config = config;

        this.spawnLogic = new BwSpawnLogic(gameWorld.getWorld(), map);
    }

    public static CompletableFuture<Void> open(MinecraftServer server, BwConfig config) {
        BwSkyMapBuilder mapBuilder = new BwSkyMapBuilder(config);

        return mapBuilder.create().thenAccept(map -> {
            BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                    .setGenerator(map.getChunkGenerator())
                    .setDefaultGameMode(GameMode.SPECTATOR);

            GameWorld gameWorld = GameWorld.open(server, worldConfig);
            BwWaiting waiting = new BwWaiting(gameWorld, map, config);

            gameWorld.newGame(game -> {
                game.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
                game.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
                game.setRule(GameRule.ENABLE_HUNGER, RuleResult.DENY);
                game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);

                game.on(RequestStartListener.EVENT, waiting::requestStart);
                game.on(OfferPlayerListener.EVENT, waiting::offerPlayer);

                game.on(PlayerAddListener.EVENT, waiting::addPlayer);
                game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);

                game.on(UseItemListener.EVENT, waiting::onUseItem);
            });
        });
    }

    private JoinResult offerPlayer(ServerPlayerEntity player) {
        if (this.gameWorld.getPlayerCount() >= this.config.players.getMaxPlayers()) {
            return JoinResult.gameFull();
        }

        return JoinResult.ok();
    }

    private StartResult requestStart() {
        if (this.gameWorld.getPlayerCount() < this.config.players.getMinPlayers()) {
            return StartResult.notEnoughPlayers();
        }

        Multimap<GameTeam, ServerPlayerEntity> players = this.allocatePlayers();
        BwActive.open(this.gameWorld, this.map, this.config, players);
        return StartResult.ok();
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private boolean onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnPlayer(player);
        return true;
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (CustomItem.match(stack) == BwCustomItems.TEAM_SELECTOR) {
            CompoundTag tag = stack.getOrCreateTag();
            String teamKey = tag.getString(TEAM_KEY);

            GameTeam team = this.config.getTeam(teamKey);
            if (team != null) {
                this.requestedTeams.put(player.getUuid(), team);

                Text message = new LiteralText("You have requested to join the ")
                        .append(new LiteralText(team.getDisplay() + " Team").formatted(team.getFormatting()));

                player.sendMessage(message, false);

                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnAtCenter(player);

        List<GameTeam> teams = this.config.teams;
        for (int i = 0; i < teams.size(); i++) {
            GameTeam team = teams.get(i);

            Text selectorName = new LiteralText("Request " + team.getDisplay() + " Team")
                    .formatted(Formatting.BOLD, team.getFormatting());

            ItemStack selectorStack = new ItemStack(ColoredBlocks.wool(team.getDye()));
            selectorStack.setCustomName(selectorName);

            selectorStack.getOrCreateTag().putString(TEAM_KEY, team.getKey());

            player.inventory.setStack(i, BwCustomItems.TEAM_SELECTOR.applyTo(selectorStack));
        }
    }

    private Multimap<GameTeam, ServerPlayerEntity> allocatePlayers() {
        TeamAllocator<GameTeam, ServerPlayerEntity> allocator = new TeamAllocator<>(this.config.teams);

        for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
            GameTeam requestedTeam = this.requestedTeams.get(player.getUuid());
            allocator.add(player, requestedTeam);
        }

        return allocator.build();
    }
}
