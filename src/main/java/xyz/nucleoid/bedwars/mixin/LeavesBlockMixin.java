package xyz.nucleoid.bedwars.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.plasmid.api.util.WoodType;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

    /**
     * Drop custom items for decay
     *
     * @author SuperCoder79
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void handleRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        var gameSpace = GameSpaceManager.get().byWorld(world);
        if (gameSpace != null && gameSpace.getBehavior().testRule(BedWars.LEAVES_DROP_GOLDEN_APPLES) == EventResult.ALLOW) {
            if (!state.getValue(LeavesBlock.PERSISTENT) && state.getValue(LeavesBlock.DISTANCE) == 7) {
                if (world.random.nextDouble() < 0.025) {
                    var plant = WoodType.getType(state.getBlock()).getPlant();
                    world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(plant)));
                }

                if (world.random.nextDouble() < 0.01) {
                    world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.GOLDEN_APPLE)));
                }

                world.removeBlock(pos, false);
            }

            ci.cancel();
        }
    }
}
