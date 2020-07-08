package net.gegy1000.bedwars.api;

import net.gegy1000.bedwars.PartialRegion;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface RegionConstructor {
    void startTracing(BlockPos origin);

    void trace(BlockPos pos);

    void finishTracing(BlockPos pos);

    boolean isTracing();

    @Nullable
    PartialRegion getTracing();

    @Nullable
    PartialRegion takeReady();
}
