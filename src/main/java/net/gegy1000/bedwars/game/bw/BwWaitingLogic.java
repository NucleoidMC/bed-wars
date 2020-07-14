package net.gegy1000.bedwars.game.bw;

import com.mojang.datafixers.util.Either;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.custom.CustomItems;
import net.gegy1000.bedwars.game.GameTeam;
import net.gegy1000.bedwars.game.JoinResult;
import net.gegy1000.bedwars.game.StartResult;
import net.gegy1000.bedwars.game.config.PlayerConfig;
import net.gegy1000.bedwars.util.ColoredBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.List;

public final class BwWaitingLogic {
    private static final String TEAM_KEY = BedWarsMod.ID + ":team";

    private final BedWars game;
    private final WaitingPlayers players = new WaitingPlayers();

    public BwWaitingLogic(BedWars game) {
        this.game = game;
    }

    public JoinResult offerPlayer(ServerPlayerEntity player) {
        PlayerConfig playerConfig = this.game.config.getPlayerConfig();

        if (this.players.size() >= playerConfig.getMaxPlayers()) {
            return JoinResult.GAME_FULL;
        }

        if (this.players.addPlayer(player)) {
            this.spawnPlayer(player);

            return JoinResult.OK;
        } else {
            return JoinResult.ALREADY_JOINED;
        }
    }

    public void onUseRequestTeam(ServerPlayerEntity player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String teamKey = tag.getString(TEAM_KEY);

        GameTeam team = this.game.config.getTeam(teamKey);
        if (team != null) {
            this.players.requestTeam(player, team);

            Text message = new LiteralText("You have requested to join the ")
                    .append(new LiteralText(team.getDisplay() + " Team").formatted(team.getFormatting()));

            player.sendMessage(message, false);
        }
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.game.joinPlayerToMap(player);

        player.setGameMode(GameMode.ADVENTURE);
        this.game.playerLogic.spawnAtCenter(player);

        List<GameTeam> teams = this.game.config.getTeams();
        for (int i = 0; i < teams.size(); i++) {
            GameTeam team = teams.get(i);

            Text selectorName = new LiteralText("Request " + team.getDisplay() + " Team")
                    .formatted(Formatting.BOLD, team.getFormatting());

            ItemStack selectorStack = new ItemStack(ColoredBlocks.wool(team.getDye()));
            selectorStack.setCustomName(selectorName);

            selectorStack.getOrCreateTag().putString(TEAM_KEY, team.getKey());

            player.inventory.setStack(i, CustomItems.TEAM_SELECTOR.applyTo(selectorStack));
        }
    }

    public Either<BwState, StartResult> tryStart() {
        PlayerConfig playerConfig = this.game.config.getPlayerConfig();
        if (this.players.size() < playerConfig.getMinPlayers()) {
            return Either.right(StartResult.NOT_ENOUGH_PLAYERS);
        }

        BwState state = BwState.start(this.players, this.game.config);
        return Either.left(state);
    }
}
