package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public class MixinExplosion {
    @Final
    @Shadow
    private double x;
    @Final
    @Shadow
    private double y;
    @Final
    @Shadow
    private double z;

    @Final
    @Shadow
    private List<BlockPos> affectedBlocks;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void affectWorld(boolean blocks, CallbackInfo ci) {
        BedWars game = GameManager.activeFor(BedWars.GAME);
        if (game != null) {
            game.onExplosion(this.affectedBlocks);
        }
    }

    @Inject(method = "affectWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/world/explosion/Explosion;createFire:Z"), cancellable = true)
    private void createFire(boolean blocks, CallbackInfo ci) {
        BedWars game = GameManager.activeFor(BedWars.GAME);
        if (game != null && game.map.contains(new BlockPos(this.x, this.y, this.z))) {
            ci.cancel();
        }
    }
}
