/*
package net.gegy1000.bedwars.game.generator.legacy.feature;

import java.util.Random;

import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.map.GameMapBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.WeightedList;
import net.minecraft.util.math.BlockPos;

public class GrassGen implements MapGen {
	private static final WeightedList<BlockState> STATES = new WeightedList<BlockState>()
			.add(Blocks.GRASS.getDefaultState(), 8)
			.add(Blocks.DANDELION.getDefaultState(), 1);

	private final BlockPos origin;

	public GrassGen(BlockPos origin) {
		this.origin = origin;
	}

	@Override
	public void addTo(GameMap builder) {
		Random random = new Random();
		BlockState state = STATES.pickRandom(random);

		for (int i = 0; i < 16; i++) {
			int aX = random.nextInt(8) - random.nextInt(8);
			int aY = random.nextInt(4) - random.nextInt(4);
			int aZ = random.nextInt(8) - random.nextInt(8);
			BlockPos pos = origin.add(aX, aY, aZ);

			if (builder.getBlockState(pos.down()) == Blocks.GRASS_BLOCK.getDefaultState() && builder.getBlockState(pos).isAir()) {
				builder.setBlockState(pos, state, false);
			}
		}
	}

	@Override
	public void addRegionsTo(GameMap map) {
	}
}
*/
