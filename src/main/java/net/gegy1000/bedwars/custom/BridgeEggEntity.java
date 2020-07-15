package net.gegy1000.bedwars.custom;

import net.gegy1000.bedwars.game.BedWars;
import net.gegy1000.gl.game.GameManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BridgeEggEntity extends EggEntity {
    private BlockState trailBlock;

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

        BedWars game = GameManager.openFor(BedWars.TYPE);
        if (game != null) {
            BlockPos pos = this.getBlockPos().down();
            if (game.map.contains(pos)) {
                if (this.world.getBlockState(pos).isAir()) {
                    this.world.setBlockState(pos, this.trailBlock);
                }
            }
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
