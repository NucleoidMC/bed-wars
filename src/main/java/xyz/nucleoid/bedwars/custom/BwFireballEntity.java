package xyz.nucleoid.bedwars.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public final class BwFireballEntity extends FireballEntity {
    public BwFireballEntity(World world, LivingEntity owner, double velocityX, double velocityY, double velocityZ, int explosionPower) {
        super(world, owner, velocityX, velocityY, velocityZ, explosionPower);
    }

    @Override
    protected void onCollision(HitResult result) {
        HitResult.Type type = result.getType();
        if (type == HitResult.Type.BLOCK) {
            this.onBlockHit((BlockHitResult) result);
        }

        if (!this.getWorld().isClient()) {
            Explosion explosion = new Explosion(this.getWorld(), this, null, null, this.getX(), this.getY(), this.getZ(), this.explosionPower, false, Explosion.DestructionType.DESTROY, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.ENTITY_GENERIC_EXPLODE);
            explosion.collectBlocksAndDamageEntities();
            explosion.affectWorld(true);

            this.discard();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }
}
