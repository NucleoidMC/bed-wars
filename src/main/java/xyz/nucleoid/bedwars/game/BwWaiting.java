package xyz.nucleoid.bedwars.game;

import com.google.common.collect.Multimap;
import fr.catcore.server.translations.api.LocalizableText;
import fr.catcore.server.translations.api.LocalizationTarget;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.TeamAllocator;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.world.bubble.BubbleWorldConfig;

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

    public static CompletableFuture<GameWorld> open(GameOpenContext<BwConfig> context) {
        BwConfig config = context.getConfig();
        BwMapBuilder mapBuilder = new BwMapBuilder(config);

        return mapBuilder.create(context.getServer()).thenCompose(map -> {
            BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                    .setGenerator(map.getChunkGenerator())
                    .setDimensionType(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, config.dimension))
                    .setDefaultGameMode(GameMode.SPECTATOR);

            return context.openWorld(worldConfig).thenApply(gameWorld -> {
                BwWaiting waiting = new BwWaiting(gameWorld, map, config);

                return GameWaitingLobby.open(gameWorld, config.players, game -> {
                    game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);

                    game.on(RequestStartListener.EVENT, waiting::requestStart);

                    game.on(PlayerAddListener.EVENT, waiting::addPlayer);
                    game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
                    game.on(PlayerDamageListener.EVENT, waiting::onPlayerDamage);

                    game.on(UseItemListener.EVENT, waiting::onUseItem);
                });
            });
        });
    }

    private StartResult requestStart() {
        Multimap<GameTeam, ServerPlayerEntity> players = this.allocatePlayers();
        BwActive.open(this.gameWorld, this.map, this.config, players);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private boolean onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (amount > 1.0F) {
            this.spawnPlayer(player);
        }
        return true;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem().isIn(ItemTags.WOOL)) {
            CompoundTag tag = stack.getOrCreateTag();
            String teamKey = tag.getString(TEAM_KEY);

            GameTeam team = this.config.getTeam(teamKey);
            if (team != null) {
                this.requestedTeams.put(player.getUuid(), team);

                Text message = new TranslatableText("text.bedwars.team.join", new LiteralText(team.getDisplay()).formatted(team.getFormatting()));

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

            Text selectorName = new TranslatableText("text.bedwars.team.join.item", team.getDisplay())
                    .formatted(Formatting.BOLD, team.getFormatting());

            ItemStack selectorStack = new ItemStack(ColoredBlocks.wool(team.getDye()));
            selectorStack.setCustomName(LocalizableText.asLocalizedFor(selectorName, (LocalizationTarget) player));

            selectorStack.getOrCreateTag().putString(TEAM_KEY, team.getKey());

            player.inventory.setStack(i, selectorStack);
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
