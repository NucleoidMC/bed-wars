package net.gegy1000.bedwars.game.generator.island;

import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.gl.game.map.GameMapBuilder;

import net.minecraft.util.math.BlockPos;

public class RandomSmallIslandGen implements MapGen {
	private final NoiseIslandGen noiseGen;

	public RandomSmallIslandGen(BlockPos origin, long seed) {
		this.noiseGen = new NoiseIslandGen(origin, seed);
		this.noiseGen.setRadius(5);
		this.noiseGen.setNoiseFrequency(1.0 / 6.0, 1.0 / 8.0);
		this.noiseGen.setCheckSpace(true);
		this.noiseGen.setFalloff(RandomSmallIslandGen::computeNoiseFalloff);
	}

	@Override
	public void addTo(GameMapBuilder builder) {
		noiseGen.addTo(builder);
	}

	@Override
	public void addRegionsTo(GameMapBuilder builder) {

	}

	// Desmos: 0.5\cdot\frac{10}{x+3}-2
	private static double computeNoiseFalloff(double y) {
		return (0.5 * (10.0 / (y + 3))) - 2;
	}
}
