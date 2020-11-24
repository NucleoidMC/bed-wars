package xyz.nucleoid.bedwars.mixin;

import net.minecraft.block.AbstractGlassBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import java.util.Optional;

@Mixin(ExplosionBehavior.class)
public class ExplosionBehaviorMixin {
    private static final Optional<Float> GLASS_RESISTANCE = Optional.of(99999.0F);

    @Inject(method = "getBlastResistance", at = @At("HEAD"), cancellable = true)
    private void getBlastResistance(
            Explosion explosion, BlockView blockView,
            BlockPos pos, BlockState block, FluidState fluid,
            CallbackInfoReturnable<Optional<Float>> ci
    ) {
        if (blockView instanceof ServerWorldAccess) {
            ServerWorld world = ((ServerWorldAccess) blockView).toServerWorld();

            ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
            if (gameSpace != null) {
                RuleResult result = gameSpace.testRule(BedWars.BLAST_PROOF_GLASS_RULE);
                if (result == RuleResult.ALLOW) {
                    if (block.getBlock() instanceof AbstractGlassBlock) {
                        ci.setReturnValue(GLASS_RESISTANCE);
                    }
                }
            }
        }
    }
}
