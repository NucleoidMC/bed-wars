package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.util.BlockStateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(EggEntity.class)
public abstract class MixinEggEntity extends ThrownItemEntity implements BlockStateHolder {
	@Unique
	private BlockState woolState = Blocks.WHITE_WOOL.getDefaultState();

	public MixinEggEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	// Set wool blockstate
	public void setBlockState(BlockState state) {
		woolState = state;
	}

	@Override
	public void tick() {
		super.tick();

		// Place wool every tick
		if (!this.world.isClient()) {
			BedWars game = GameManager.activeFor(BedWars.TYPE);
			if (game != null) {
				BlockPos pos = this.getBlockPos().down();
				if (world.getBlockState(pos).isAir()) {
					world.setBlockState(pos, woolState);
				}
			}
		}
	}

	@Inject(method = "onCollision", at = @At("HEAD"), cancellable = true)
	private void handleCollision(HitResult hitResult, CallbackInfo ci) {
		if (!this.world.isClient()) {
			BedWars game = GameManager.activeFor(BedWars.TYPE);
			if (game != null) {
				// Cancel if it's trying to hit our wool
				if (hitResult.getType() == HitResult.Type.BLOCK) {
					if (world.getBlockState(((BlockHitResult) hitResult).getBlockPos()) == woolState) {
						ci.cancel();
					}
				}
			}
		}
	}


}
