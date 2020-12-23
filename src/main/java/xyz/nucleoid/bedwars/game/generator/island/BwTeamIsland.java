package xyz.nucleoid.bedwars.game.generator.island;

import java.util.Random;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapConfig;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public final class BwTeamIsland {
    private static final double PI2 = Math.PI / 2.0;
    private static final int RADIUS = 10;

    final BlockPos origin;
    final BlockBounds bounds;
    final GameTeam team;
    private final double angle;

    public BwTeamIsland(BlockPos origin, GameTeam team, double angle) {
        this.bounds = new BlockBounds(
                origin.add(-RADIUS, 0, -RADIUS),
                origin.add(RADIUS, 0, RADIUS)
        );

        this.origin = origin;
        this.team = team;
        this.angle = angle;
        System.out.println(team.getDye().getName() + " -> " + angle);
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

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        mutablePos.set(origin.getX(), origin.getY() + 1, origin.getZ() - 2);
        template.setBlockState(mutablePos, Blocks.CHEST.getDefaultState());

        mutablePos.set(origin.getX(), origin.getY() + 1, origin.getZ() + 2);
        template.setBlockState(mutablePos, Blocks.ENDER_CHEST.getDefaultState());

        Direction direction;
        int ax = 0;
        int az = 0;

        if (this.angle >= PI2 * 3) {
            az = 5;
            direction = Direction.SOUTH;
        } else if (this.angle >= PI2 * 2) {
            ax = 5;
            direction = Direction.EAST;
        } else if (this.angle >= PI2 * 1) {
            az = -5;
            direction = Direction.NORTH;
        } else {
            ax = -5;
            direction = Direction.WEST;
        }

        BlockState bed = ColoredBlocks.bed(this.team.getDye()).getDefaultState()
                .with(BedBlock.FACING, direction);

        mutablePos.set(origin.getX() + ax, origin.getY() + 1, origin.getZ() + az);
        template.setBlockState(mutablePos, bed.with(BedBlock.PART, BedPart.FOOT));
        mutablePos.move(direction, 1);
        template.setBlockState(mutablePos, bed.with(BedBlock.PART, BedPart.HEAD));

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

        BlockBounds chest = BlockBounds.of(this.origin.add(0, 1, -2));
        BlockBounds enderChest = BlockBounds.of(this.origin.add(0, 1, 2));
        BlockBounds teamShop = BlockBounds.of(this.origin.add(-2, 1, -1));
        BlockBounds itemShop = BlockBounds.of(this.origin.add(-2, 1, 1));

        Direction direction;
        int ax = 0;
        int az = 0;

        if (this.angle >= PI2 * 3) {
            az = 5;
            direction = Direction.SOUTH;
        } else if (this.angle >= PI2 * 2) {
            ax = 5;
            direction = Direction.EAST;
        } else if (this.angle >= PI2 * 1) {
            az = -5;
            direction = Direction.NORTH;
        } else {
            ax = -5;
            direction = Direction.WEST;
        }

        BlockBounds bed = new BlockBounds(
                this.origin.add(ax, 1, ax),
                this.origin.add(ax + direction.getOffsetX(), 1, az + direction.getOffsetZ())
        );

        map.addProtectedBlocks(this.bounds);
        map.addProtectedBlocks(chest);
        map.addProtectedBlocks(enderChest);
        map.addProtectedBlocks(bed);

        map.addTeamRegions(this.team, new BwMap.TeamRegions(spawn, bed, base, itemShop, teamShop, chest));
    }
}
