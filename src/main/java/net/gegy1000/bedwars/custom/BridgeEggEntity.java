package net.gegy1000.bedwars.custom;

import net.gegy1000.plasmid.game.GameWorld;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BridgeEggEntity extends EggEntity {
    private static final Direction[] DIRECTIONS = Direction.values();

    private final BlockState trailBlock;

    public BridgeEggEntity(World world, LivingEntity thrower, BlockState trailBlock) {
        super(world, thrower);
        this.trailBlock = trailBlock;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient()) {
            return;
        }

        GameWorld game = GameWorld.forWorld(this.world);
        if (game == null) {
            this.remove();
            return;
        }

        BlockPos pos = this.getBlockPos().down();
        this.tryPlaceAt(pos);

        for (Direction direction : DIRECTIONS) {
            if (this.random.nextInt(3) != 0) {
                this.tryPlaceAt(pos.offset(direction));
            }
        }
    }

    private void tryPlaceAt(BlockPos pos) {
        if (this.world.getBlockState(pos).isAir()) {
            this.world.setBlockState(pos, this.trailBlock);
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        // ignore self-collisions
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            if (this.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()) == this.trailBlock) {
                return;
            }
        }

        super.onCollision(hitResult);
    }
}
