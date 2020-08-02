package net.gegy1000.bedwars.game.generator.island;

import kdotjpg.opensimplex.OpenSimplexNoise;
import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.plasmid.game.map.GameMapBuilder;

import net.minecraft.util.math.BlockPos;

public class RandomSmallIslandGen implements MapGen {
	private final NoiseIslandGen generator;
	private final BlockPos origin;
	private final long seed;

	public RandomSmallIslandGen(NoiseIslandGen generator, BlockPos origin, long seed) {
		this.generator = generator;
		this.origin = origin;
		this.seed = seed;
	}

	@Override
	public void addTo(GameMapBuilder builder) {
		this.generator.setOrigin(origin);
		this.generator.setNoise(new OpenSimplexNoise(seed));
		this.generator.addTo(builder);
		addRegionsTo(builder);
	}

	@Override
	public void addRegionsTo(GameMapBuilder builder) {

	}
}
