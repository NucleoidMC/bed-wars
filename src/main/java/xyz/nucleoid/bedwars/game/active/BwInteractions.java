package xyz.nucleoid.bedwars.game.active;

import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.custom.BridgeEggEntity;
import xyz.nucleoid.bedwars.custom.BwFireballEntity;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.custom.MovingCloud;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;

public final class BwInteractions {
    private final BwActive game;
    private final ServerLevel world;

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

    private EventResult onBreakBlock(ServerPlayer player, ServerLevel world, BlockPos pos) {
        if (this.game.map.isProtectedBlock(pos)) {
            for (var team : this.game.teams()) {
                var bed = this.game.map.getTeamRegions(team.key()).bed();
                if (bed != null && bed.contains(pos)) {
                    this.game.teamLogic.onBedBroken(player, pos);
                }
            }

            return EventResult.DENY;
        }

        if (this.treeChopper.onBreakBlock(player, world, pos)) {
            return EventResult.DENY;
        }

        return EventResult.PASS;
    }

    private InteractionResult onUseBlock(ServerPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();

        BwParticipant participant = this.game.participantBy(player);
        if (participant != null) {
            ItemStack heldStack = player.getItemInHand(hand);
            if (heldStack.getItem() == Items.FIRE_CHARGE) {
                this.onUseFireball(player, heldStack);
                return InteractionResult.SUCCESS;
            }

            BlockState state = this.world.getBlockState(pos);
            if (state.getBlock() instanceof AbstractChestBlock) {
                return this.onUseChest(player, participant, pos);
            } else if (state.is(BlockTags.BEDS)) {
                player.getItemInHand(hand).useOn(new BlockPlaceContext(player, hand, player.getItemInHand(hand), hitResult));

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private InteractionResult onUseChest(ServerPlayer player, BwParticipant participant, BlockPos pos) {
        GameTeam team = participant.team;

        GameTeam chestTeam = this.getOwningTeamForChest(pos);
        if (chestTeam == null || chestTeam.equals(team) || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        BwActive.TeamState chestTeamState = this.game.teamState(chestTeam.key());
        if (chestTeamState == null || chestTeamState.eliminated) {
            return InteractionResult.PASS;
        }

        player.displayClientMessage(Component.translatable("text.bedwars.cannot_open_chest").withStyle(ChatFormatting.RED), true);

        return InteractionResult.FAIL;
    }

    @Nullable
    private GameTeam getOwningTeamForChest(BlockPos pos) {
        for (var team : this.game.teams()) {
            BwMap.TeamRegions regions = this.game.map.getTeamRegions(team.key());
            if (regions.teamChest() != null && regions.teamChest().contains(pos)) {
                return team;
            }
        }
        return null;
    }

    private InteractionResult onUseItem(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (stack.getItem() == Items.FIRE_CHARGE) {
            return this.onUseFireball(player, stack);
        } else if (stack.getItem() == BwItems.BRIDGE_EGG) {
            return this.onUseBridgeEgg(player, stack);
        } else if (stack.getItem() == BwItems.MOVING_CLOUD) {
            return this.onUseMovingCloud(player, stack);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult onUseFireball(ServerPlayer player, ItemStack stack) {
        Vec3 dir = player.getViewVector(1.0F);

        BwFireballEntity fireball = new BwFireballEntity(this.world, player, dir.x * 0.5, dir.y * 0.5, dir.z * 0.5, 2);
        fireball.absSnapTo(player.getX() + dir.x, player.getEyeY() + dir.y, fireball.getZ() + dir.z);

        this.world.addFreshEntity(fireball);

        player.getCooldowns().addCooldown(stack, 20);
        stack.shrink(1);

        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(ItemStack.EMPTY);
    }

    private InteractionResult onUseBridgeEgg(ServerPlayer player, ItemStack stack) {
        this.world.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS,
                0.5F, 0.4F / (this.world.random.nextFloat() * 0.4F + 0.8F)
        );

        // Get player wool color
        GameTeam team = this.game.teamFor(PlayerRef.of(player));
        if (team == null) {
            return InteractionResult.PASS;
        }

        BlockState state = ColoredBlocks.wool(team.config().blockDyeColor()).defaultBlockState();

        // Spawn egg
        BridgeEggEntity eggEntity = new BridgeEggEntity(this.world, player, state);
        eggEntity.setItem(stack);
        eggEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);

        this.world.addFreshEntity(eggEntity);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult onUseMovingCloud(ServerPlayer player, ItemStack stack) {
        this.world.playSound(
                null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EGG_THROW, SoundSource.PLAYERS,
                0.5F, 0.4F / (this.world.random.nextFloat() * 0.4F + 0.8F)
        );

        Direction direction = player.getDirection();
        BlockPos blockPos = player.blockPosition().below().relative(direction);
        if (!this.world.isEmptyBlock(blockPos)) {
            return InteractionResult.PASS;
        }

        MovingCloud cloud = new MovingCloud(this.world, blockPos, direction);
        this.game.movingClouds.add(cloud);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
