package xyz.nucleoid.bedwars.mixin;

import xyz.nucleoid.plasmid.item.CustomItem;
import xyz.nucleoid.bedwars.custom.BwCustomItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Random;

@Mixin(ChorusFruitItem.class)
public abstract class ChorusFruitItemMixin {
    private static final int ATTEMPTS = 32;
    private static final double MIN_DISTANCE_SQ = 6.0 * 6.0;

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void finishUsing(ItemStack stack, World world, LivingEntity entity, CallbackInfoReturnable<ItemStack> ci) {
        if (!world.isClient && CustomItem.match(stack) == BwCustomItems.BW_CHORUS_FRUIT) {
            ItemStack resultStack = entity.eatFood(world, stack);

            double originX = entity.getX();
            double originY = entity.getY();
            double originZ = entity.getZ();

            for (int i = 0; i < ATTEMPTS; ++i) {
                Vec3d target = generateTarget(entity);
                if (target == null) {
                    continue;
                }

                if (entity.hasVehicle()) {
                    entity.stopRiding();
                }

                if (entity.teleport(target.x, target.y, target.z, true)) {
                    SoundEvent sound = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                    world.playSound(null, originX, originY, originZ, sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    entity.playSound(sound, 1.0F, 1.0F);
                    break;
                }
            }

            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).getItemCooldownManager().set(stack.getItem(), 20);
            }

            ci.setReturnValue(resultStack);
        }
    }

    @Nullable
    private static Vec3d generateTarget(LivingEntity entity) {
        Random random = entity.getRandom();

        double deltaX = (random.nextDouble() - 0.5) * 20.0;
        double deltaZ = (random.nextDouble() - 0.5) * 20.0;
        int deltaY = random.nextInt(16) - 8;

        if (deltaX * deltaX + deltaZ * deltaZ < MIN_DISTANCE_SQ) {
            return null;
        }

        return new Vec3d(
                entity.getX() + deltaX,
                MathHelper.clamp(entity.getY() + deltaY, 0.0, entity.world.getDimensionHeight() - 1),
                entity.getZ() + deltaZ
        );
    }
}
