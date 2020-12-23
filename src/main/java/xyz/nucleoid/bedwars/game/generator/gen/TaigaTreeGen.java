package xyz.nucleoid.bedwars.game.generator.gen;

import java.util.Random;

import xyz.nucleoid.plasmid.game.gen.GenHelper;
import xyz.nucleoid.substrate.gen.MapGen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;

public class TaigaTreeGen implements MapGen {
	public static final MapGen INSTANCE = new TaigaTreeGen(Blocks.SPRUCE_LOG.getDefaultState(), Blocks.SPRUCE_LEAVES.getDefaultState().with(Properties.DISTANCE_1_7, 1));
	private final BlockState log;
	private final BlockState leaves;

	public TaigaTreeGen(BlockState log, BlockState leaves) {
		this.log = log;
		this.leaves = leaves;
	}

	@Override
	public void generate(ServerWorldAccess world, BlockPos pos, Random random) {
		if (world.getBlockState(pos.down()) != Blocks.GRASS_BLOCK.getDefaultState()) return;

		int heightAddition = random.nextInt(4);

		double maxRadius = 1.8 + ((random.nextDouble() - 0.5) * 0.2);

		BlockPos.Mutable mutable = pos.mutableCopy();
		for (int y = 0; y < 8 + heightAddition; y++) {
			world.setBlockState(mutable, this.log, 0);
			mutable.move(Direction.UP);
		}

		mutable = pos.mutableCopy();
		mutable.move(Direction.UP, 1 + heightAddition);

		for (int y = 0; y < 9; y++) {
			GenHelper.circle(mutable.mutableCopy(), maxRadius * radius(y / 10.f), leafPos -> {
				if (world.getBlockState(leafPos).isAir()) {
					world.setBlockState(leafPos, this.leaves, 0);
				}
			});
			mutable.move(Direction.UP);
		}

		return;
	}

	private double radius(double x) {
		return -0.15 * (x * x) - x + 1.3;
	}
}
