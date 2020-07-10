package net.gegy1000.bedwars.game.bw.gen.island;

import net.gegy1000.bedwars.game.bw.gen.NoiseIslandGen;
import net.gegy1000.bedwars.map.GameMapBuilder;

import net.minecraft.util.math.BlockPos;

public class DiamondIslandGen extends NoiseIslandGen {
    public DiamondIslandGen(BlockPos origin, long seed) {
        super(origin, seed);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {

    }

    @Override
    protected int radius() {
        return 10;
    }

    @Override
    protected double horizontalScale() {
        return 8.0;
    }

    @Override
    protected double verticalScale() {
        return 12.0;
    }

    // Desmos: \frac{40}{x+10}-4.25
    @Override
    protected double computeNoiseFalloff(double y) {
        return (40.0 / (y + 10)) - 4.25;
    }
}