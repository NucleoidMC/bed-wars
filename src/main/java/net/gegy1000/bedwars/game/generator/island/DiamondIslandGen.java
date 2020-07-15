package net.gegy1000.bedwars.game.generator.island;

import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.gl.game.map.GameMapBuilder;

import net.minecraft.util.math.BlockPos;

public class DiamondIslandGen implements MapGen {
    private final NoiseIslandGen noiseGen;

    public DiamondIslandGen(BlockPos origin, long seed) {
        this.noiseGen = new NoiseIslandGen(origin, seed);
        this.noiseGen.setRadius(10);
        this.noiseGen.setNoiseFrequency(1.0 / 8.0, 1.0 / 12.0);
        this.noiseGen.setFalloff(DiamondIslandGen::computeNoiseFalloff);
    }

    @Override
    public void addTo(GameMapBuilder builder) {
        this.noiseGen.addTo(builder);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {
    }

    // Desmos: \frac{40}{x+10}-4.25
    private static double computeNoiseFalloff(double y) {
        return (40.0 / (y + 10)) - 4.25;
    }
}
