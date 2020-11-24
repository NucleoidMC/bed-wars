package xyz.nucleoid.bedwars.game;

import com.google.common.collect.Multimap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.TeamAllocator;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BwWaiting {
    private static final String TEAM_KEY = BedWars.ID + ":team";

    private final GameSpace gameSpace;
    private final BwMap map;
    private final BwConfig config;

    private final BwSpawnLogic spawnLogic;

    private final Map<UUID, GameTeam> requestedTeams = new HashMap<>();

    private BwWaiting(GameSpace gameSpace, BwMap map, BwConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;

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
            BwWaiting waiting = new BwWaiting(game.getSpace(), map, config);

            GameWaitingLobby.applyTo(game, config.players);

            game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);

            game.on(RequestStartListener.EVENT, waiting::requestStart);

            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
            game.on(PlayerDamageListener.EVENT, waiting::onPlayerDamage);

            game.on(UseItemListener.EVENT, waiting::onUseItem);
        });
    }

    private StartResult requestStart() {
        Multimap<GameTeam, ServerPlayerEntity> players = this.allocatePlayers();
        BwActive.open(this.gameSpace, this.map, this.config, players);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        if (amount > 1.0F) {
            this.spawnPlayer(player);
        }
        return ActionResult.FAIL;
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

            player.inventory.setStack(i, selectorStack);
        }
    }

    private Multimap<GameTeam, ServerPlayerEntity> allocatePlayers() {
        TeamAllocator<GameTeam, ServerPlayerEntity> allocator = new TeamAllocator<>(this.config.teams);

        for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
            GameTeam requestedTeam = this.requestedTeams.get(player.getUuid());
            allocator.add(player, requestedTeam);
        }

        return allocator.build();
    }
}
