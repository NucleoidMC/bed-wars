package xyz.nucleoid.bedwars.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileEntity {
    @Shadow
    private boolean dealtDamage;

    private TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (!this.dealtDamage && this.getY() <= 0.0) {
            GameWorld gameWorld = GameWorld.forWorld(this.world);
            if (gameWorld != null && gameWorld.testRule(BedWars.TRIDENTS_LOYAL_IN_VOID) == RuleResult.ALLOW) {
                this.dealtDamage = true;
                this.setVelocity(0.0, 0.0, 0.0);
            }
        }
    }
}
