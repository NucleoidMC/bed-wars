package xyz.nucleoid.bedwars.game.generator.gen;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import xyz.nucleoid.substrate.gen.GenHelper;
import xyz.nucleoid.substrate.gen.MapGen;

public class TaigaTreeGen implements MapGen {
	public static final MapGen INSTANCE = new TaigaTreeGen(Blocks.SPRUCE_LOG.defaultBlockState(), Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(BlockStateProperties.DISTANCE, 1));
	private final BlockState log;
	private final BlockState leaves;

	public TaigaTreeGen(BlockState log, BlockState leaves) {
		this.log = log;
		this.leaves = leaves;
	}

	@Override
	public void generate(ServerLevelAccessor world, BlockPos pos, RandomSource random) {
		if (world.getBlockState(pos.below()) != Blocks.GRASS_BLOCK.defaultBlockState()) return;

		int heightAddition = random.nextInt(4);

		double maxRadius = 1.8 + ((random.nextDouble() - 0.5) * 0.2);

		BlockPos.MutableBlockPos mutable = pos.mutable();
		for (int y = 0; y < 8 + heightAddition; y++) {
			world.setBlock(mutable, this.log, 0);
			mutable.move(Direction.UP);
		}

		mutable = pos.mutable();
		mutable.move(Direction.UP, 1 + heightAddition);

		for (int y = 0; y < 9; y++) {
			GenHelper.circle(mutable.mutable(), maxRadius * radius(y / 10.f), leafPos -> {
				if (world.getBlockState(leafPos).isAir()) {
					world.setBlock(leafPos, this.leaves, 0);
				}
			});
			mutable.move(Direction.UP);
		}
	}

	private double radius(double x) {
		return -0.15 * (x * x) - x + 1.3;
	}
}
