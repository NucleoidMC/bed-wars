package net.gegy1000.bedwars.game.bw.gen.island;

import net.gegy1000.bedwars.game.bw.gen.NoiseIslandGen;
import net.gegy1000.bedwars.map.GameMapBuilder;

import net.minecraft.util.math.BlockPos;

public class CenterIslandGen extends NoiseIslandGen {
    public CenterIslandGen(BlockPos origin, long seed) {
        super(origin, seed);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {

    }

    @Override
    protected int radius() {
        return 20;
    }

    @Override
    protected double horizontalScale() {
        return 12.0;
    }

    @Override
    protected double verticalScale() {
        return 16.0;
    }

    // Desmos: 0.75\cdot\frac{90}{x+20}-3.4
    @Override
    protected double computeNoiseFalloff(double y) {
        return (0.75 * (90.0 / (y + 20))) - 3.4;
    }
}