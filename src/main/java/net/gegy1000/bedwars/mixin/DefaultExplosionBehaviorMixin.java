package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.BedWars;
import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.DefaultExplosionBehavior;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DefaultExplosionBehavior.class)
public class DefaultExplosionBehaviorMixin {
    private static final Optional<Float> GLASS_RESISTANCE = Optional.of(99999.0F);

    @Inject(method = "getBlastResistance", at = @At("HEAD"), cancellable = true)
    private void getBlastResistance(
            Explosion explosion, BlockView blockView,
            BlockPos pos, BlockState block, FluidState fluid,
            CallbackInfoReturnable<Optional<Float>> ci
    ) {
        if (blockView instanceof WorldAccess) {
            World world = ((WorldAccess) blockView).getWorld();

            GameWorld gameWorld = GameWorld.forWorld(world);
            if (gameWorld != null) {
                RuleResult result = gameWorld.testRule(BedWars.BLAST_PROOF_GLASS_RULE);
                if (result == RuleResult.ALLOW) {
                    if (block.getBlock() instanceof AbstractGlassBlock) {
                        ci.setReturnValue(GLASS_RESISTANCE);
                    }
                }
            }
        }
    }
}
