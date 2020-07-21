package net.gegy1000.bedwars.game;

import com.google.common.collect.Multimap;
import net.gegy1000.bedwars.BedWars;
import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.gl.game.Game;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.JoinResult;
import net.gegy1000.gl.game.StartResult;
import net.gegy1000.gl.game.config.PlayerConfig;
import net.gegy1000.gl.game.event.OfferPlayerListener;
import net.gegy1000.gl.game.event.PlayerAddListener;
import net.gegy1000.gl.game.event.PlayerDeathListener;
import net.gegy1000.gl.game.event.RequestStartListener;
import net.gegy1000.gl.game.event.UseItemListener;
import net.gegy1000.gl.game.player.TeamAllocator;
import net.gegy1000.gl.game.rule.GameRule;
import net.gegy1000.gl.game.rule.RuleResult;
import net.gegy1000.gl.item.CustomItem;
import net.gegy1000.gl.util.ColoredBlocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

public final class BwWaiting {
    private static final String TEAM_KEY = BedWars.ID + ":team";

    private final BwMap map;
    private final BwConfig config;

    private final Map<UUID, GameTeam> requestedTeams = new HashMap<>();

    private BwWaiting(BwMap map, BwConfig config) {
        this.map = map;
        this.config = config;
    }

    public static Game build(BwMap map, BwConfig config) {
        BwWaiting waiting = new BwWaiting(map, config);

        Game.Builder builder = Game.builder();
        builder.setMap(map.asInner());

        builder.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
        builder.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);

        builder.on(RequestStartListener.EVENT, waiting::requestStart);
        builder.on(OfferPlayerListener.EVENT, waiting::offerPlayer);

        builder.on(PlayerAddListener.EVENT, waiting::addPlayer);
        builder.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);

        builder.on(UseItemListener.EVENT, waiting::onUseItem);

        return builder.build();
    }

    private JoinResult offerPlayer(Game game, ServerPlayerEntity player) {
        if (game.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers()) {
            return JoinResult.gameFull();
        }

        return JoinResult.ok();
    }

    private StartResult requestStart(Game game) {
        PlayerConfig playerConfig = this.config.getPlayerConfig();
        if (game.getPlayerCount() < playerConfig.getMinPlayers()) {
            return StartResult.notEnoughPlayers();
        }

        Multimap<GameTeam, ServerPlayerEntity> players = this.allocatePlayers(game);
        Game active = BwActive.open(this.map, this.config, players);
        return StartResult.ok(active);
    }

    private void addPlayer(Game game, ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private boolean onPlayerDeath(Game game, ServerPlayerEntity player, DamageSource source) {
        this.spawnPlayer(player);
        return true;
    }

    private TypedActionResult<ItemStack> onUseItem(Game game, ServerPlayerEntity player, Hand hand) {
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
        BedWars.resetPlayer(player, GameMode.ADVENTURE);
        this.map.spawnAtCenter(player);

        List<GameTeam> teams = this.config.getTeams();
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

    private Multimap<GameTeam, ServerPlayerEntity> allocatePlayers(Game game) {
        TeamAllocator<GameTeam, ServerPlayerEntity> allocator = new TeamAllocator<>(this.config.getTeams());

        ServerWorld world = game.getWorld();

        for (UUID uuid : game.getPlayers()) {
            ServerPlayerEntity player = (ServerPlayerEntity) world.getPlayerByUuid(uuid);
            if (player != null) {
                GameTeam requestedTeam = this.requestedTeams.get(uuid);
                allocator.add(player, requestedTeam);
            }
        }

        return allocator.build();
    }
}
