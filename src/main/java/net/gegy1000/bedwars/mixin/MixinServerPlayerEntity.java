package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.PartialRegion;
import net.gegy1000.bedwars.RegionTraceMode;
import net.gegy1000.bedwars.api.MapViewer;
import net.gegy1000.bedwars.api.RegionConstructor;
import net.gegy1000.bedwars.event.PlayerDeathCallback;
import net.gegy1000.bedwars.game.map.StagingMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity implements MapViewer, RegionConstructor {
    private StagingMap viewing;

    private PartialRegion tracing;
    private PartialRegion ready;

    private RegionTraceMode traceMode = RegionTraceMode.OFFSET;

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void onDeath(DamageSource source, CallbackInfo ci) {
        boolean cancel = PlayerDeathCallback.EVENT.invoker().onDeath((ServerPlayerEntity) (Object) this, source);
        if (cancel) {
            ci.cancel();
        }
    }

    @Override
    public void setViewing(StagingMap map) {
        this.viewing = map;
    }

    @Nullable
    @Override
    public StagingMap getViewing() {
        return this.viewing;
    }

    @Override
    public void startTracing(BlockPos origin) {
        this.ready = null;
        this.tracing = new PartialRegion(origin);
    }

    @Override
    public void trace(BlockPos pos) {
        if (this.tracing != null) {
            this.tracing.setTarget(pos);
        }
    }

    @Override
    public void finishTracing(BlockPos pos) {
        this.tracing.setTarget(pos);
        this.ready = this.tracing;
        this.tracing = null;
    }

    @Override
    public boolean isTracing() {
        return this.tracing != null;
    }

    @Nullable
    @Override
    public PartialRegion getTracing() {
        return this.tracing != null ? this.tracing : this.ready;
    }

    @Nullable
    @Override
    public PartialRegion takeReady() {
        PartialRegion ready = this.ready;
        this.ready = null;
        return ready;
    }

    @Override
    public void setTraceMode(RegionTraceMode traceMode) {
        this.traceMode = traceMode;
    }

    @Override
    public RegionTraceMode getTraceMode() {
        return this.traceMode;
    }
}
