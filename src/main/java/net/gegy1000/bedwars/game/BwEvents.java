package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.bedwars.game.active.BwParticipant;
import net.gegy1000.bedwars.game.active.modifiers.BwGameTriggers;
import net.gegy1000.bedwars.game.active.upgrade.UpgradeType;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.item.CustomItem;
import net.gegy1000.gl.world.BlockBounds;
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
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public final class BwEvents {
    private final BedWars game;

    public BwEvents(BedWars game) {
        this.game = game;
    }

    public void onExplosion(List<BlockPos> affectedBlocks) {
        affectedBlocks.removeIf(this.game.map::isProtectedBlock);
    }

    public boolean onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        BwPhase phase = this.game.phase;
        if (phase instanceof BwWaiting) {
            BwWaiting waiting = (BwWaiting) phase;

            if (waiting.containsPlayer(player)) {
                waiting.spawnPlayer(player);
                return true;
            }

            return false;
        }

        if (phase instanceof BwActive) {
            BwActive active = (BwActive) phase;
            BwParticipant participant = active.getParticipant(player);

            // TODO: this should go in KillLogic?

            // TODO: cancel if cause is own player
            if (participant != null) {
                participant.upgrades.tryDowngrade(UpgradeType.SWORD);
                participant.upgrades.tryDowngrade(UpgradeType.PICKAXE);
                participant.upgrades.tryDowngrade(UpgradeType.AXE);

                active.killLogic.onPlayerDeath(player, source);

                BwMap.TeamSpawn spawn = active.teamLogic.tryRespawn(participant);
                active.broadcast.broadcastDeath(player, source, spawn == null);

                // Run death modifiers
                active.triggerModifiers(BwGameTriggers.PLAYER_DEATH);

                if (spawn != null) {
                    active.playerLogic.respawnOnTimer(player, spawn);
                } else {
                    this.dropEnderChest(player, participant);

                    active.playerTracker.spawnAtCenter(player, GameMode.SPECTATOR);
                    active.winStateLogic.eliminatePlayer(participant);

                    // Run final death modifiers
                    active.triggerModifiers(BwGameTriggers.FINAL_DEATH);
                }

                active.scoreboard.markDirty();

                return true;
            }
        }

        return false;
    }

    private void dropEnderChest(ServerPlayerEntity player, BwParticipant participant) {
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
        BwPhase phase = this.game.phase;

        if (phase instanceof BwActive) {
            BwActive active = (BwActive) phase;
            BwParticipant participant = active.getParticipant(player);

            if (participant != null) {
                BwMap.TeamSpawn spawn = active.teamLogic.tryRespawn(participant);
                if (spawn != null) {
                    active.playerLogic.respawnOnTimer(player, spawn);
                } else {
                    active.playerTracker.spawnAtCenter(player, GameMode.SPECTATOR);
                }

                active.scoreboard.markDirty();
            }
        }
    }

    public boolean onBreakBlock(ServerPlayerEntity player, BlockPos pos) {
        BwPhase phase = this.game.phase;

        if (phase instanceof BwActive) {
            BwActive active = (BwActive) phase;

            if (this.game.map.contains(pos)) {
                for (GameTeam team : this.game.config.getTeams()) {
                    BlockBounds bed = this.game.map.getTeamRegions(team).bed;
                    if (bed != null && bed.contains(pos)) {
                        active.teamLogic.onBedBroken(player, pos);
                    }
                }

                return true;
            }
        }

        return false;
    }

    public ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        BwPhase phase = this.game.phase;

        if (phase instanceof BwActive) {
            BwActive active = (BwActive) phase;

            BwParticipant participant = active.getParticipant(player);
            BwParticipant attackedParticipant = active.getParticipant(entity.getUuid());
            if (participant != null && attackedParticipant != null) {
                if (participant.team == attackedParticipant.team) {
                    return ActionResult.FAIL;
                }
            }
        }

        return ActionResult.PASS;
    }

    public ActionResult onUseBlock(PlayerEntity player, BlockHitResult hitResult) {
        BwPhase phase = this.game.phase;

        if (phase instanceof BwActive) {
            BwActive active = (BwActive) phase;
            BwParticipant participant = active.getParticipant(player);

            if (participant != null) {
                BlockPos pos = hitResult.getBlockPos();
                if (pos != null) {
                    if (this.game.map.contains(pos)) {
                        BlockState state = this.game.world.getBlockState(pos);
                        if (state.getBlock() instanceof AbstractChestBlock) {
                            return this.onUseChest(active, participant, pos);
                        }
                    } else {
                        return ActionResult.FAIL;
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    private ActionResult onUseChest(BwActive active, BwParticipant participant, BlockPos pos) {
        GameTeam team = participant.team;

        GameTeam chestTeam = this.getOwningTeamForChest(pos);
        if (chestTeam == null || chestTeam.equals(team)) {
            return ActionResult.PASS;
        }

        BwActive.TeamState chestTeamState = active.getTeam(chestTeam);
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
            BwPhase phase = this.game.phase;

            if (phase instanceof BwWaiting) {
                BwWaiting waiting = (BwWaiting) phase;
                waiting.onUseRequestTeam((ServerPlayerEntity) player, stack);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }
}
