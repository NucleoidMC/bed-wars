package net.gegy1000.bedwars.game;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.gl.event.BlockBreakCallback;
import net.gegy1000.gl.event.CraftCheckCallback;
import net.gegy1000.gl.event.PlayerDeathCallback;
import net.gegy1000.gl.event.PlayerJoinCallback;
import net.gegy1000.gl.game.Game;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.game.GameType;
import net.gegy1000.gl.game.JoinResult;
import net.gegy1000.gl.game.StartResult;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class BedWars implements Game {
    public static final GameType<BedWars, BwConfig> TYPE = GameType.register(
            new Identifier(BedWarsMod.ID, "bed_wars"),
            BedWars::open,
            BwConfig.CODEC
    );

    public static final int RESPAWN_TIME_SECONDS = 3;

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            BedWars game = GameManager.openFor(TYPE);
            if (game != null) {
                game.tick();
            }
        });

        PlayerDeathCallback.EVENT.register((player, source) -> {
            BedWars game = GameManager.openFor(TYPE);
            if (game != null) {
                return game.events.onPlayerDeath(player, source);
            } else {
                return false;
            }
        });

        PlayerJoinCallback.EVENT.register(player -> {
            BedWars game = GameManager.openFor(TYPE);
            if (game != null) {
                game.events.onPlayerJoin(player);
            }
        });

        BlockBreakCallback.EVENT.register((world, player, pos) -> {
            BedWars game = GameManager.openFor(TYPE);
            if (game != null) {
                return game.events.onBreakBlock(player, pos);
            }
            return false;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BedWars game = GameManager.openFor(TYPE);
            if (game != null) {
                return game.events.onUseBlock(player, hitResult);
            }
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            BedWars game = GameManager.openFor(TYPE);
            if (game != null) {
                return game.events.onUseItem(player, world, hand);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            BedWars game = GameManager.openFor(TYPE);
            if (game != null) {
                return game.events.onAttackEntity(player, world, hand, entity, hitResult);
            }
            return ActionResult.PASS;
        });

        CraftCheckCallback.EVENT.register((world, player, recipe) -> GameManager.openFor(TYPE) == null);
    }

    public final ServerWorld world;
    public final BwConfig config;

    public final BwMap map;

    public final BwPlayerTracker playerTracker;

    public BwPhase phase;

    public final BwEvents events = new BwEvents(this);

    private boolean closed = false;

    private BedWars(BwConfig config, BwMap map) {
        this.world = map.getWorld();
        this.config = config;
        this.map = map;

        this.playerTracker = new BwPlayerTracker(map);
        this.phase = new BwWaiting(this);
    }

    private static CompletableFuture<BedWars> open(MinecraftServer server, BwConfig config) {
        return BwMap.create(server, config).thenApply(map -> new BedWars(config, map));
    }

    public static void resetPlayer(ServerPlayerEntity player) {
        player.inventory.clear();
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
    }

    private void tick() {
        if (this.closed) {
            return;
        }

        if (this.phase instanceof BwActive) {
            BwActive active = (BwActive) this.phase;
            BwWinStateLogic.WinResult winResult = active.tick();
            if (winResult != null) {
                this.switchPhaseTo(new BwClosing(active, winResult.getTeam()));
            }
        } else if (this.phase instanceof BwClosing) {
            BwClosing closing = (BwClosing) this.phase;
            if (closing.tick()) {
                this.stop();
            }
        }
    }

    @Override
    public StartResult requestStart() {
        if (this.phase instanceof BwWaiting) {
            Either<BwPhase, StartResult> result = ((BwWaiting) this.phase).tryStart();

            Optional<StartResult> err = result.right();
            if (err.isPresent()) {
                return err.get();
            }

            this.switchPhaseTo(result.left().get());

            return StartResult.OK;
        }

        return StartResult.ALREADY_STARTED;
    }

    private void switchPhaseTo(BwPhase toPhase) {
        if (this.phase != null) {
            this.phase.stop();
        }

        toPhase.start();
        this.phase = toPhase;
    }

    @Override
    public JoinResult offerPlayer(ServerPlayerEntity player) {
        return this.phase.offerPlayer(player);
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (this.closed) {
            return CompletableFuture.completedFuture(null);
        }

        this.closed = true;

        if (this.phase != null) {
            this.phase.stop();
        }

        this.playerTracker.restorePlayers();

        return this.map.delete();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }
}
