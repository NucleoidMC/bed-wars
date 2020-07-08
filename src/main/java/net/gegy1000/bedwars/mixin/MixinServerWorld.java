package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.event.PlayerJoinCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
    @Inject(method = "onPlayerConnected", at = @At("RETURN"))
    private void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        PlayerJoinCallback.EVENT.invoker().onJoin(player);
    }
}
