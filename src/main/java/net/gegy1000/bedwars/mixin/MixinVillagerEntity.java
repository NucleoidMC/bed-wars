package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.custom.CustomEntities;
import net.gegy1000.bedwars.custom.CustomEntity;
import net.gegy1000.bedwars.custom.CustomizableEntity;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.sound.SoundEvent;

@Mixin(VillagerEntity.class)
public class MixinVillagerEntity {

	// Mute villagers inside bedwars games
	@Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
	private void handleGetAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
		BedWars game = GameManager.activeFor(BedWars.TYPE);
		CustomEntity customEntity = ((CustomizableEntity) this).getCustomEntity();

		// Mute only these two custom villagers
		if ((customEntity == CustomEntities.ITEM_SHOP || customEntity == CustomEntities.TEAM_SHOP) && game != null) {
			cir.setReturnValue(null);
		}
	}
}
