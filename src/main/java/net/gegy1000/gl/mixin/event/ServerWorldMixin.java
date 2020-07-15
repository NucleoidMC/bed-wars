package net.gegy1000.gl.mixin.event;

import net.gegy1000.gl.event.PlayerJoinCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "onPlayerConnected", at = @At("RETURN"))
    private void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        PlayerJoinCallback.EVENT.invoker().onJoin(player);
    }
}
