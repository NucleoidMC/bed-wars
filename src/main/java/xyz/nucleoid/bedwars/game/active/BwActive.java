package xyz.nucleoid.bedwars.game.active;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.custom.BridgeEggEntity;
import xyz.nucleoid.bedwars.custom.BwFireballEntity;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.custom.MovingCloud;
import xyz.nucleoid.bedwars.game.BwConfig;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.BwSpawnLogic;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.bedwars.game.active.modifiers.GameModifier;
import xyz.nucleoid.bedwars.game.active.modifiers.GameTrigger;
import xyz.nucleoid.bedwars.util.WoodBlocks;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.logic.combat.OldCombat;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.*;
import java.util.stream.Stream;

public final class BwActive {
    public static final int RESPAWN_TIME_SECONDS = 5;
    public static final long RESPAWN_TICKS = 20 * RESPAWN_TIME_SECONDS;
    public static final long CLOSE_TICKS = 10 * 20;
    public static final int BED_GONE_TICKS = 20 * 60 * 20;

    public final ServerWorld world;
    public final GameSpace gameSpace;

    public final BwMap map;
    public final BwConfig config;

    private final Map<PlayerRef, BwParticipant> participants = new Object2ObjectOpenHashMap<>();
    private final Map<GameTeam, TeamState> teams = new Reference2ObjectOpenHashMap<>();

    public final BwScoreboard scoreboard;
    public final BwBroadcast broadcast;
    public final BwTeamLogic teamLogic;
    public final BwKillLogic killLogic;
    public final BwWinStateLogic winStateLogic;
    public final BwMapLogic mapLogic;
    public final BwPlayerLogic playerLogic;
    public final BwSpawnLogic spawnLogic;
    private final BwBar bar;

    private boolean opened;

    private long startTime;
    private boolean destroyedBeds;

    private long lastWinCheck;

    private GameTeam winningTeam;
    private long closeTime;

    private final List<MovingCloud> movingClouds = new ArrayList<>();

    private BwActive(GameSpace gameSpace, BwMap map, BwConfig config, GlobalWidgets widgets) {
        this.world = gameSpace.getWorld();
        this.gameSpace = gameSpace;

        this.map = map;
        this.config = config;

        this.scoreboard = gameSpace.addResource(BwScoreboard.create(this, widgets));

        this.broadcast = new BwBroadcast(this);
        this.teamLogic = new BwTeamLogic(this);
        this.killLogic = new BwKillLogic(this);
        this.winStateLogic = new BwWinStateLogic(this);
        this.mapLogic = new BwMapLogic(this);
        this.playerLogic = new BwPlayerLogic(this);
        this.spawnLogic = new BwSpawnLogic(this.world, map);

        this.bar = BwBar.create(widgets);
    }

    public static void open(GameSpace gameSpace, BwMap map, BwConfig config, Multimap<GameTeam, ServerPlayerEntity> players) {
        gameSpace.openGame(game -> {
            GlobalWidgets widgets = new GlobalWidgets(game);

            BwActive active = new BwActive(gameSpace, map, config, widgets);
            active.addPlayers(players);

            for (GameTeam team : config.teams) {
                active.scoreboard.addTeam(team);
            }

            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.ALLOW);
            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.TEAM_CHAT, RuleResult.ALLOW);
            game.setRule(GameRule.TRIDENTS_LOYAL_IN_VOID, RuleResult.ALLOW);
            game.setRule(BedWars.BLAST_PROOF_GLASS_RULE, RuleResult.ALLOW);
            game.setRule(BedWars.LEAVES_DROP_GOLDEN_APPLES, RuleResult.ALLOW);
            game.setRule(BedWars.FAST_TREE_GROWTH, RuleResult.ALLOW);

            game.on(GameOpenListener.EVENT, active::onOpen);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);

            game.on(BreakBlockListener.EVENT, active::onBreakBlock);
            game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
            game.on(UseBlockListener.EVENT, active::onUseBlock);
            game.on(UseItemListener.EVENT, active::onUseItem);

