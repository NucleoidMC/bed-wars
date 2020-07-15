package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.game.modifiers.BwGameTriggers;
import net.gegy1000.gl.item.CustomItem;
import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.gl.game.GameTeam;
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
import net.minecraft.server.network.ServerPlayerEntity;
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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public final class BwEvents {
    private final BedWars game;

    public BwEvents(BedWars game) {
        this.game = game;
    }

    public void onExplosion(List<BlockPos> affectedBlocks) {
        affectedBlocks.removeIf(this.game.map::isStandardBlock);
    }

    public boolean onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        if (this.game.state == null) {
            if (this.game.waiting.containsPlayer(player)) {
                this.game.waiting.spawnPlayer(player);
                return true;
            }
            return false;
        }

        BwState.Participant participant = this.game.state.getParticipant(player);

        // TODO: this should go in KillLogic?

        // TODO: cancel if cause is own player
        if (participant != null) {
            this.game.killLogic.onPlayerDeath(player, source);

            BwMap.TeamSpawn spawn = this.game.teamLogic.tryRespawn(participant);
            this.game.broadcast.broadcastDeath(player, source, spawn == null);

            // Run death modifiers
            this.game.triggerModifiers(BwGameTriggers.PLAYER_DEATH);

            if (spawn != null) {
                this.game.playerLogic.respawnOnTimer(player, spawn);
            } else {
                this.dropEnderChest(player, participant);

                this.game.playerLogic.spawnSpectator(player);
                this.game.winStateLogic.eliminatePlayer(participant);

                // Run final death modifiers
                this.game.triggerModifiers(BwGameTriggers.FINAL_DEATH);
            }

            this.game.scoreboardLogic.markDirty();

            return true;
        }

        return false;
    }

    private void dropEnderChest(ServerPlayerEntity player, BwState.Participant participant) {
        EnderChestInventory enderChest = player.getEnderChestInventory();

        BwMap.TeamRegions teamRegions = this.game.map.getTeamRegions(participant.team);
        if (teamRegions.spawn != null) {
            Vec3d dropSpawn = teamRegions.spawn.getCenter();

            for (int slot = 0; slot < enderChest.size(); slot++) {
                ItemStack stack = enderChest.removeStack(slot);
                if (!stack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(this.game.world, dropSpawn.x, dropSpawn.y + 0.5, dropSpawn.z, stack);
                    this.game.world.spawnEntity(itemEntity);
                }
            }
        }

        enderChest.clear();
    }

    public void onPlayerJoin(ServerPlayerEntity player) {
        if (this.game.state == null) {
            return;
        }

        BwState.Participant participant = this.game.state.getParticipant(player);

        if (participant != null) {
            BwMap.TeamSpawn spawn = this.game.teamLogic.tryRespawn(participant);
            if (spawn != null) {
                this.game.playerLogic.respawnOnTimer(player, spawn);
            } else {
                this.game.playerLogic.spawnSpectator(player);
            }

            this.game.scoreboardLogic.markDirty();
        }
    }

    public boolean onBreakBlock(ServerPlayerEntity player, BlockPos pos) {
        if (this.game.map.contains(pos)) {
            BlockState state = this.game.world.getBlockState(pos);

            if (this.game.map.isStandardBlock(pos)) {
                if (state.getBlock().isIn(BlockTags.BEDS)) {
                    this.game.teamLogic.onBedBroken(player, pos);
                }

                return true;
            }
        }

        return false;
    }

    public ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (this.game.state == null) {
            return ActionResult.PASS;
        }

        BwState.Participant participant = this.game.state.getParticipant(player);
        BwState.Participant attackedParticipant = this.game.state.getParticipant(entity.getUuid());
        if (participant != null && attackedParticipant != null) {
            if (participant.team == attackedParticipant.team) {
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    public ActionResult onUseBlock(PlayerEntity player, BlockHitResult hitResult) {
        if (this.game.state == null) {
            return ActionResult.PASS;
        }

        BwState.Participant participant = this.game.state.getParticipant(player);

        if (participant != null) {
            BlockPos pos = hitResult.getBlockPos();
            if (pos != null) {
                if (this.game.map.contains(pos)) {
                    BlockState state = this.game.world.getBlockState(pos);
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

        BwState.TeamState chestTeamState = this.game.state.getTeam(chestTeam);
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
        for (GameTeam team : this.game.config.getTeams()) {
            BwMap.TeamRegions regions = this.game.map.getTeamRegions(team);
            if (regions.teamChest != null && regions.teamChest.contains(pos)) {
                return team;
            }
        }
        return null;
    }

    public TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isEmpty() || world.isClient()) {
            return TypedActionResult.pass(ItemStack.EMPTY);
        }

        if (stack.getItem() == Items.FIRE_CHARGE) {
            Vec3d dir = player.getRotationVec(1.0F);

            FireballEntity fireball = new FireballEntity(world, player, dir.x * 0.5, dir.y * 0.5, dir.z * 0.5);
            fireball.explosionPower = 2;
            fireball.updatePosition(player.getX() + dir.x, player.getEyeY() + dir.y, fireball.getZ() + dir.z);

            world.spawnEntity(fireball);

            player.getItemCooldownManager().set(Items.FIRE_CHARGE, 20);
            stack.decrement(1);

            return TypedActionResult.success(ItemStack.EMPTY);
        } else if (CustomItem.match(stack) == BwCustomItems.TEAM_SELECTOR) {
            if (this.game.waiting != null) {
                this.game.waiting.onUseRequestTeam((ServerPlayerEntity) player, stack);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }
}
