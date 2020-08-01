package net.gegy1000.bedwars.game.active;

import com.google.common.collect.Multimap;
import net.gegy1000.bedwars.BedWars;
import net.gegy1000.bedwars.custom.BridgeEggEntity;
import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.bedwars.custom.BwFireballEntity;
import net.gegy1000.bedwars.game.BwConfig;
import net.gegy1000.bedwars.game.BwMap;
import net.gegy1000.bedwars.game.active.modifiers.BwGameTriggers;
import net.gegy1000.bedwars.game.active.modifiers.GameModifier;
import net.gegy1000.bedwars.game.active.modifiers.GameTrigger;
import net.gegy1000.gl.game.Game;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.JoinResult;
import net.gegy1000.gl.game.event.AttackEntityListener;
import net.gegy1000.gl.game.event.BreakBlockListener;
import net.gegy1000.gl.game.event.ExplosionListener;
import net.gegy1000.gl.game.event.GameCloseListener;
import net.gegy1000.gl.game.event.GameOpenListener;
import net.gegy1000.gl.game.event.GameTickListener;
import net.gegy1000.gl.game.event.OfferPlayerListener;
import net.gegy1000.gl.game.event.PlayerAddListener;
import net.gegy1000.gl.game.event.PlayerDeathListener;
import net.gegy1000.gl.game.event.PlayerRejoinListener;
import net.gegy1000.gl.game.event.UseBlockListener;
import net.gegy1000.gl.game.event.UseItemListener;
import net.gegy1000.gl.game.rule.GameRule;
import net.gegy1000.gl.game.rule.RuleResult;
import net.gegy1000.gl.item.CustomItem;
import net.gegy1000.gl.logic.combat.OldCombat;
import net.gegy1000.gl.util.ColoredBlocks;
import net.gegy1000.gl.util.ItemUtil;
import net.gegy1000.gl.world.BlockBounds;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BwActive {
    public static final int RESPAWN_TIME_SECONDS = 5;
    public static final long RESPAWN_TICKS = 20 * RESPAWN_TIME_SECONDS;
    public static final long CLOSE_TICKS = 10 * 20;

    public final BwMap map;
    public final BwConfig config;

    private final Map<UUID, BwParticipant> participants = new HashMap<>();
    private final Map<GameTeam, TeamState> teams = new HashMap<>();

    public final BwScoreboard scoreboard;
    public final BwBroadcast broadcast;
    public final BwTeamLogic teamLogic;
    public final BwKillLogic killLogic;
    public final BwWinStateLogic winStateLogic;
    public final BwMapLogic mapLogic;
    public final BwPlayerLogic playerLogic;

    private long startTime;
    private boolean destroyedBeds;

    private long lastWinCheck;

    private GameTeam winningTeam;
    private long closeTime;

    private boolean closed;

    private BwActive(BwMap map, BwConfig config) {
        this.map = map;
        this.config = config;

        this.scoreboard = BwScoreboard.create(this);

        this.broadcast = new BwBroadcast(this);
        this.teamLogic = new BwTeamLogic(this);
        this.killLogic = new BwKillLogic(this);
        this.winStateLogic = new BwWinStateLogic(this);
        this.mapLogic = new BwMapLogic(this);
        this.playerLogic = new BwPlayerLogic(this);
    }

    public static Game open(BwMap map, BwConfig config, Multimap<GameTeam, ServerPlayerEntity> players) {
        BwActive active = new BwActive(map, config);
        active.addPlayers(players);

        for (GameTeam team : config.getTeams()) {
            active.scoreboard.addTeam(team);
        }

        Game.Builder builder = Game.builder();
        builder.setMap(map.asInner());

        builder.setRule(GameRule.ALLOW_PORTALS, RuleResult.DENY);
        builder.setRule(GameRule.ALLOW_PVP, RuleResult.ALLOW);
        builder.setRule(GameRule.INSTANT_LIGHT_TNT, RuleResult.ALLOW);
        builder.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
        builder.setRule(BedWars.BLAST_PROOF_GLASS_RULE, RuleResult.ALLOW);

        builder.on(GameOpenListener.EVENT, active::open);
        builder.on(GameCloseListener.EVENT, active::close);

        builder.on(OfferPlayerListener.EVENT, (game, player) -> JoinResult.ok());
        builder.on(PlayerAddListener.EVENT, active::addPlayer);

        builder.on(GameTickListener.EVENT, active::tick);

        builder.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
        builder.on(PlayerRejoinListener.EVENT, active::rejoinPlayer);

        builder.on(BreakBlockListener.EVENT, active::onBreakBlock);
        builder.on(AttackEntityListener.EVENT, active::onAttackEntity);
        builder.on(UseBlockListener.EVENT, active::onUseBlock);
        builder.on(UseItemListener.EVENT, active::onUseItem);

        builder.on(ExplosionListener.EVENT, (game, affectedBlocks) -> {
            affectedBlocks.removeIf(map::isProtectedBlock);
        });

        return builder.build();
    }

    private void addPlayers(Multimap<GameTeam, ServerPlayerEntity> players) {
        players.forEach((team, player) -> {
            this.participants.put(player.getUuid(), new BwParticipant(this, player, team));
        });

        for (GameTeam team : this.config.getTeams()) {
            List<BwParticipant> participants = this.participantsFor(team).collect(Collectors.toList());

            if (!participants.isEmpty()) {
                TeamState teamState = new TeamState(team);
                participants.forEach(participant -> {
                    teamState.players.add(participant.playerId);
                });

                this.teams.put(team, teamState);
            }
        }
    }

    private void open(Game game) {
        this.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) {
                return;
            }

            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.spawnPlayer(player, spawn);
            } else {
                BedWars.LOGGER.warn("No spawn for player {}", participant.playerId);

                BedWars.resetPlayer(player, GameMode.SPECTATOR);
                this.map.spawnAtCenter(player);
            }
        });

        this.map.spawnShopkeepers(this, this.config);
        this.triggerModifiers(BwGameTriggers.GAME_RUNNING);

        this.startTime = game.getWorld().getTime();
    }

    private void addPlayer(Game game, ServerPlayerEntity player) {
        // player has already joined the game
        if (this.isParticipant(player)) {
            return;
        }

        BedWars.resetPlayer(player, GameMode.SPECTATOR);
        player.getEnderChestInventory().clear();

        this.map.spawnAtCenter(player);
    }

    private void rejoinPlayer(Game game, ServerPlayerEntity player) {
        BwParticipant participant = this.getParticipant(player);

        if (participant != null) {
            BwMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.playerLogic.respawnOnTimer(player, spawn);
            } else {
                BedWars.resetPlayer(player, GameMode.SPECTATOR);
                this.map.spawnAtCenter(player);
            }

            this.scoreboard.markDirty();
        }
    }

    private boolean onBreakBlock(Game game, ServerPlayerEntity player, BlockPos pos) {
        if (this.map.contains(pos) && this.map.isProtectedBlock(pos)) {
            for (GameTeam team : this.config.getTeams()) {
                BlockBounds bed = this.map.getTeamRegions(team).bed;
                if (bed != null && bed.contains(pos)) {
                    this.teamLogic.onBedBroken(player, pos);
                }
            }

            return true;
        }

        return false;
    }

    private ActionResult onAttackEntity(Game game, ServerPlayerEntity player, Hand hand, Entity entity, EntityHitResult hitResult) {
        BwParticipant participant = this.getParticipant(player);
        BwParticipant attackedParticipant = this.getParticipant(entity.getUuid());
        if (participant != null && attackedParticipant != null) {
            if (participant.team == attackedParticipant.team) {
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseBlock(Game game, ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        if (pos == null) {
            return ActionResult.PASS;
        }

        BwParticipant participant = this.getParticipant(player);
        if (participant != null) {
            if (this.map.contains(pos)) {
                if (player.getStackInHand(hand).getItem() == Items.FIRE_CHARGE) {
                    return ActionResult.PASS;
                }

                BlockState state = this.map.getWorld().getBlockState(pos);
                if (state.getBlock() instanceof AbstractChestBlock) {
                    return this.onUseChest(participant, pos);
                } else if (state.getBlock().isIn(BlockTags.BEDS)) {
                    return ActionResult.CONSUME;
                }
            } else {
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseChest(BwParticipant participant, BlockPos pos) {
        GameTeam team = participant.team;

        GameTeam chestTeam = this.getOwningTeamForChest(pos);
        if (chestTeam == null || chestTeam.equals(team)) {
            return ActionResult.PASS;
        }

        BwActive.TeamState chestTeamState = this.getTeam(chestTeam);
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

    private TypedActionResult<ItemStack> onUseItem(Game game, ServerPlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isEmpty()) {
            return TypedActionResult.pass(ItemStack.EMPTY);
        }

        if (stack.getItem() == Items.FIRE_CHARGE) {
            return this.onUseFireball(player, stack);
        } else if (CustomItem.match(stack) == BwCustomItems.BRIDGE_EGG) {
            return this.onUseBridgeEgg(player, stack);
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    private TypedActionResult<ItemStack> onUseFireball(ServerPlayerEntity player, ItemStack stack) {
        ServerWorld world = this.map.getWorld();

        Vec3d dir = player.getRotationVec(1.0F);

        BwFireballEntity fireball = new BwFireballEntity(world, player, dir.x * 0.5, dir.y * 0.5, dir.z * 0.5);
        fireball.explosionPower = 2;
        fireball.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireball.getZ() + dir.z);

        world.spawnEntity(fireball);

        player.getItemCooldownManager().set(Items.FIRE_CHARGE, 20);
        stack.decrement(1);

        return TypedActionResult.success(ItemStack.EMPTY);
    }

    private TypedActionResult<ItemStack> onUseBridgeEgg(ServerPlayerEntity player, ItemStack stack) {
        ServerWorld world = this.map.getWorld();

        world.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS,
                0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F)
        );

        // Get player wool color
        GameTeam team = this.getTeam(player.getUuid());
        if (team == null) {
            return TypedActionResult.pass(stack);
        }

        BlockState state = ColoredBlocks.wool(team.getDye()).getDefaultState();

        // Spawn egg
        BridgeEggEntity eggEntity = new BridgeEggEntity(world, player, state);
        eggEntity.setItem(stack);
        eggEntity.setProperties(player, player.pitch, player.yaw, 0.0F, 1.5F, 1.0F);

        world.spawnEntity(eggEntity);

        if (!player.abilities.creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.consume(stack);
    }

    private void tick(Game game) {
        if (this.winningTeam != null) {
            if (this.tickClosing()) {
                game.close();
            }
            return;
        }

        // TODO: this should be modular
        if (!this.destroyedBeds) {
            long time = game.getWorld().getTime();
            if (time - this.startTime > 20 * 60 * 20) {
                this.destroyedBeds = true;

                for (GameTeam team : this.config.getTeams()) {
                    this.teamLogic.removeBed(team);
                }

                this.broadcast.broadcast(this.broadcast.everyone(), new LiteralText("Destroyed all beds!").formatted(Formatting.RED));
                this.broadcast.broadcastSound(this.broadcast.everyone(), SoundEvents.BLOCK_END_PORTAL_SPAWN);
            }
        }

        BwWinStateLogic.WinResult winResult = this.tickActive();
        if (winResult != null) {
            this.winningTeam = winResult.getTeam();
            this.closeTime = this.map.getWorld().getTime() + CLOSE_TICKS;
        }
    }

    @Nullable
    private BwWinStateLogic.WinResult tickActive() {
        ServerWorld world = this.map.getWorld();
        long time = world.getTime();

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
        for (GameModifier modifier : this.config.getModifiers()) {
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

        return this.map.getWorld().getTime() >= this.closeTime;
    }

    private void spawnFireworks(GameTeam team) {
        ServerWorld world = this.map.getWorld();
        Random random = world.random;

        if (random.nextInt(18) == 0) {
            List<ServerPlayerEntity> players = this.players().collect(Collectors.toList());
            ServerPlayerEntity player = players.get(random.nextInt(players.size()));

            int flight = random.nextInt(3);
            FireworkItem.Type type = random.nextInt(4) == 0 ? FireworkItem.Type.STAR : FireworkItem.Type.BURST;
            FireworkRocketEntity firework = new FireworkRocketEntity(
                    world,
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    team.createFirework(flight, type)
            );

            world.spawnEntity(firework);
        }
    }

    private boolean onPlayerDeath(Game game, ServerPlayerEntity player, DamageSource source) {
        BwParticipant participant = this.getParticipant(player);

        // TODO: cancel if cause is own player
        if (participant != null) {
            this.killLogic.onPlayerDeath(participant, player, source);
            return true;
        }

        return false;
    }

    private void close(Game game) {
        if (this.closed) {
            return;
        }

        this.closed = true;
        this.scoreboard.close();

        // TODO: async?
        this.map.delete();
    }

    public ItemStack createArmor(ItemStack stack) {
        return ItemUtil.unbreakable(stack);
    }

    public ItemStack createTool(ItemStack stack) {
        stack = ItemUtil.unbreakable(stack);
        if (this.config.getCombatConfig().isOldMechanics()) {
            stack = OldCombat.applyTo(stack);
        }

        return stack;
    }

    public void triggerModifiers(GameTrigger type) {
        for (GameModifier modifier : this.config.getModifiers()) {
            if (modifier.getTrigger() == type) {
                modifier.init(this);
            }
        }
    }

    @Nullable
    public BwParticipant getParticipant(PlayerEntity player) {
        return this.participants.get(player.getUuid());
    }

    @Nullable
    public GameTeam getTeam(UUID id) {
        BwParticipant participant = this.participants.get(id);
        if (participant != null) {
            return participant.team;
        }
        return null;
    }

    @Nullable
    public BwParticipant getParticipant(UUID id) {
        return this.participants.get(id);
    }

    public boolean isParticipant(PlayerEntity player) {
        return this.participants.containsKey(player.getUuid());
    }

    public Stream<BwParticipant> participantsFor(GameTeam team) {
        return this.participants.values().stream().filter(participant -> participant.team == team);
    }

    public Stream<BwParticipant> participants() {
        return this.participants.values().stream();
    }

    public Stream<ServerPlayerEntity> players() {
        return this.participants().map(BwParticipant::player).filter(Objects::nonNull);
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

        final Set<UUID> players = new HashSet<>();
        final GameTeam team;
        boolean hasBed = true;
        boolean eliminated;

        public boolean trapSet;
        public boolean healPool;
        public boolean hasteEnabled;
        public int swordSharpness;
        public int armorProtection;

        TeamState(GameTeam team) {
            this.team = team;
        }
    }
}
