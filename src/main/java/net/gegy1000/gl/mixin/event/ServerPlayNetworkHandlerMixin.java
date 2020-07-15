package net.gegy1000.gl.mixin.event;

import net.gegy1000.gl.event.SwingHandCallback;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void onHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        SwingHandCallback.EVENT.invoker().onSwingHand(this.player, packet.getHand());
    }
}
