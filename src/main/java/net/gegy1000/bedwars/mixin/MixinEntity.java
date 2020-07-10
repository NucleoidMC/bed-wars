package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.custom.CustomizableEntity;
import net.gegy1000.bedwars.custom.CustomEntity;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class MixinEntity implements CustomizableEntity {
    @Shadow
    public abstract void setCustomName(@Nullable Text name);

    @Shadow
    public World world;

    private CustomEntity customEntity;

    @Override
    public void setCustomEntity(CustomEntity customEntity) {
        this.customEntity = customEntity;
        if (customEntity != null) {
            this.setCustomName(customEntity.getName());
        }
    }

    @Nullable
    @Override
    public CustomEntity getCustomEntity() {
        return this.customEntity;
    }

    @Inject(method = "toTag", at = @At("RETURN"))
    private void toTag(CompoundTag root, CallbackInfoReturnable<CompoundTag> ci) {
        if (this.customEntity != null) {
            root.putString("custom_entity", this.customEntity.getIdentifier().toString());
        }
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void fromTag(CompoundTag root, CallbackInfo ci) {
        if (root.contains("custom_entity")) {
            Identifier customId = new Identifier(root.getString("custom_entity"));
            this.customEntity = CustomEntity.get(customId);
        }
    }

    @Inject(method = "getEffectiveExplosionResistance", at = @At("HEAD"), cancellable = true)
    private void getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max, CallbackInfoReturnable<Float> ci) {
        BedWars game = GameManager.activeFor(BedWars.TYPE);
        if (game != null && game.map.contains(pos)) {
            if (blockState.getBlock() instanceof StainedGlassBlock) {
                ci.setReturnValue(99999.0F);
            }
        }
    }
}
