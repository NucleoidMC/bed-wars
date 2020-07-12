package net.gegy1000.bedwars.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.ConfiguredGame;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.JoinResult;
import net.gegy1000.bedwars.game.config.GameConfigs;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
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

    public static final SimpleCommandExceptionType GAME_NOT_READY = new SimpleCommandExceptionType(
            new LiteralText("Game not ready to start yet!")
    );

    public static final SimpleCommandExceptionType NOT_ACTIVE = new SimpleCommandExceptionType(
            new LiteralText("Game not currently running!")
    );

    // @formatter:off
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("game")
                .then(literal("open")
                    .then(argument("game_type", IdentifierArgumentType.identifier())
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

        inactive.get().recruit(game);

        PlayerManager playerManager = server.getPlayerManager();

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

        return Command.SINGLE_SUCCESS;
    }

    private static int joinGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        PlayerManager playerManager = source.getMinecraftServer().getPlayerManager();

        Optional<GameManager.Recruiting<?>> recruiting = GameManager.recruiting();
        if (!recruiting.isPresent()) {
            throw NOT_RECRUITING.create();
        }

        JoinResult joinResult = recruiting.get().joinPlayer(player);
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

        source.sendFeedback(new LiteralText("You have joined the lobby! You will be teleported when the game starts"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int startGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        Optional<GameManager.Recruiting<?>> recruitingOpt = GameManager.recruiting();
        if (!recruitingOpt.isPresent()) {
            throw NOT_RECRUITING.create();
        }

        GameManager.Recruiting<?> recruiting = recruitingOpt.get();
        if (!recruiting.canStart()) {
            throw GAME_NOT_READY.create();
        }

        ConfiguredGame<?, ?> game = recruiting.getGame();

        MinecraftServer server = source.getMinecraftServer();
        PlayerManager playerManager = server.getPlayerManager();
        playerManager.broadcastChatMessage(new LiteralText(game.getName() + " is starting.."), MessageType.SYSTEM, Util.NIL_UUID);

        recruiting.start(source.getMinecraftServer())
                .handle((v, throwable) -> {
                    if (throwable != null) {
                        BedWarsMod.LOGGER.error("Failed to start game", throwable);
                        playerManager.broadcastChatMessage(new LiteralText("An exception occurred while trying to start game"), MessageType.SYSTEM, Util.NIL_UUID);
                    }
                    return null;
                });

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Optional<GameManager.Active<?>> activeOpt = GameManager.active();
        if (!activeOpt.isPresent()) {
            throw NOT_ACTIVE.create();
        }

        GameManager.Active<?> active = activeOpt.get();

        ServerCommandSource source = context.getSource();

        active.stop().handle((game, throwable) -> {
            if (throwable == null) {
                MinecraftServer server = source.getMinecraftServer();

                LiteralText message = new LiteralText(game.getConfigured().getName() + " has been stopped");
                server.getPlayerManager().broadcastChatMessage(message, MessageType.SYSTEM, Util.NIL_UUID);
            } else {
                source.sendError(new LiteralText("Failed to stop game: " + throwable.getClass()));
                BedWarsMod.LOGGER.error("Failed to stop game", throwable);
            }

            return null;
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int listGames(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(new LiteralText("Registered games:"), false);
        for (Identifier id : GameConfigs.getConfiguredGames().keySet()) {
            source.sendFeedback(new LiteralText(id.toString()), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
