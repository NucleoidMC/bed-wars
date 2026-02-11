package xyz.nucleoid.bedwars.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;

// TODO: collision needs fixing
public class BridgeEggEntity extends ThrownEgg {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final BlockState trailBlock;

    public BridgeEggEntity(ServerLevel world, LivingEntity thrower, BlockState trailBlock) {
        super(world, thrower, new ItemStack(Items.EGG));
        this.trailBlock = trailBlock;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        var game = GameSpaceManager.get().byWorld(this.level());
        if (game == null) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        BlockPos pos = this.blockPosition().below();
        this.tryPlaceAt(pos);

        for (Direction direction : DIRECTIONS) {
            if (this.random.nextInt(3) != 0) {
                this.tryPlaceAt(pos.offset(direction.getUnitVec3i()));
            }
        }
    }

    private void tryPlaceAt(BlockPos pos) {
        if (this.level().getBlockState(pos).isAir()) {
            this.level().setBlock(pos, this.trailBlock, 3);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        // ignore self-collisions
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            if (this.level().getBlockState(((BlockHitResult) hitResult).getBlockPos()) == this.trailBlock) {
                return;
            }
        }

        super.onHit(hitResult);
    }
}
