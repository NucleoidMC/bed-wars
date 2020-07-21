package net.gegy1000.gl.mixin.event;

import net.gegy1000.gl.game.Game;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.game.event.PlayerRejoinListener;
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
        if (player.world.isClient) {
            return;
        }

        Game game = GameManager.openGame();
        if (game != null && game.containsPlayer(player)) {
            game.invoker(PlayerRejoinListener.EVENT).onRejoin(game, player);
        }
    }
}
