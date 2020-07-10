package net.gegy1000.bedwars.game.bw.gen.island;

import net.gegy1000.bedwars.game.bw.gen.MapGen;
import net.gegy1000.bedwars.game.bw.gen.NoiseIslandGen;
import net.gegy1000.bedwars.map.GameMapBuilder;
import net.minecraft.util.math.BlockPos;

public class CenterIslandGen implements MapGen {
    private final NoiseIslandGen noiseGen;

    public CenterIslandGen(BlockPos origin, long seed) {
        this.noiseGen = new NoiseIslandGen(origin, seed);
        this.noiseGen.setRadius(20);
        this.noiseGen.setNoiseFrequency(1.0 / 12.0, 1.0 / 16.0);
        this.noiseGen.setFalloff(CenterIslandGen::computeNoiseFalloff);
    }

    @Override
    public void addTo(GameMapBuilder builder) {
        this.noiseGen.addTo(builder);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {

    }

    // Desmos: 0.75\cdot\frac{90}{x+20}-3.4
    private static double computeNoiseFalloff(double y) {
        return (0.75 * (90.0 / (y + 20))) - 3.4;
    }
}
