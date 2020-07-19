package net.gegy1000.bedwars.game.generator.feature;

import java.util.Random;
import java.util.function.Consumer;

import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.gl.game.map.GameMapBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AspenTreeGen implements MapGen {
	private final BlockPos origin;

	public AspenTreeGen(BlockPos origin) {
		this.origin = origin;
	}

	@Override
	public void addTo(GameMapBuilder builder) {
		Random random = new Random();

		double maxRadius = 2 + ((random.nextDouble() - 0.5) * 0.2);
		int leafDistance = random.nextInt(4) + 3;

		BlockPos.Mutable mutable = origin.mutableCopy();
		for (int y = 0; y < 8; y++) {
			builder.setBlockState(mutable, Blocks.BIRCH_LOG.getDefaultState(), false);
			//add branch blocks
			if (maxRadius * radius(y / 7.f) > 2.1) {
				Direction.Axis axis = getAxis(random);
				builder.setBlockState(mutable.offset(getDirection(axis, random)).up(leafDistance), Blocks.BIRCH_LOG.getDefaultState().with(Properties.AXIS, axis), false);
			}

			mutable.move(Direction.UP);
		}

		mutable = origin.mutableCopy();
		mutable.move(Direction.UP, leafDistance);

		for (int y = 0; y < 8; y++) {
			circle(mutable.mutableCopy(), maxRadius * radius(y / 7.f), leafPos -> {
				if (builder.getBlockState(leafPos).isAir()) {
					builder.setBlockState(leafPos, Blocks.BIRCH_LEAVES.getDefaultState(), false);
				}
			});
			mutable.move(Direction.UP);
		}
	}

	private double radius(double x) {
		return -Math.pow(((1.4 * x) - 0.3), 2) + 1.2;
	}

	private Direction.Axis getAxis(Random random) {
		return random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
	}

	private Direction getDirection(Direction.Axis axis, Random random) {
		if (axis == Direction.Axis.X) {
			return random.nextBoolean() ? Direction.EAST : Direction.WEST;
		} else {
			return random.nextBoolean() ? Direction.NORTH : Direction.SOUTH;
		}
	}

	@Override
	public void addRegionsTo(GameMapBuilder builder) {

	}

	// Code used from Terraform
	public static void circle(BlockPos.Mutable origin, double radius, Consumer<BlockPos.Mutable> consumer) {
		int x = origin.getX();
		int z = origin.getZ();
		double radiusSq = radius * radius;
		int radiusCeil = (int)Math.ceil(radius);

		for(int dz = -radiusCeil; dz <= radiusCeil; ++dz) {
			int dzSq = dz * dz;

			for(int dx = -radiusCeil; dx <= radiusCeil; ++dx) {
				int dxSq = dx * dx;
				if ((double)(dzSq + dxSq) <= radiusSq) {
					origin.set(x + dx, origin.getY(), z + dz);
					consumer.accept(origin);
				}
			}
		}

	}
}
