package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.custom.BwCustomEntities;
import net.gegy1000.gl.entity.CustomEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {

	// Mute villagers inside bedwars games
	@Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
	private void handleGetAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
		CustomEntity customEntity = CustomEntity.match((Entity) (Object) this);

		// Mute only these two custom villagers
		if (customEntity == BwCustomEntities.ITEM_SHOP || customEntity == BwCustomEntities.TEAM_SHOP) {
			cir.setReturnValue(null);
		}
	}
}
