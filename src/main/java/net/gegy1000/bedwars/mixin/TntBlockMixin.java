package net.gegy1000.bedwars.mixin;

import net.gegy1000.gl.game.GameManager;
import net.gegy1000.bedwars.game.BedWars;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntBlock.class)
public class TntBlockMixin {
    @Inject(method = "onBlockAdded", at = @At("HEAD"))
    private void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved, CallbackInfo ci) {
        BedWars game = GameManager.openFor(BedWars.TYPE);
        if (game != null && game.map.contains(pos)) {
            TntBlock.primeTnt(world, pos);
            world.removeBlock(pos, false);
        }
    }
}
