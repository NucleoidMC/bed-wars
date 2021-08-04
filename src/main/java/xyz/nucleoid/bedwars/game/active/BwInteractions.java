package xyz.nucleoid.bedwars.game.active;

import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.custom.BridgeEggEntity;
import xyz.nucleoid.bedwars.custom.BwFireballEntity;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.custom.MovingCloud;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.util.ColoredBlocks;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;

public final class BwInteractions {
    private final BwActive game;
    private final ServerWorld world;

    private final BwTreeChopper treeChopper = new BwTreeChopper();

    public BwInteractions(BwActive game) {
        this.game = game;
        this.world = game.world;
    }

    public void addTo(GameActivity activity) {
        activity.listen(BlockBreakEvent.EVENT, this::onBreakBlock);
        activity.listen(BlockUseEvent.EVENT, this::onUseBlock);
        activity.listen(ItemUseEvent.EVENT, this::onUseItem);
    }

    private ActionResult onBreakBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (this.game.map.isProtectedBlock(pos)) {
            for (GameTeam team : this.game.config.teams()) {
                BlockBounds bed = this.game.map.getTeamRegions(team).bed();
                if (bed != null && bed.contains(pos)) {
                    this.game.teamLogic.onBedBroken(player, pos);
                }
            }

            return ActionResult.FAIL;
        }

        if (this.treeChopper.onBreakBlock(player, world, pos)) {
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        if (pos == null) {
            return ActionResult.PASS;
        }

        BwParticipant participant = this.game.getParticipant(player);
        if (participant != null) {
            ItemStack heldStack = player.getStackInHand(hand);
            if (heldStack.getItem() == Items.FIRE_CHARGE) {
                this.onUseFireball(player, heldStack);
                return ActionResult.SUCCESS;
            }

            BlockState state = this.world.getBlockState(pos);
            if (state.getBlock() instanceof AbstractChestBlock) {
                return this.onUseChest(player, participant, pos);
            } else if (state.isIn(BlockTags.BEDS)) {
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

        BwActive.TeamState chestTeamState = this.game.getTeam(chestTeam);
        if (chestTeamState == null || chestTeamState.eliminated) {
            return ActionResult.PASS;
        }

        player.sendMessage(new TranslatableText("text.bedwars.cannot_open_chest").formatted(Formatting.RED), true);

        return ActionResult.FAIL;
    }

    @Nullable
    private GameTeam getOwningTeamForChest(BlockPos pos) {
        for (GameTeam team : this.game.config.teams()) {
            BwMap.TeamRegions regions = this.game.map.getTeamRegions(team);
            if (regions.teamChest() != null && regions.teamChest().contains(pos)) {
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

        BwFireballEntity fireball = new BwFireballEntity(this.world, player, dir.x * 0.5, dir.y * 0.5, dir.z * 0.5, 2);
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
        GameTeam team = this.game.getTeam(PlayerRef.of(player));
        if (team == null) {
            return TypedActionResult.pass(stack);
        }

        BlockState state = ColoredBlocks.wool(team.blockDyeColor()).getDefaultState();

        // Spawn egg
        BridgeEggEntity eggEntity = new BridgeEggEntity(this.world, player, state);
        eggEntity.setItem(stack);
        eggEntity.setProperties(player, player.getPitch(), player.getYaw(), 0.0F, 1.5F, 1.0F);

        this.world.spawnEntity(eggEntity);

        if (!player.getAbilities().creativeMode) {
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
        this.game.movingClouds.add(cloud);

        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.consume(stack);
    }
}
