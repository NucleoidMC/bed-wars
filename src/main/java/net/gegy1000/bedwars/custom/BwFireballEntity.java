package net.gegy1000.bedwars.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public final class BwFireballEntity extends FireballEntity {
    public BwFireballEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ) {
        super(world, owner, velocityX, velocityY, velocityZ);
    }

    @Override
    protected void onCollision(HitResult result) {
        HitResult.Type type = result.getType();
        if (type == HitResult.Type.BLOCK) {
            this.onBlockHit((BlockHitResult) result);
        }

        if (!this.world.isClient) {
            this.world.createExplosion(
                    null, this.getX(), this.getY(), this.getZ(),
                    this.explosionPower,
                    false,
                    Explosion.DestructionType.DESTROY
            );

            this.remove();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }
}
