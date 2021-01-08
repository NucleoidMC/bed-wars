package xyz.nucleoid.bedwars.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.util.WoodBlocks;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

	/**
	 * Drop custom items for decay
	 *
	 * @author SuperCoder79
	 */
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	public void handleRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
		ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(world);
		if (gameSpace != null && gameSpace.testRule(BedWars.LEAVES_DROP_GOLDEN_APPLES) == RuleResult.ALLOW) {
			if (!state.get(LeavesBlock.PERSISTENT) && state.get(LeavesBlock.DISTANCE) == 7) {
				if (world.random.nextDouble() < 0.025) {
					world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(WoodBlocks.saplingOf(state).getBlock())));
				}

				if (world.random.nextDouble() < 0.01) {
					world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.GOLDEN_APPLE)));
				}

				world.removeBlock(pos, false);
			}

			ci.cancel();
		}
	}
}
