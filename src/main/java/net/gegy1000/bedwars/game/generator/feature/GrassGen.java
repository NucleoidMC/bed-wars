package net.gegy1000.bedwars.game.generator.feature;

import java.util.Random;

import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.gl.game.map.GameMapBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class GrassGen implements MapGen {
	private final BlockPos origin;

	public GrassGen(BlockPos origin) {
		this.origin = origin;
	}

	@Override
	public void addTo(GameMapBuilder builder) {
		Random random = new Random();
		for (int i = 0; i < 16; i++) {
			int aX = random.nextInt(8) - random.nextInt(8);
			int aY = random.nextInt(4) - random.nextInt(4);
			int aZ = random.nextInt(8) - random.nextInt(8);
			BlockPos pos = origin.add(aX, aY, aZ);
			if (builder.getBlockState(pos.down()) == Blocks.GRASS_BLOCK.getDefaultState()) {
				builder.setBlockState(pos, Blocks.GRASS.getDefaultState());
			}
		}
	}

	@Override
	public void addRegionsTo(GameMapBuilder builder) {

	}
}
