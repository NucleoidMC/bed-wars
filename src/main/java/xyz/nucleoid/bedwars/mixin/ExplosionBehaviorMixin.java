package xyz.nucleoid.bedwars.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
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
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

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

            var gameSpace = GameSpaceManager.get().byWorld(world);
            if (gameSpace != null) {
                var result = gameSpace.getBehavior().testRule(BedWars.BLAST_PROOF_GLASS_RULE);
                if (result == EventResult.ALLOW) {
                    if (block.isOf(Blocks.GLASS) || block.isOf(Blocks.TINTED_GLASS) || block.getBlock() instanceof StainedGlassBlock) {
                        ci.setReturnValue(GLASS_RESISTANCE);
                    }
                }
            }
        }
    }
}
