package xyz.nucleoid.bedwars.custom;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public final class BwChorusFruitItem extends Item implements PolymerItem {
    private static final int ATTEMPTS = 32;
    private static final double MIN_DISTANCE_SQ = 6.0 * 6.0;

    public BwChorusFruitItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (!world.isClientSide()) {
            stack.shrink(1);

            double originX = entity.getX();
            double originY = entity.getY();
            double originZ = entity.getZ();

            for (int i = 0; i < ATTEMPTS; ++i) {
                Vec3 target = generateTarget(entity);
                if (target == null) {
                    continue;
                }

                if (entity.isPassenger()) {
                    entity.stopRiding();
                }

                if (entity.randomTeleport(target.x, target.y, target.z, true)) {
                    SoundEvent sound = SoundEvents.CHORUS_FRUIT_TELEPORT;
                    world.playSound(null, originX, originY, originZ, sound, SoundSource.PLAYERS, 1.0F, 1.0F);
                    entity.playSound(sound, 1.0F, 1.0F);
                    break;
                }
            }

            if (entity instanceof Player) {
                ((Player) entity).getCooldowns().addCooldown(stack, 20);
            }

            return stack;
        }

        return stack;
    }

    @Nullable
    private static Vec3 generateTarget(LivingEntity entity) {
        RandomSource random = entity.getRandom();

        double deltaX = (random.nextDouble() - 0.5) * 20.0;
        double deltaZ = (random.nextDouble() - 0.5) * 20.0;
        int deltaY = random.nextInt(16) - 8;

        if (deltaX * deltaX + deltaZ * deltaZ < MIN_DISTANCE_SQ) {
            return null;
        }

        return new Vec3(
                entity.getX() + deltaX,
                Mth.clamp(entity.getY() + deltaY, entity.level().getMinY(), entity.level().getMaxY()),
                entity.getZ() + deltaZ
        );
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        return Items.CHORUS_FRUIT;
    }

    @Override
    public Component getName(ItemStack stack) {
        var name = stack.getComponentsPatch().get(DataComponents.ITEM_NAME);
        return name != null && name.isPresent() ? name.get() : Items.CHORUS_FRUIT.getName();
    }

    @Override
    @Nullable
    public Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }
}
