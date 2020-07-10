package net.gegy1000.bedwars.game.bw.gen;

import net.gegy1000.bedwars.map.GameMapBuilder;
import net.gegy1000.bedwars.util.OpenSimplexNoise;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public abstract class NoiseIslandGen implements MapGen {
	protected final OpenSimplexNoise noise;
	protected final BlockPos origin;

	public NoiseIslandGen(BlockPos origin, long seed) {
		this.origin = origin;
		noise = new OpenSimplexNoise(seed);
	}

	@Override
	public void addTo(GameMapBuilder builder) {
		int radius = radius();

		int radiusSquared = radius * radius;

		// Iterate a circle
		for(int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {

				// Populate Noise
				for(int y = -radius; y <= radius; y++) {
					int squareDistance = (x * x) + (y * y) + (z * z);

					// Place stone based on noise
					if (squareDistance <= radiusSquared) {
						double noiseAt = noise.eval((origin.getX() + x) / horizontalScale(), (origin.getY() + y) / verticalScale(), (origin.getZ() + z) / horizontalScale());
						noiseAt += computeNoiseFalloff(y);

						if (noiseAt > 0) {
							builder.setBlockState(origin.add(x, y, z), Blocks.STONE.getDefaultState());
						}
					}
				}

				// Build Surface
				for(int y = radius; y > -radius; y--) {
					// Air above
					if (builder.getBlockState(origin.add(x, y + 1, z)).isAir()) {
						// Stone here
						if (builder.getBlockState(origin.add(x, y, z)) == Blocks.STONE.getDefaultState()) {
							// Set grass, iterate downwards and set dirt
							builder.setBlockState(origin.add(x, y, z), Blocks.GRASS_BLOCK.getDefaultState());
							for (int i = 1; i < 3; i++) {
								// check for stone
								if (builder.getBlockState(origin.add(x, y - i, z)) == Blocks.STONE.getDefaultState()) {
									builder.setBlockState(origin.add(x, y - i, z), Blocks.DIRT.getDefaultState());
								} else {
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	protected abstract double horizontalScale();

	protected abstract double verticalScale();

	protected abstract int radius();

	// Computes the falloff constant at a y coordinate.
	protected abstract double computeNoiseFalloff(double y);
}
