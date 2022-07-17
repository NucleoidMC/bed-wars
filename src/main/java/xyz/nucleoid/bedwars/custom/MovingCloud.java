package xyz.nucleoid.bedwars.custom;

import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.map_templates.BlockBounds;

public final class MovingCloud {
    private static final double MOVE_PER_TICK = 1.5 / 20.0;
    private static final int PAUSE_TICKS = 2 * 20;
    private static final int STOP_TICKS = 5 * 20;

    private static final int MAX_AGE = 20 * 60;

    private final ServerWorld world;
    private Vec3d pos;
    private final Vec3d movePerTick;

    private int ticks;
    private BlockPos lastBlockPos;

    private int pauseTicks = PAUSE_TICKS;
    private int stoppedTicks;

    public MovingCloud(ServerWorld world, BlockPos pos, Direction direction) {
        this.world = world;
        this.pos = Vec3d.ofCenter(pos);
        this.movePerTick = new Vec3d(
                direction.getOffsetX() * MOVE_PER_TICK,
                direction.getOffsetY() * MOVE_PER_TICK,
                direction.getOffsetZ() * MOVE_PER_TICK
        );
    }

    public boolean tick() {
        if (this.lastBlockPos == null) {
            this.updatePlatform(new BlockPos(this.pos));
        }

        if (this.ticks++ >= MAX_AGE) {
            if (this.lastBlockPos != null) {
                this.removePlatform(this.lastBlockPos);
            }
            return true;
        }

        if (this.ticks % 2 == 0) {
            this.spawnParticles();
        }

        if (this.pauseTicks > 0) {
            if (--this.pauseTicks > 0) {
                return false;
            }
        }

        if (this.stoppedTicks > 0) {
            if (--this.stoppedTicks <= 0) {
                this.removePlatform(this.lastBlockPos);
                return true;
            }
        }

        this.pos = this.pos.add(this.movePerTick);

        BlockPos blockPos = new BlockPos(this.pos);
        if (!blockPos.equals(this.lastBlockPos)) {
            this.updatePlatform(blockPos);
        }

        return false;
    }

    private void updatePlatform(BlockPos blockPos) {
        if (this.lastBlockPos != null) {
            this.removePlatform(this.lastBlockPos);
        }

        if (this.tryAddPlatform(blockPos)) {
            this.lastBlockPos = blockPos;
        } else {
            this.stoppedTicks = STOP_TICKS;
        }
    }

    private void spawnParticles() {
        Random random = this.world.random;

        double centerX = this.pos.x + this.movePerTick.x * 20.0;
        double centerZ = this.pos.z + this.movePerTick.z * 20.0;
        double y = this.pos.y;

        for (int i = 0; i < 4; i++) {
            double x = centerX + random.nextGaussian() * 0.5;
            double z = centerZ + random.nextGaussian() * 0.5;

            this.world.spawnParticles(ParticleTypes.CLOUD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void removePlatform(BlockPos origin) {
        BlockBounds platform = this.getPlatformAt(origin);
        for (BlockPos pos : platform) {
            if (this.world.getBlockState(pos).getBlock() == Blocks.BARRIER) {
                this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    private boolean tryAddPlatform(BlockPos origin) {
        if (!this.world.isAir(origin)) {
            return false;
        }

        BlockBounds platform = this.getPlatformAt(origin);
        for (BlockPos pos : platform) {
            if (this.world.isAir(pos)) {
                this.world.setBlockState(pos, Blocks.BARRIER.getDefaultState());
            }
        }

        return true;
    }

    private BlockBounds getPlatformAt(BlockPos pos) {
        return BlockBounds.of(
                pos.add(-1, 0, -1),
                pos.add(1, 0, 1)
        );
    }
}
