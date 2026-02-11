package xyz.nucleoid.bedwars.mixin;

import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
@Mixin(BedBlock.class)
public class BedBlockMixin {

	/**
	 * @reason Hacky fix to prevent bed explosions.
	 *
	 * @author SuperCoder79
	 */
	@Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
	private static void noExplosion(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<Boolean> cir) {
		var gameSpace = GameSpaceManager.get().byWorld(world);
		if (gameSpace != null && gameSpace.getMetadata().sourceConfig().value().type() == BedWars.TYPE) {
			cir.setReturnValue(true);
		}
	}
}
