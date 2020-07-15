package net.gegy1000.bedwars.mixin;

import net.gegy1000.gl.game.GameManager;
import net.gegy1000.bedwars.game.BedWars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.FireChargeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

@Mixin(FireChargeItem.class)
public class FireChargeItemMixin {
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void handleUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		BedWars game = GameManager.openFor(BedWars.TYPE);
		if (game != null) {
			cir.setReturnValue(game.events.onUseItem(context.getPlayer(), context.getWorld(), context.getHand()).getResult());
		}
	}
}