            game.on(ExplosionListener.EVENT, affectedBlocks -> {
                affectedBlocks.removeIf(map::isProtectedBlock);
            });
        });
    }

    private void addPlayers(Multimap<GameTeam, ServerPlayerEntity> players) {
        MinecraftServer server = this.gameSpace.getServer();

        players.forEach((team, player) -> {
            BwParticipant participant = new BwParticipant(this, player, team);
            this.participants.put(participant.ref, participant);

            TeamState teamState = this.teams.computeIfAbsent(team, t -> new TeamState(server, t));
            teamState.players.add(player);
        });
    }

    private void onOpen() {
        this.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) {
                return;
            }

            this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);

            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.spawnPlayer(player, spawn);
            } else {
                BedWars.LOGGER.warn("No spawn for player {}", participant.ref);
                this.spawnLogic.spawnAtCenter(player);
            }
        });

        this.map.spawnShopkeepers(this.world, this, this.config);
        this.triggerModifiers(BwGameTriggers.GAME_RUNNING);

        this.startTime = this.world.getTime();
        this.opened = true;
    }

    private void addPlayer(ServerPlayerEntity player) {
        if (this.opened && this.isParticipant(player)) {
            this.rejoinPlayer(player);
        } else {
            this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
            this.spawnLogic.spawnAtCenter(player);
        }
    }

    private void rejoinPlayer(ServerPlayerEntity player) {
        BwParticipant participant = this.getParticipant(player);

        if (participant != null) {
            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.respawnOnTimer(player, spawn);
            } else {
                this.spawnLogic.respawnPlayer(player, GameMode.SPECTATOR);
                this.spawnLogic.spawnAtCenter(player);
            }
        }
    }

    private ActionResult onBreakBlock(ServerPlayerEntity player, BlockPos pos) {
        if (this.map.isProtectedBlock(pos)) {
            for (GameTeam team : this.config.teams) {
                BlockBounds bed = this.map.getTeamRegions(team).bed;
                if (bed != null && bed.contains(pos)) {
                    this.teamLogic.onBedBroken(player, pos);
                }
            }

            return ActionResult.FAIL;
        }


        ServerWorld world = player.getServerWorld();
        BlockState state = world.getBlockState(pos);

        // Automatic tree breaking
        if (state.isIn(BlockTags.LOGS) && !player.isSneaking()) {
            Set<BlockPos> logs = new HashSet<>();
            logs.add(pos);

            findLogs(world, pos, logs);

            for (BlockPos log : logs) {
                BlockState logState = world.getBlockState(log);
                world.breakBlock(log, false);

                // Drop 1-3 planks
                int count = 1 + world.random.nextInt(3);
                world.spawnEntity(new ItemEntity(world, log.getX(), log.getY(), log.getZ(), new ItemStack(WoodBlocks.planksOf(logState).getBlock(), count)));
            }

            return ActionResult.FAIL;
        }

        if (state.isIn(BlockTags.LEAVES)) {
            if (world.random.nextDouble() < 0.025) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(WoodBlocks.saplingOf(state).getBlock())));
            }

            if (world.random.nextDouble() < 0.01) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.GOLDEN_APPLE)));
            }

            world.removeBlock(pos, false);

            return ActionResult.FAIL;
        }

        // Drop ingots from gold
        if (state.isOf(Blocks.GOLD_ORE)) {
            world.breakBlock(pos, false);

            // Drop 2-4 ingots
            int count = 2 + world.random.nextInt(3);
            ItemStack stack = new ItemStack(Items.GOLD_INGOT, count);

            if (!player.inventory.insertStack(stack.copy())) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
            }
        }

        // Drop ingots from gold
        if (state.isOf(Blocks.DIAMOND_ORE)) {
            world.breakBlock(pos, false);

            // Drop 1-2 diamonds
            int count = 1 + world.random.nextInt(2);
            ItemStack stack = new ItemStack(Items.DIAMOND, count);

            if (!player.inventory.insertStack(stack.copy())) {
                world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
            }
        }

        return ActionResult.PASS;
    }

    private void findLogs(ServerWorld world, BlockPos pos, Set<BlockPos> logs) {
        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                for(int y = -1; y <= 1; y++) {
                    BlockPos local = pos.add(x, y, z);
                    BlockState state = world.getBlockState(local);

                    if (!logs.contains(local)) {
                        if (state.isIn(BlockTags.LOGS)) {
                            logs.add(local);
                            findLogs(world, local, logs);
                        }
                    }
                }
            }
        }
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity attackedPlayer, DamageSource source, float amount) {
        if (source == DamageSource.OUT_OF_WORLD && attackedPlayer.isSpectator()) {
            return ActionResult.FAIL;
        }

        BwParticipant attackedParticipant = this.getParticipant(attackedPlayer);
        if (attackedParticipant == null) {
            return ActionResult.PASS;
        }

        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity) {
            ServerPlayerEntity attackerPlayer = (ServerPlayerEntity) attacker;
            BwParticipant attackerParticipant = this.getParticipant(attackerPlayer);

            if (attackerParticipant != null) {
                if (attackerParticipant.team == attackedParticipant.team) {
                    return ActionResult.FAIL;
                }

                attackedParticipant.lastAttack = AttackRecord.fromAttacker(attackerPlayer);
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        if (pos == null) {
            return ActionResult.PASS;
        }

        BwParticipant participant = this.getParticipant(player);
        if (participant != null) {
            ItemStack heldStack = player.getStackInHand(hand);
            if (heldStack.getItem() == Items.FIRE_CHARGE) {
                this.onUseFireball(player, heldStack);
                return ActionResult.SUCCESS;
            }

            BlockState state = this.world.getBlockState(pos);
            if (state.getBlock() instanceof AbstractChestBlock) {
                return this.onUseChest(player, participant, pos);
            } else if (state.getBlock().isIn(BlockTags.BEDS)) {
                player.getStackInHand(hand).useOnBlock(new ItemPlacementContext(player, hand, player.getStackInHand(hand), hitResult));

                return ActionResult.CONSUME;
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseChest(ServerPlayerEntity player, BwParticipant participant, BlockPos pos) {
        GameTeam team = participant.team;

        GameTeam chestTeam = this.getOwningTeamForChest(pos);
        if (chestTeam == null || chestTeam.equals(team) || player.isSpectator()) {
            return ActionResult.PASS;
        }

        BwActive.TeamState chestTeamState = this.getTeam(chestTeam);
        if (chestTeamState == null || chestTeamState.eliminated) {
            return ActionResult.PASS;
        }

        player.sendMessage(new TranslatableText("text.bedwars.cannot_open_chest").formatted(Formatting.RED), true);

        return ActionResult.FAIL;
    }

    @Nullable
    private GameTeam getOwningTeamForChest(BlockPos pos) {
        for (GameTeam team : this.config.teams) {
            BwMap.TeamRegions regions = this.map.getTeamRegions(team);
            if (regions.teamChest != null && regions.teamChest.contains(pos)) {
                return team;
            }
        }
        return null;
    }

    private TypedActionResult<ItemStack> onUseItem(ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isEmpty()) {
            return TypedActionResult.pass(ItemStack.EMPTY);
        }

        if (stack.getItem() == Items.FIRE_CHARGE) {
            return this.onUseFireball(player, stack);
        } else if (stack.getItem() == BwItems.BRIDGE_EGG) {
            return this.onUseBridgeEgg(player, stack);
        } else if (stack.getItem() == BwItems.MOVING_CLOUD) {
            return this.onUseMovingCloud(player, stack);
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    private TypedActionResult<ItemStack> onUseFireball(ServerPlayerEntity player, ItemStack stack) {
        Vec3d dir = player.getRotationVec(1.0F);

        BwFireballEntity fireball = new BwFireballEntity(this.world, player, dir.x * 0.5, dir.y * 0.5, dir.z * 0.5);
        fireball.explosionPower = 2;
        fireball.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireball.getZ() + dir.z);

        this.world.spawnEntity(fireball);

        player.getItemCooldownManager().set(Items.FIRE_CHARGE, 20);
        stack.decrement(1);

        return TypedActionResult.success(ItemStack.EMPTY);
    }

    private TypedActionResult<ItemStack> onUseBridgeEgg(ServerPlayerEntity player, ItemStack stack) {
        this.world.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS,
                0.5F, 0.4F / (this.world.random.nextFloat() * 0.4F + 0.8F)
        );

        // Get player wool color
        GameTeam team = this.getTeam(PlayerRef.of(player));
        if (team == null) {
            return TypedActionResult.pass(stack);
        }

        BlockState state = ColoredBlocks.wool(team.getDye()).getDefaultState();

        // Spawn egg
        BridgeEggEntity eggEntity = new BridgeEggEntity(this.world, player, state);
        eggEntity.setItem(stack);
        eggEntity.setProperties(player, player.pitch, player.yaw, 0.0F, 1.5F, 1.0F);

        this.world.spawnEntity(eggEntity);

        if (!player.abilities.creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.consume(stack);
    }

    private TypedActionResult<ItemStack> onUseMovingCloud(ServerPlayerEntity player, ItemStack stack) {
        this.world.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS,
                0.5F, 0.4F / (this.world.random.nextFloat() * 0.4F + 0.8F)
        );

        Direction direction = player.getHorizontalFacing();
        BlockPos blockPos = player.getBlockPos().down().offset(direction);
        if (!this.world.isAir(blockPos)) {
            return TypedActionResult.fail(stack);
        }

        MovingCloud cloud = new MovingCloud(this.world, blockPos, direction);
        this.movingClouds.add(cloud);

        if (!player.abilities.creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.consume(stack);
    }

    private void tick() {
        if (this.winningTeam != null) {
            if (this.tickClosing()) {
                this.gameSpace.close(GameCloseReason.FINISHED);
            }
            return;
        }

        long time = this.world.getTime();

        PlayerSet players = this.gameSpace.getPlayers();

        // TODO: this should be modular
        if (!this.destroyedBeds) {
            if (time - this.startTime > BED_GONE_TICKS) {
                this.destroyedBeds = true;

                for (GameTeam team : this.config.teams) {
                    this.teamLogic.removeBed(team);
                }

                players.sendMessage(new TranslatableText("text.bedwars.all_beds_destroyed").formatted(Formatting.RED));
                players.sendSound(SoundEvents.BLOCK_END_PORTAL_SPAWN);
            }
        }

        if (time % 20 == 0) {
            long bedGoneTime = this.startTime + BED_GONE_TICKS;
            this.bar.update(bedGoneTime - time, BED_GONE_TICKS);

            for (ServerPlayerEntity player : players) {
                if (!player.isSpectator() && !this.map.isLegalAt(player.getBlockPos())) {
                    player.damage(DamageSource.OUT_OF_WORLD, 10000.0F);
                }
            }
        }

        this.movingClouds.removeIf(MovingCloud::tick);

        BwWinStateLogic.WinResult winResult = this.tickActive();
        if (winResult != null) {
            this.winningTeam = winResult.getTeam();
            this.closeTime = this.world.getTime() + CLOSE_TICKS;
        }
    }

    @Nullable
    private BwWinStateLogic.WinResult tickActive() {
        long time = this.world.getTime();

        if (time - this.lastWinCheck > 20) {
            BwWinStateLogic.WinResult winResult = this.winStateLogic.checkWinResult();
            if (winResult != null) {
                this.broadcast.broadcastGameOver(winResult);
                return winResult;
            }

            this.lastWinCheck = time;
        }

        this.mapLogic.tick();

        this.scoreboard.tick();
        this.playerLogic.tick();

        // Tick modifiers
        for (GameModifier modifier : this.config.modifiers) {
            if (modifier.getTrigger().tickable) {
                modifier.tick(this);
            }
        }

        return null;
    }

    private boolean tickClosing() {
        if (this.winningTeam != null) {
            this.spawnFireworks(this.winningTeam);
        }

        return this.world.getTime() >= this.closeTime;
    }

    private void spawnFireworks(GameTeam team) {
        Random random = this.world.random;

        if (random.nextInt(18) == 0) {
            List<ServerPlayerEntity> players = Lists.newArrayList(this.players());
            ServerPlayerEntity player = players.get(random.nextInt(players.size()));

            int flight = random.nextInt(3);
            FireworkItem.Type type = random.nextInt(4) == 0 ? FireworkItem.Type.STAR : FireworkItem.Type.BURST;
            FireworkRocketEntity firework = new FireworkRocketEntity(
                    this.world,
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    team.createFirework(flight, type)
            );

            this.world.spawnEntity(firework);
        }
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        BwParticipant participant = this.getParticipant(player);

        // TODO: cancel if cause is own player
        if (participant != null) {
            this.killLogic.onPlayerDeath(participant, player, source);
            return ActionResult.FAIL;
        }

        return ActionResult.SUCCESS;
    }

    public ItemStack createArmor(ItemStack stack) {
        return ItemStackBuilder.of(stack).setUnbreakable().build();
    }

    public ItemStack createTool(ItemStack stack) {
        stack = ItemStackBuilder.of(stack).setUnbreakable().build();
        if (this.config.combat.isOldMechanics()) {
            stack = OldCombat.applyTo(stack);
        }

        return stack;
    }

    public void triggerModifiers(GameTrigger type) {
        for (GameModifier modifier : this.config.modifiers) {
            if (modifier.getTrigger() == type) {
                modifier.init(this);
            }
        }
    }

    @Nullable
    public BwParticipant getParticipant(PlayerEntity player) {
        return this.participants.get(PlayerRef.of(player));
    }

    @Nullable
    public BwParticipant getParticipant(PlayerRef player) {
        return this.participants.get(player);
    }

    @Nullable
    public GameTeam getTeam(PlayerRef player) {
        BwParticipant participant = this.participants.get(player);
        if (participant != null) {
            return participant.team;
        }
        return null;
    }

    public boolean isParticipant(PlayerEntity player) {
        return this.participants.containsKey(PlayerRef.of(player));
    }

    public Stream<BwParticipant> participantsFor(GameTeam team) {
        return this.participants.values().stream().filter(participant -> participant.team == team);
    }

    public PlayerSet playersFor(GameTeam team) {
        TeamState teamState = this.teams.get(team);
        return teamState != null ? teamState.players : PlayerSet.EMPTY;
    }

    public  PlayerSet players() {
        return this.gameSpace.getPlayers();
    }

    public Stream<BwParticipant> participants() {
        return this.participants.values().stream();
    }

    public Stream<TeamState> teams() {
        return this.teams.values().stream();
    }

    public int getTeamCount() {
        return this.teams.size();
    }

    @Nullable
    public TeamState getTeam(GameTeam team) {
        return this.teams.get(team);
    }

    public static class TeamState {
        public static final int MAX_SHARPNESS = 3;
        public static final int MAX_PROTECTION = 3;

        final MutablePlayerSet players;
        final GameTeam team;
        boolean hasBed = true;
        boolean eliminated;

        public boolean trapSet;
        public boolean healPool;
        public boolean hasteEnabled;
        public int swordSharpness;
        public int armorProtection;

        TeamState(MinecraftServer server, GameTeam team) {
            this.players = new MutablePlayerSet(server);
            this.team = team;
        }
    }
}
