package xyz.nucleoid.bedwars.game.active;

import com.google.common.collect.Multimap;
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
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.custom.BridgeEggEntity;
import xyz.nucleoid.bedwars.custom.BwFireballEntity;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.game.BwConfig;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.BwSpawnLogic;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.bedwars.game.active.modifiers.GameModifier;
import xyz.nucleoid.bedwars.game.active.modifiers.GameTrigger;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.event.AttackEntityListener;
import xyz.nucleoid.plasmid.game.event.BreakBlockListener;
import xyz.nucleoid.plasmid.game.event.ExplosionListener;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.event.UseItemListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.logic.combat.OldCombat;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;
import xyz.nucleoid.plasmid.util.PlayerRef;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BwActive {
    public static final int RESPAWN_TIME_SECONDS = 5;
    public static final long RESPAWN_TICKS = 20 * RESPAWN_TIME_SECONDS;
    public static final long CLOSE_TICKS = 10 * 20;
    public static final int BED_GONE_TICKS = 20 * 60 * 20;

    public final ServerWorld world;
    public final GameWorld gameWorld;

    public final BwMap map;
    public final BwConfig config;

    private final Map<PlayerRef, BwParticipant> participants = new HashMap<>();
    private final Map<GameTeam, TeamState> teams = new HashMap<>();

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

    private BwActive(GameWorld gameWorld, BwMap map, BwConfig config) {
        this.world = gameWorld.getWorld();
        this.gameWorld = gameWorld;

        this.map = map;
        this.config = config;

        this.scoreboard = gameWorld.addResource(BwScoreboard.create(this));

        this.broadcast = new BwBroadcast(this);
        this.teamLogic = new BwTeamLogic(this);
        this.killLogic = new BwKillLogic(this);
        this.winStateLogic = new BwWinStateLogic(this);
        this.mapLogic = new BwMapLogic(this);
        this.playerLogic = new BwPlayerLogic(this);
        this.spawnLogic = new BwSpawnLogic(this.world, map);
        this.bar = gameWorld.addResource(new BwBar(gameWorld));
    }

    public static void open(GameWorld gameWorld, BwMap map, BwConfig config, Multimap<GameTeam, ServerPlayerEntity> players) {
        BwActive active = new BwActive(gameWorld, map, config);
        active.addPlayers(players);

        for (GameTeam team : config.teams) {
            active.scoreboard.addTeam(team);
        }

        gameWorld.openGame(game -> {
            game.setRule(GameRule.PORTALS, RuleResult.DENY);
            game.setRule(GameRule.PVP, RuleResult.ALLOW);
            game.setRule(GameRule.UNSTABLE_TNT, RuleResult.ALLOW);
            game.setRule(GameRule.CRAFTING, RuleResult.DENY);
            game.setRule(GameRule.FALL_DAMAGE, RuleResult.ALLOW);
            game.setRule(GameRule.HUNGER, RuleResult.DENY);
            game.setRule(GameRule.TEAM_CHAT, RuleResult.ALLOW);
            game.setRule(BedWars.BLAST_PROOF_GLASS_RULE, RuleResult.ALLOW);

            game.on(GameOpenListener.EVENT, active::onOpen);

            game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
            game.on(PlayerAddListener.EVENT, active::addPlayer);

            game.on(GameTickListener.EVENT, active::tick);

            game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);

            game.on(BreakBlockListener.EVENT, active::onBreakBlock);
            game.on(AttackEntityListener.EVENT, active::onAttackEntity);
            game.on(UseBlockListener.EVENT, active::onUseBlock);
            game.on(UseItemListener.EVENT, active::onUseItem);

            game.on(ExplosionListener.EVENT, affectedBlocks -> {
                affectedBlocks.removeIf(map::isProtectedBlock);
            });
        });
    }

    private void addPlayers(Multimap<GameTeam, ServerPlayerEntity> players) {
        players.forEach((team, player) -> {
            this.participants.put(PlayerRef.of(player), new BwParticipant(this, player, team));
        });

        for (GameTeam team : this.config.teams) {
            List<BwParticipant> participants = this.participantsFor(team).collect(Collectors.toList());

            if (!participants.isEmpty()) {
                TeamState teamState = new TeamState(team);
                participants.forEach(participant -> {
                    teamState.players.add(participant.playerRef);
                });

                this.teams.put(team, teamState);
            }
        }
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
                BedWars.LOGGER.warn("No spawn for player {}", participant.playerRef);
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

        return ActionResult.PASS;
    }

    private ActionResult onAttackEntity(ServerPlayerEntity attackerPlayer, Hand hand, Entity attackedEntity, EntityHitResult hitResult) {
        if (attackedEntity instanceof ServerPlayerEntity) {
            return this.onAttackPlayer(attackerPlayer, (ServerPlayerEntity) attackedEntity);
        }

        return ActionResult.PASS;
    }

    private ActionResult onAttackPlayer(ServerPlayerEntity attackerPlayer, ServerPlayerEntity attackedPlayer) {
        BwParticipant attackerParticipant = this.getParticipant(attackerPlayer);
        BwParticipant attackedParticipant = this.getParticipant(attackedPlayer);
        if (attackerParticipant == null || attackedParticipant == null) {
            return ActionResult.PASS;
        }

        if (attackerParticipant.team == attackedParticipant.team) {
            return ActionResult.FAIL;
        }

        attackedParticipant.lastAttack = AttackRecord.fromAttacker(attackerPlayer);

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

        player.sendMessage(new LiteralText("You cannot access this team's chest!").formatted(Formatting.RED), true);

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

    private void tick() {
        if (this.winningTeam != null) {
            if (this.tickClosing()) {
                this.gameWorld.close();
            }
            return;
        }

        long time = this.world.getTime();

        // TODO: this should be modular
        if (!this.destroyedBeds) {
            if (time - this.startTime > BED_GONE_TICKS) {
                this.destroyedBeds = true;

                for (GameTeam team : this.config.teams) {
                    this.teamLogic.removeBed(team);
                }

                this.broadcast.broadcast(this.broadcast.everyone(), new LiteralText("Destroyed all beds!").formatted(Formatting.RED));
                this.broadcast.broadcastSound(this.broadcast.everyone(), SoundEvents.BLOCK_END_PORTAL_SPAWN);
            }
        }

        if (time % 20 == 0) {
            long bedGoneTime = this.startTime + BED_GONE_TICKS;
            this.bar.update(bedGoneTime - time, BED_GONE_TICKS);
        }

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
            List<ServerPlayerEntity> players = this.players().collect(Collectors.toList());
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

        final Set<PlayerRef> players = new HashSet<>();
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
