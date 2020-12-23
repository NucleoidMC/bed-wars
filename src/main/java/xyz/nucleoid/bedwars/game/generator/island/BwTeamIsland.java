package xyz.nucleoid.bedwars.game.generator.island;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapConfig;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

import java.util.Random;

public final class BwTeamIsland {
    private static final int RADIUS = 10;

    final BlockPos origin;
    final BlockBounds bounds;
    final GameTeam team;
    private final double angle;
    private final Direction direction;

    public BwTeamIsland(BlockPos origin, GameTeam team, double angle) {
        this.bounds = new BlockBounds(
                origin.add(-RADIUS, 0, -RADIUS),
                origin.add(RADIUS, 0, RADIUS)
        );

        this.origin = origin;
        this.team = team;

        this.angle = angle;
        this.direction = Direction.fromRotation(Math.toDegrees(angle) + 90.0);
    }

    public void addTo(BwSkyMapConfig config, BwMap map, MapTemplate template) {
        BlockPos origin = BwTeamIsland.this.origin;
        BlockState terracotta = ColoredBlocks.terracotta(BwTeamIsland.this.team.getDye()).getDefaultState();
        Random random = new Random();

        for (BlockPos pos : this.bounds) {
            int deltaX = pos.getX() - origin.getX();
            int deltaZ = pos.getZ() - origin.getZ();
            int radius = Math.max(Math.abs(deltaX), Math.abs(deltaZ));

            if (radius <= 1) {
                template.setBlockState(pos, Blocks.IRON_BLOCK.getDefaultState());
            } else {
                template.setBlockState(pos, config.theme.teamIslandState(random, terracotta));
            }
        }

        template.setBlockState(this.transformPosition(-1, 1, -2), Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, this.direction));
        template.setBlockState(this.transformPosition(1, 1, -2), Blocks.ENDER_CHEST.getDefaultState().with(ChestBlock.FACING, this.direction));

        BlockState bed = ColoredBlocks.bed(this.team.getDye()).getDefaultState()
                .with(BedBlock.FACING, this.direction);

        template.setBlockState(this.transformPosition(0, 1, 5), bed.with(BedBlock.PART, BedPart.FOOT));
        template.setBlockState(this.transformPosition(0, 1, 6), bed.with(BedBlock.PART, BedPart.HEAD));

        this.addRegionsTo(map);
    }

    private void addRegionsTo(BwMap map) {
        BlockBounds spawn = new BlockBounds(
                this.origin.add(-1, 1, -1),
                this.origin.add(1, 1, 1)
        );

        BlockBounds base = new BlockBounds(
                this.bounds.getMin().down(1),
                this.bounds.getMax().up(4)
        );

        BlockBounds chest = BlockBounds.of(this.transformPosition(-1, 1, -2));
        BlockBounds enderChest = BlockBounds.of(this.transformPosition(1, 1, -2));
        BlockBounds teamShop = BlockBounds.of(this.transformPosition(-2, 1, -1));
        BlockBounds itemShop = BlockBounds.of(this.transformPosition(-2, 1, 1));

        BlockBounds bed = new BlockBounds(
                this.transformPosition(0, 1, 5),
                this.transformPosition(0, 1, 6)
        );

        map.addProtectedBlocks(this.bounds);
        map.addProtectedBlocks(chest);
        map.addProtectedBlocks(enderChest);
        map.addProtectedBlocks(bed);

        Direction shopDirection = this.direction.rotateYClockwise();
        map.addTeamRegions(this.team, new BwMap.TeamRegions(spawn, bed, base, chest, itemShop, teamShop, shopDirection, shopDirection));
    }

    private BlockPos transformPosition(int x, int y, int z) {
        Direction forward = this.direction;
        Direction side = this.direction.rotateYClockwise();

        return this.origin.add(
                side.getOffsetX() * x + side.getOffsetZ() * z,
                y,
                forward.getOffsetX() * x + forward.getOffsetZ() * z
        );
    }
}
