package net.gegy1000.bedwars.game.generator.feature;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;
import java.util.function.Consumer;

public class PoplarTreeFeature extends Feature<DefaultFeatureConfig> {
    public static final PoplarTreeFeature INSTANCE = new PoplarTreeFeature(DefaultFeatureConfig.CODEC);
    private static final BlockState LOG = Blocks.OAK_LOG.getDefaultState();
    private static final BlockState LEAVES = Blocks.OAK_LEAVES.getDefaultState().with(Properties.DISTANCE_1_7, 1);

    public PoplarTreeFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(ServerWorldAccess world, StructureAccessor accessor, ChunkGenerator generator, Random random, BlockPos pos, DefaultFeatureConfig config) {
        if (world.getBlockState(pos.down()) != Blocks.GRASS_BLOCK.getDefaultState()) return false;

        double maxRadius = 2.6 + ((random.nextDouble() - 0.5) * 0.2);
        int leafDistance = random.nextInt(3) + 2;

        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int y = 0; y < 12; y++) {
            world.setBlockState(mutable, LOG, 0);
            //add branch blocks
            if (maxRadius * radius(y / 11.f) > 2.3) {
                Direction.Axis axis = getAxis(random);
                world.setBlockState(mutable.offset(getDirection(axis, random)).up(leafDistance), LOG.with(Properties.AXIS, axis), 0);
            }

            mutable.move(Direction.UP);
        }

        mutable = pos.mutableCopy();
        mutable.move(Direction.UP, leafDistance);

        for (int y = 0; y < 12; y++) {
            circle(mutable.mutableCopy(), maxRadius * radius(y / 11.f), leafPos -> {
                if (world.getBlockState(leafPos).isAir()) {
                    world.setBlockState(leafPos, LEAVES, 0);
                }
            });
            mutable.move(Direction.UP);
        }

        return true;
    }

    private double radius(double x) {
        return (-2 * (x * x * x)) + (1.9 * x) + 0.2;
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