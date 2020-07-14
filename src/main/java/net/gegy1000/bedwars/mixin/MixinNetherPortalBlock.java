package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

@Mixin(NetherPortalBlock.class)
public class MixinNetherPortalBlock {
	@Inject(method = "createPortalAt", at = @At("HEAD"), cancellable = true)
	private static void handleCreatePortal(WorldAccess world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BedWars game = GameManager.openFor(BedWars.TYPE);
		if (game != null && game.map.contains(pos)) {
			cir.setReturnValue(false);
		}
	}
}
