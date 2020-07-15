package net.gegy1000.gl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.gegy1000.gl.GameLib;
import net.gegy1000.gl.game.ConfiguredGame;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.game.JoinResult;
import net.gegy1000.gl.game.StartResult;
import net.gegy1000.gl.game.config.GameConfigs;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class GameCommand {
    public static final DynamicCommandExceptionType GAME_NOT_FOUND = new DynamicCommandExceptionType(arg -> {
        return new TranslatableText("Game with id '%s' was not found!", arg);
    });

    public static final SimpleCommandExceptionType ALREADY_OPEN = new SimpleCommandExceptionType(
            new LiteralText("A game is already open!")
    );

    public static final SimpleCommandExceptionType NOT_RECRUITING = new SimpleCommandExceptionType(
            new LiteralText("Game not recruiting!")
    );

    public static final SimpleCommandExceptionType GAME_FULL = new SimpleCommandExceptionType(
            new LiteralText("Game full! :(")
    );

    public static final SimpleCommandExceptionType ALREADY_JOINED = new SimpleCommandExceptionType(
            new LiteralText("Already joined game!")
    );

    public static final SimpleCommandExceptionType NOT_ENOUGH_PLAYERS = new SimpleCommandExceptionType(
            new LiteralText("Game does not have enough players yet!")
    );

    public static final SimpleCommandExceptionType ALREADY_ACTIVE = new SimpleCommandExceptionType(
            new LiteralText("Game is already running!")
    );

    public static final SimpleCommandExceptionType NOT_ACTIVE = new SimpleCommandExceptionType(
            new LiteralText("Game not currently running!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .then(argument("game_type", IdentifierArgumentType.identifier()).suggests(gameSuggestions())
                    .executes(GameCommand::openGame)
                ))
                .then(literal("join").executes(GameCommand::joinGame))
                .then(literal("start").executes(GameCommand::startGame))
                .then(literal("stop").executes(GameCommand::stopGame))
                .then(literal("list").executes(GameCommand::listGames))
        );
    }
    // @formatter:on

    private static int openGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getMinecraftServer();

        Identifier gameTypeId = IdentifierArgumentType.getIdentifier(context, "game_type");

        ConfiguredGame<?, ?> game = GameConfigs.get(gameTypeId);
        if (game == null) {
            throw GAME_NOT_FOUND.create(gameTypeId);
        }

        Optional<GameManager.Inactive> inactive = GameManager.inactive();
        if (!inactive.isPresent()) {
            throw ALREADY_OPEN.create();
        }

        PlayerManager playerManager = server.getPlayerManager();
        playerManager.broadcastChatMessage(new LiteralText(game.getName() + " is opening! Hold tight.."), MessageType.SYSTEM, Util.NIL_UUID);

        inactive.get().open(server, game)
                .handle((gameAndConfig, throwable) -> {
                    if (throwable == null) {
                        onOpenSuccess(game, playerManager);
                    } else {
                        onOpenError(playerManager, throwable);
                    }
                    return null;
                });

        return Command.SINGLE_SUCCESS;
    }

    private static void onOpenSuccess(ConfiguredGame<?, ?> game, PlayerManager playerManager) {
        ClickEvent joinClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game join");
        HoverEvent joinHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("/game join"));
        Style joinStyle = Style.EMPTY
                .withFormatting(Formatting.UNDERLINE)
                .withColor(Formatting.BLUE)
                .withClickEvent(joinClick)
                .setHoverEvent(joinHover);

        Text openMessage = new LiteralText(game.getName() + " has opened! ")
                .append(new LiteralText("Click here to join").setStyle(joinStyle));
        playerManager.broadcastChatMessage(openMessage, MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static void onOpenError(PlayerManager playerManager, Throwable throwable) {
        GameLib.LOGGER.error("Failed to start game", throwable);
        playerManager.broadcastChatMessage(new LiteralText("An exception occurred while trying to start game"), MessageType.SYSTEM, Util.NIL_UUID);
    }

    private static int joinGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();

        Optional<GameManager.Open<?>> openOpt = GameManager.open();
        if (!openOpt.isPresent()) {
            throw NOT_RECRUITING.create();
        }

        JoinResult joinResult = openOpt.get().offerPlayer(player);
        if (joinResult == JoinResult.GAME_FULL) {
            throw GAME_FULL.create();
        }

        if (joinResult == JoinResult.ALREADY_JOINED) {
            throw ALREADY_JOINED.create();
        }

        Text joinMessage = player.getDisplayName().shallowCopy()
                .append(" has joined the game lobby!")
                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
        playerManager.broadcastChatMessage(joinMessage, MessageType.SYSTEM, Util.NIL_UUID);

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        Optional<GameManager.Open<?>> openOpt = GameManager.open();
        if (!openOpt.isPresent()) {
            throw NOT_RECRUITING.create();
        }

        GameManager.Open<?> open = openOpt.get();

        StartResult result = open.requestStart();
        if (result == StartResult.NOT_ENOUGH_PLAYERS) {
            throw NOT_ENOUGH_PLAYERS.create();
        } else if (result == StartResult.ALREADY_STARTED) {
            throw ALREADY_ACTIVE.create();
        }

        MinecraftServer server = source.getMinecraftServer();
        PlayerManager playerManager = server.getPlayerManager();
        playerManager.broadcastChatMessage(new LiteralText(open.getName() + " is starting!"), MessageType.SYSTEM, Util.NIL_UUID);

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Optional<GameManager.Open<?>> activeOpt = GameManager.open();
        if (!activeOpt.isPresent()) {
            throw NOT_ACTIVE.create();
        }

        GameManager.Open<?> open = activeOpt.get();

        ServerCommandSource source = context.getSource();

        open.stop().handle((game, throwable) -> {
            if (throwable == null) {
                MinecraftServer server = source.getMinecraftServer();

                LiteralText message = new LiteralText(game.getConfigured().getName() + " has been stopped");
                server.getPlayerManager().broadcastChatMessage(message, MessageType.SYSTEM, Util.NIL_UUID);
            } else {
                source.sendError(new LiteralText("Failed to stop game: " + throwable.getClass()));
                GameLib.LOGGER.error("Failed to stop game", throwable);
            }

            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int listGames(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText("Registered games:").formatted(Formatting.BOLD), false);
        for (Identifier id : GameConfigs.getKeys()) {
            String command = "/game open " + id;

            ClickEvent linkClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
            HoverEvent linkHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(command));
            Style linkStyle = Style.EMPTY
                    .withFormatting(Formatting.UNDERLINE)
                    .withColor(Formatting.BLUE)
                    .withClickEvent(linkClick)
                    .setHoverEvent(linkHover);

            MutableText link = new LiteralText(id.toString()).setStyle(linkStyle);
            source.sendFeedback(new LiteralText(" - ").append(link), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static SuggestionProvider<ServerCommandSource> gameSuggestions() {
        return (ctx, builder) -> {
            return CommandSource.suggestMatching(
                    GameConfigs.getKeys().stream().map(Identifier::toString),
                    builder
            );
        };
    }
}
