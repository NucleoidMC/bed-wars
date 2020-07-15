package net.gegy1000.bedwars.game;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.modifiers.BwGameTriggers;
import net.gegy1000.gl.event.BlockBreakCallback;
import net.gegy1000.gl.event.CraftCheckCallback;
import net.gegy1000.gl.event.PlayerDeathCallback;
import net.gegy1000.gl.event.PlayerJoinCallback;
import net.gegy1000.gl.game.Game;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.GameType;
import net.gegy1000.gl.game.JoinResult;
import net.gegy1000.gl.game.StartResult;
import net.gegy1000.gl.game.modifier.GameModifier;
import net.gegy1000.gl.game.modifier.GameTrigger;
import net.gegy1000.gl.util.ItemUtil;
import net.gegy1000.gl.logic.combat.OldCombat;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class BedWars implements Game {
    public static final GameType<BedWars, BedWarsConfig> TYPE = GameType.register(
            new Identifier(BedWarsMod.ID, "bed_wars"),
            BedWars::open,
            BedWarsConfig.CODEC
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
    public final BedWarsConfig config;

    public BwState state;

    public final BwMap map;

    public final BwBroadcast broadcast = new BwBroadcast(this);
    public final BwScoreboardLogic scoreboardLogic = new BwScoreboardLogic(this);
    public final BwPlayerLogic playerLogic = new BwPlayerLogic(this);
    public final BwTeamLogic teamLogic = new BwTeamLogic(this);
    public final BwKillLogic killLogic = new BwKillLogic(this);
    public final BwWinStateLogic winStateLogic = new BwWinStateLogic(this);
    public final BwMapLogic mapLogic = new BwMapLogic(this);

    public final BwEvents events = new BwEvents(this);

    public BwWaitingLogic waiting;
    private BwCloseLogic closing;

    private final Map<UUID, PlayerSnapshot> playerSnapshots = new HashMap<>();

    private boolean active = false;
    private boolean closed = false;

    private long lastWinCheck;

    private BedWars(BedWarsConfig config, BwMap map) {
        this.world = map.getWorld();
        this.config = config;
        this.map = map;

        this.waiting = new BwWaitingLogic(this);
    }

    private static CompletableFuture<BedWars> open(MinecraftServer server, BedWarsConfig config) {
        return BwMap.create(server, config)
                .thenApply(map -> new BedWars(config, map));
    }

    private void tick() {
        if (!this.active) {
            return;
        }

        long time = this.world.getTime();

        if (this.closing != null) {
            if (this.closing.tick()) {
                this.stop();
                return;
            }
        } else {
            if (time - this.lastWinCheck > 20) {
                BwWinStateLogic.WinResult winResult = this.winStateLogic.checkWinResult();
                if (winResult != null) {
                    this.broadcast.broadcastGameOver(winResult);
                    this.closing = new BwCloseLogic(this, winResult.getTeam());
                    return;
                }

                this.lastWinCheck = time;
            }

            this.mapLogic.tick();
        }

        this.scoreboardLogic.tick();
        this.playerLogic.tick();

        // Tick modifiers
        for (GameModifier modifier : this.config.getModifiers()) {
            if (modifier.getTrigger().tickable) {
                modifier.tick(this);
            }
        }
    }

    public ItemStack createArmor(ItemStack stack) {
        return ItemUtil.unbreakable(stack);
    }

    public void triggerModifiers(GameTrigger type) {
        for (GameModifier modifier : this.config.getModifiers()) {
            if (modifier.getTrigger() == type) {
                modifier.init(this);
            }
        }
    }

    public ItemStack createTool(ItemStack stack) {
        stack = ItemUtil.unbreakable(stack);
        if (this.config.getCombatConfig().isOldMechanics()) {
            stack = OldCombat.applyTo(stack);
        }

        return stack;
    }

    public void joinPlayerToMap(ServerPlayerEntity player) {
        this.playerSnapshots.put(player.getUuid(), PlayerSnapshot.take(player));
        this.playerLogic.resetPlayer(player);

        player.getEnderChestInventory().clear();
    }

    private void restorePlayers() {
        this.playerSnapshots.forEach((uuid, snapshot) -> {
            // TODO: restore if offline
            PlayerEntity player = this.world.getPlayerByUuid(uuid);
            if (player != null && player instanceof ServerPlayerEntity) {
                snapshot.restore((ServerPlayerEntity) player);
            }
        });
    }

    @Override
    public StartResult requestStart() {
        if (this.active) {
            return StartResult.ALREADY_STARTED;
        }

        Either<BwState, StartResult> result = this.waiting.tryStart();

        Optional<StartResult> err = result.right();
        if (err.isPresent()) {
            return err.get();
        }

        BwState state = result.left().get();
        this.startWith(state);

        return StartResult.OK;
    }

    private void startWith(BwState state) {
        this.active = true;
        this.state = state;

        this.playerLogic.resetPlayers();
        this.scoreboardLogic.setupScoreboard();

        for (GameTeam team : this.config.getTeams()) {
            this.scoreboardLogic.setupTeam(team);
        }

        this.state.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) {
                return;
            }

            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.spawnPlayer(player, spawn);
            } else {
                BedWarsMod.LOGGER.warn("No spawn for player {}", participant.playerId);
                this.playerLogic.spawnSpectator(player);
            }
        });

        this.map.spawnShopkeepers(this.config);
        this.triggerModifiers(BwGameTriggers.GAME_RUNNING);
    }

    @Override
    public JoinResult offerPlayer(ServerPlayerEntity player) {
        if (this.waiting != null) {
            return this.waiting.offerPlayer(player);
        }

        this.joinPlayerToMap(player);
        this.playerLogic.spawnSpectator(player);

        return JoinResult.OK;
    }

    @Override
    public CompletableFuture<Void> stop() {
        if (this.closed) {
            return CompletableFuture.completedFuture(null);
        }

        this.closed = true;

        this.restorePlayers();

        if (this.active) {
            this.scoreboardLogic.resetScoreboard();
            this.active = false;
        }

        return this.map.delete();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    public static class PlayerSnapshot {
        private final RegistryKey<World> dimension;
        private final Vec3d position;
        private final GameMode gameMode;
        private final DefaultedList<ItemStack> inventory;
        private final DefaultedList<ItemStack> enderInventory;
        private final Collection<StatusEffectInstance> potionEffects;

        private PlayerSnapshot(
                RegistryKey<World> dimension, Vec3d position,
                GameMode gameMode,
                DefaultedList<ItemStack> inventory,
                DefaultedList<ItemStack> enderInventory,
                Collection<StatusEffectInstance> potionEffects
        ) {
            this.dimension = dimension;
            this.position = position;
            this.gameMode = gameMode;
            this.inventory = inventory;
            this.enderInventory = enderInventory;
            this.potionEffects = potionEffects;
        }

        public static PlayerSnapshot take(ServerPlayerEntity player) {
            RegistryKey<World> dimension = player.world.getRegistryKey();
            Vec3d position = player.getPos();
            GameMode gameMode = player.interactionManager.getGameMode();

            DefaultedList<ItemStack> inventory = snapshotInventory(player.inventory);
            DefaultedList<ItemStack> enderInventory = snapshotInventory(player.getEnderChestInventory());

            List<StatusEffectInstance> potionEffects = player.getStatusEffects().stream()
                    .map(StatusEffectInstance::new)
                    .collect(Collectors.toList());

            return new PlayerSnapshot(dimension, position, gameMode, inventory, enderInventory, potionEffects);
        }

        public void restore(ServerPlayerEntity player) {
            ServerWorld world = player.getServerWorld().getServer().getWorld(this.dimension);

            player.setGameMode(GameMode.ADVENTURE);

            this.restoreInventory(player.inventory, this.inventory);
            this.restoreInventory(player.getEnderChestInventory(), this.enderInventory);

            player.clearStatusEffects();
            for (StatusEffectInstance potionEffect : this.potionEffects) {
                player.addStatusEffect(potionEffect);
            }

            player.teleport(world, this.position.x, this.position.y, this.position.z, 0.0F, 0.0F);
            player.setGameMode(this.gameMode);
        }

        private void restoreInventory(Inventory inventory, DefaultedList<ItemStack> from) {
            inventory.clear();
            for (int i = 0; i < from.size(); i++) {
                inventory.setStack(i, from.get(i));
            }
        }

        private static DefaultedList<ItemStack> snapshotInventory(Inventory inventory) {
            DefaultedList<ItemStack> copy = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
            for (int i = 0; i < copy.size(); i++) {
                copy.set(i, inventory.getStack(i));
            }
            return copy;
        }
    }
}
