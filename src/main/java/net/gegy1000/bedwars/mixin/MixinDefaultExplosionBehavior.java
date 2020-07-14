package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.explosion.DefaultExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DefaultExplosionBehavior.class)
public class MixinDefaultExplosionBehavior {
    private static final Optional<Float> GLASS_RESISTANCE = Optional.of(99999.0F);

    @Inject(method = "getBlastResistance", at = @At("HEAD"), cancellable = true)
    private void getBlastResistance(
            Explosion explosion, BlockView world,
            BlockPos pos, BlockState block, FluidState fluid,
            CallbackInfoReturnable<Optional<Float>> ci
    ) {
        BedWars game = GameManager.openFor(BedWars.TYPE);
        if (game != null && game.map.contains(pos)) {
            if (block.getBlock() instanceof StainedGlassBlock) {
                ci.setReturnValue(GLASS_RESISTANCE);
            }
        }
    }
}
