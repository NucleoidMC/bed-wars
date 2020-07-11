package net.gegy1000.bedwars.game.bw;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.event.BlockBreakCallback;
import net.gegy1000.bedwars.event.CraftCheckCallback;
import net.gegy1000.bedwars.event.PlayerDeathCallback;
import net.gegy1000.bedwars.event.PlayerJoinCallback;
import net.gegy1000.bedwars.game.Game;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.GameTeam;
import net.gegy1000.bedwars.game.GameType;
import net.gegy1000.bedwars.game.modifier.GameModifier;
import net.gegy1000.bedwars.util.ItemUtil;
import net.gegy1000.bedwars.util.OldCombat;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class BedWars implements Game {
    public static final GameType<BedWars, BedWarsConfig> TYPE = GameType.register(
            new Identifier(BedWarsMod.ID, "bed_wars"),
            BedWars::initialize,
            BedWarsConfig.CODEC
    );

    public static final int RESPAWN_TIME_SECONDS = 3;

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            BedWars game = GameManager.activeFor(TYPE);
            if (game != null) {
                game.tick();
            }
        });

        PlayerDeathCallback.EVENT.register((player, source) -> {
            BedWars game = GameManager.activeFor(TYPE);
            if (game != null) {
                return game.onPlayerDeath(player, source);
            } else {
                return false;
            }
        });

        PlayerJoinCallback.EVENT.register(player -> {
            BedWars game = GameManager.activeFor(TYPE);
            if (game != null) {
                game.onPlayerJoin(player);
            }
        });

        BlockBreakCallback.EVENT.register((world, player, pos) -> {
            BedWars game = GameManager.activeFor(TYPE);
            if (game != null) {
                return game.onBreakBlock(player, pos);
            }
            return false;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BedWars game = GameManager.activeFor(TYPE);
            if (game != null) {
                return game.onUseBlock(player, hitResult);
            }
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            BedWars game = GameManager.activeFor(TYPE);
            if (game != null) {
                return game.onUseItem(player, world, hand);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            BedWars game = GameManager.activeFor(TYPE);
            if (game != null) {
                return game.onAttackEntity(player, world, hand, entity, hitResult);
            }
            return ActionResult.PASS;
        });

        CraftCheckCallback.EVENT.register((world, player, recipe) -> GameManager.activeFor(TYPE) == null);
    }

    public final ServerWorld world;
    public final BedWarsConfig config;

    public final BwMap map;
    public final BwState state;

    public final BwBroadcast broadcast = new BwBroadcast(this);
    public final BwScoreboardLogic scoreboardLogic = new BwScoreboardLogic(this);
    public final BwPlayerLogic playerLogic = new BwPlayerLogic(this);
    public final BwTeamLogic teamLogic = new BwTeamLogic(this);
    public final BwKillLogic killLogic = new BwKillLogic(this);
    public final BwWinStateLogic winStateLogic = new BwWinStateLogic(this);
    public final BwMapLogic mapLogic = new BwMapLogic(this);
    public final List<GameModifier> modifiers = new ArrayList<>();

    private BwCloseLogic closing;
    private boolean active = true;

    private int ticks;

    private long lastWinCheck;

    private BedWars(BedWarsConfig config, BwMap map, BwState state) {
        this.world = map.getWorld();
        this.config = config;
        this.map = map;
        this.state = state;
    }

    private static CompletableFuture<BedWars> initialize(MinecraftServer server, List<ServerPlayerEntity> players, BedWarsConfig config) {
        return BwMap.create(server, config)
                .thenApplyAsync(map -> {
                    BwState state = BwState.create(players, config);

                    BedWars game = new BedWars(config, map, state);
                    game.initializeModifiers();
                    game.playerLogic.setupPlayers();

                    game.scoreboardLogic.setupScoreboard();

                    for (GameTeam team : config.getTeams()) {
                        game.scoreboardLogic.setupTeam(team);
                    }

                    return game;
                }, server)
                .thenApplyAsync(game -> {
                    game.state.participants().forEach(participant -> {
                        ServerPlayerEntity player = participant.player();
                        if (player == null) {
                            return;
                        }

                        BwMap.TeamSpawn spawn = game.teamLogic.tryRespawn(participant);
                        if (spawn != null) {
                            game.playerLogic.spawnPlayer(player, spawn);
                        } else {
                            BedWarsMod.LOGGER.warn("No spawn for player {}", participant.playerId);
                            game.playerLogic.spawnSpectator(player);
                        }
                    });

                    return game;
                }, server);
    }

    public void onExplosion(List<BlockPos> affectedBlocks) {
        affectedBlocks.removeIf(this.map::isStandardBlock);
    }

    private boolean onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        BwState.Participant participant = this.state.getParticipant(player);

        // TODO: this should go in KillLogic?

        // TODO: cancel if cause is own player
        if (participant != null) {
            this.killLogic.onPlayerDeath(player, source);

            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            this.broadcast.broadcastDeath(player, source, spawn == null);

            // Run death modifiers
            triggerModifiers(GameModifier.Trigger.PLAYER_DEATH);

            if (spawn != null) {
                this.playerLogic.respawnOnTimer(player, spawn);
            } else {
                this.dropEnderChest(player, participant);

                this.playerLogic.spawnSpectator(player);
                this.winStateLogic.eliminatePlayer(participant);

                // Run final death modifiers
                triggerModifiers(GameModifier.Trigger.FINAL_DEATH);
            }

            this.scoreboardLogic.markDirty();

            return true;
        }

        return false;
    }

    private void dropEnderChest(ServerPlayerEntity player, BwState.Participant participant) {
        EnderChestInventory enderChest = player.getEnderChestInventory();

        BwMap.TeamRegions teamRegions = this.map.getTeamRegions(participant.team);
        if (teamRegions.spawn != null) {
            Vec3d dropSpawn = teamRegions.spawn.getCenter();

            for (int slot = 0; slot < enderChest.size(); slot++) {
                ItemStack stack = enderChest.removeStack(slot);
                if (!stack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(this.world, dropSpawn.x, dropSpawn.y + 0.5, dropSpawn.z, stack);
                    this.world.spawnEntity(itemEntity);
                }
            }
        }

        enderChest.clear();
    }

    private void onPlayerJoin(ServerPlayerEntity player) {
        BwState.Participant participant = this.state.getParticipant(player);

        if (participant != null) {
            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.respawnOnTimer(player, spawn);
            } else {
                this.playerLogic.spawnSpectator(player);
            }

            this.scoreboardLogic.markDirty();
        }
    }

    private boolean onBreakBlock(ServerPlayerEntity player, BlockPos pos) {
        if (this.map.contains(pos)) {
            BlockState state = this.world.getBlockState(pos);

            if (this.map.isStandardBlock(pos)) {
                if (state.getBlock().isIn(BlockTags.BEDS)) {
                    this.teamLogic.onBedBroken(player, pos);
                }

                return true;
            }
        }

        return false;
    }

    private ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        BwState.Participant participant = this.state.getParticipant(player);
        BwState.Participant attackedParticipant = this.state.getParticipant(entity.getUuid());
        if (participant != null && attackedParticipant != null) {
            if (participant.team == attackedParticipant.team) {
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    private ActionResult onUseBlock(PlayerEntity player, BlockHitResult hitResult) {
        BwState.Participant participant = this.state.getParticipant(player);

        if (participant != null) {
            BlockPos pos = hitResult.getBlockPos();
            if (pos != null) {
                if (this.map.contains(pos)) {
                    BlockState state = this.world.getBlockState(pos);
                    if (state.getBlock() instanceof AbstractChestBlock) {
                        return this.onUseChest(participant, pos);
                    }
                } else {
                    return ActionResult.FAIL;
                }
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseChest(BwState.Participant participant, BlockPos pos) {
        GameTeam team = participant.team;

        GameTeam chestTeam = this.getOwningTeamForChest(pos);
        if (chestTeam == null || chestTeam.equals(team)) {
            return ActionResult.PASS;
        }

        BwState.TeamState chestTeamState = this.state.getTeam(chestTeam);
        if (chestTeamState == null || chestTeamState.eliminated) {
            return ActionResult.PASS;
        }

        ServerPlayerEntity player = participant.player();
        if (player != null) {
            player.sendMessage(new LiteralText("You cannot access this team's chest!").formatted(Formatting.RED), true);
        }

        return ActionResult.FAIL;
    }

    @Nullable
    private GameTeam getOwningTeamForChest(BlockPos pos) {
        for (GameTeam team : this.config.getTeams()) {
            BwMap.TeamRegions regions = this.map.getTeamRegions(team);
            if (regions.teamChest != null && regions.teamChest.contains(pos)) {
                return team;
            }
        }
        return null;
    }

    public TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isEmpty()) {
            if (stack.getItem() == Items.FIRE_CHARGE) {
                Vec3d dir = player.getRotationVec(1.0F);

                FireballEntity fireball = new FireballEntity(world, player, dir.x * 0.5, dir.y * 0.5, dir.z * 0.5);
                fireball.explosionPower = 2;
                fireball.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireball.getZ() + dir.z);

                world.spawnEntity(fireball);

                player.getItemCooldownManager().set(Items.FIRE_CHARGE, 20);
                stack.decrement(1);

                return TypedActionResult.success(ItemStack.EMPTY);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    private void tick() {
        if (!this.active) {
            return;
        }

        if (this.ticks++ == 20) {
            this.map.spawnShopkeepers(this.config);

            // Broadcasting the game start event here because this is when the game officially starts, maybe this should be moved?
            triggerModifiers(GameModifier.Trigger.GAME_START);
        }

        long time = this.world.getTime();

        if (this.closing != null) {
            if (this.closing.tick()) {
                this.active = false;
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
        for (GameModifier modifier : modifiers) {
            if (modifier.getTrigger().isTickable()) {
                modifier.tick(this);
            }
        }
    }

    public ItemStack createArmor(ItemStack stack) {
        return ItemUtil.unbreakable(stack);
    }

    public void triggerModifiers(GameModifier.Trigger type) {
        for (GameModifier modifier : modifiers) {
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

    private void initializeModifiers() {
        // Empty for now, implement modifiers later
    }

    @Override
    public CompletableFuture<Void> stop() {
        this.active = false;
        this.restorePlayers();
        this.scoreboardLogic.resetScoreboard();

        return this.map.delete();
    }

    private void restorePlayers() {
        this.state.participants().forEach(participant -> {
            // TODO: restore if offline
            ServerPlayerEntity player = participant.player();
            if (player != null) {
                participant.formerState.restore(player);
            }
        });
    }

    @Override
    public boolean isActive() {
        return this.active;
    }
}
