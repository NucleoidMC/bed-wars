package xyz.nucleoid.bedwars.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;

import net.minecraft.block.BlockState;
import net.minecraft.block.SaplingBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(SaplingBlock.class)
public abstract class SaplingBlockMixin {
	@Shadow public abstract void generate(ServerWorld serverWorld, BlockPos blockPos, BlockState blockState, Random random);

	/**
	 * Saplings grow faster in bedwars
	 *
	 * @author SuperCoder79
	 */
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	public void handleRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
		if (gameSpace != null) {
			if (world.getLightLevel(pos.up()) >= 9) {
				this.generate(world, pos, state, random);
			}

			ci.cancel();
		}
	}
}
