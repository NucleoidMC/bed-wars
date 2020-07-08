package net.gegy1000.bedwars.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.GameType;
import net.minecraft.command.arguments.IdentifierArgumentType;
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
import net.minecraft.util.math.BlockPos;

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
        );
    }
    // @formatter:on

    private static int openGame(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier gameTypeId = IdentifierArgumentType.getIdentifier(context, "game_type");
        GameType<?> gameType = GameType.get(gameTypeId);
        if (gameType == null) {
            throw GAME_NOT_FOUND.create(gameTypeId);
        }

        Optional<GameManager.Inactive> inactive = GameManager.inactive();
        if (!inactive.isPresent()) {
            throw ALREADY_OPEN.create();
        }

        inactive.get().recruit(gameType);

        MinecraftServer server = context.getSource().getMinecraftServer();
        PlayerManager playerManager = server.getPlayerManager();

        ClickEvent joinClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game join");
        HoverEvent joinHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("/game join"));
        Style joinStyle = new Style()
                .setUnderline(true).setColor(Formatting.BLUE)
                .setClickEvent(joinClick)
                .setHoverEvent(joinHover);

        Text openMessage = gameType.getName().copy().append(" has opened! ")
                .append(new LiteralText("Click here to join").setStyle(joinStyle));
        playerManager.broadcastChatMessage(openMessage, false);

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

        if (!recruiting.get().joinPlayer(player)) {
            throw GAME_FULL.create();
        }

        Text joinMessage = player.getDisplayName()
                .append(" has joined the game lobby!")
                .setStyle(new Style().setColor(Formatting.YELLOW));
        playerManager.broadcastChatMessage(joinMessage, false);

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

        GameType<?> gameType = recruiting.getGameType();

        MinecraftServer server = source.getMinecraftServer();
        PlayerManager playerManager = server.getPlayerManager();
        playerManager.broadcastChatMessage(gameType.getName().copy().append(" is starting.."), false);

        recruiting.start(source.getWorld(), new BlockPos(100000, 40, 10000))
                .handle((v, throwable) -> {
                    if (throwable != null) {
                        BedWarsMod.LOGGER.error("Failed to start game", throwable);
                        playerManager.broadcastChatMessage(new LiteralText("An exception occurred while trying to start game"), false);
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

                Text name = game.getGameType().getName();
                server.getPlayerManager().broadcastChatMessage(name.copy().append(" has been stopped"), false);
            } else {
                source.sendError(new LiteralText("Failed to stop game: " + throwable.getClass()));
                BedWarsMod.LOGGER.error("Failed to stop game", throwable);
            }

            return null;
        });

        return Command.SINGLE_SUCCESS;
    }
}
