package xyz.nucleoid.bedwars.mixin;

import net.minecraft.block.BedBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;
import xyz.nucleoid.plasmid.game.manager.ManagedGameSpace;

@Mixin(BedBlock.class)
public class BedBlockMixin {

	/**
	 * @reason Hacky fix to prevent bed explosions.
	 *
	 * @author SuperCoder79
	 */
	@Inject(method = "isOverworld", at = @At("HEAD"), cancellable = true)
	private static void noExplosion(World world, CallbackInfoReturnable<Boolean> cir) {
		ManagedGameSpace gameSpace = GameSpaceManager.get().byWorld(world);
		if (gameSpace != null) {
			cir.setReturnValue(true);
		}
	}
}
