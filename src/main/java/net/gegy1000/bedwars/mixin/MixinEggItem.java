package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.util.BlockStateHolder;
import net.gegy1000.bedwars.util.ColoredBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.EggItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(EggItem.class)
public class MixinEggItem extends Item {
	public MixinEggItem(Settings settings) {
		super(settings);
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void handleUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (!world.isClient) {
			BedWars game = GameManager.activeFor(BedWars.TYPE);
			if (game != null) {
				ItemStack itemStack = user.getStackInHand(hand);
				world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (RANDOM.nextFloat() * 0.4F + 0.8F));

				// Get player wool color
				BlockState state = ColoredBlocks.wool(game.state.getTeam(user.getUuid()).getDye()).getDefaultState();

				// Spawn egg
				EggEntity eggEntity = new EggEntity(world, user);
				eggEntity.setItem(itemStack);
				eggEntity.setProperties(user, user.pitch, user.yaw, 0.0F, 1.5F, 1.0F);
				((BlockStateHolder)eggEntity).setBlockState(state);
				world.spawnEntity(eggEntity);

				if (!user.abilities.creativeMode) {
					itemStack.decrement(1);
				}

				cir.setReturnValue(TypedActionResult.method_29237(itemStack, world.isClient()));
			}
		}
	}
}
