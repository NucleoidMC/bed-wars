package xyz.nucleoid.bedwars.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.GameWorld;

import net.minecraft.block.BedBlock;
import net.minecraft.world.World;

@Mixin(BedBlock.class)
public class BedBlockMixin {

	/**
	 * @reason Hacky fix to prevent bed explosions.
	 *
	 * @author SuperCoder79
	 */
	@Inject(method = "isOverworld", at = @At("HEAD"), cancellable = true)
	private static void noExplosion(World world, CallbackInfoReturnable<Boolean> cir) {
		GameWorld gameWorld = GameWorld.forWorld(world);
		if (gameWorld != null) {
			cir.setReturnValue(true);
		}
	}
}
