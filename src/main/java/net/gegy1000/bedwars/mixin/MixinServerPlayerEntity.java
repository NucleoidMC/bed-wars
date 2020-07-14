package net.gegy1000.bedwars.mixin;

import com.mojang.authlib.GameProfile;
import net.gegy1000.bedwars.event.PlayerDeathCallback;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.map.MapViewer;
import net.gegy1000.bedwars.map.StagingMap;
import net.gegy1000.bedwars.map.trace.PartialRegion;
import net.gegy1000.bedwars.map.trace.RegionTraceMode;
import net.gegy1000.bedwars.map.trace.RegionTracer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements MapViewer, RegionTracer {
    @Shadow @Final public MinecraftServer server;
    private StagingMap viewing;

    private PartialRegion tracing;
    private PartialRegion ready;

    private RegionTraceMode traceMode = RegionTraceMode.OFFSET;

    public MixinServerPlayerEntity(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

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
    public void setMode(RegionTraceMode traceMode) {
        this.traceMode = traceMode;
    }

    @Override
    public RegionTraceMode getMode() {
        return this.traceMode;
    }

    /**
     * Ensure that PVP is enabled when in a game
     */
    @Inject(method = "isPvpEnabled", at = @At("HEAD"), cancellable = true)
    private void testPvpEnabled(CallbackInfoReturnable<Boolean> cir) {
        BedWars game = GameManager.openFor(BedWars.TYPE);
        if (game != null && game.map.contains(this.getBlockPos())) {
            cir.setReturnValue(true);
        }
    }
}
