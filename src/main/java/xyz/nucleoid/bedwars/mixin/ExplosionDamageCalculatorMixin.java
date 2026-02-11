package xyz.nucleoid.bedwars.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

import java.util.Optional;

@Mixin(ExplosionDamageCalculator.class)
public class ExplosionDamageCalculatorMixin {
    @Unique
    private static final Optional<Float> GLASS_RESISTANCE = Optional.of(99999.0F);

    @Inject(method = "getBlockExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void getBlastResistance(
            Explosion explosion, BlockGetter blockView,
            BlockPos pos, BlockState block, FluidState fluid,
            CallbackInfoReturnable<Optional<Float>> ci
    ) {
        if (blockView instanceof ServerLevelAccessor) {
            ServerLevel world = ((ServerLevelAccessor) blockView).getLevel();

            var gameSpace = GameSpaceManager.get().byWorld(world);
            if (gameSpace != null) {
                var result = gameSpace.getBehavior().testRule(BedWars.BLAST_PROOF_GLASS_RULE);
                if (result == EventResult.ALLOW) {
                    if (block.is(Blocks.GLASS) || block.is(Blocks.TINTED_GLASS) || block.getBlock() instanceof StainedGlassBlock) {
                        ci.setReturnValue(GLASS_RESISTANCE);
                    }
                }
            }
        }
    }
}
