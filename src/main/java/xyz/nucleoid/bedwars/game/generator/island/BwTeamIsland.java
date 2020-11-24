package xyz.nucleoid.bedwars.game.generator.island;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import xyz.nucleoid.plasmid.util.ColoredBlocks;

public final class BwTeamIsland {
    private static final int RADIUS = 10;

    final BlockPos origin;
    final BlockBounds bounds;
    final GameTeam team;

    public BwTeamIsland(BlockPos origin, GameTeam team) {
        this.bounds = new BlockBounds(
                origin.add(-RADIUS, 0, -RADIUS),
                origin.add(RADIUS, 0, RADIUS)
        );

        this.origin = origin;
        this.team = team;
    }

    public void addTo(BwMap map, MapTemplate template) {
        BlockPos origin = BwTeamIsland.this.origin;
        BlockState terracotta = ColoredBlocks.terracotta(BwTeamIsland.this.team.getDye()).getDefaultState();

        for (BlockPos pos : this.bounds) {
            int deltaX = pos.getX() - origin.getX();
            int deltaZ = pos.getZ() - origin.getZ();
            int radius = Math.max(Math.abs(deltaX), Math.abs(deltaZ));

            if (radius <= 1) {
                template.setBlockState(pos, Blocks.IRON_BLOCK.getDefaultState());
            } else {
                template.setBlockState(pos, terracotta);
            }
        }

        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        mutablePos.set(origin.getX(), origin.getY() + 1, origin.getZ() - 2);
        template.setBlockState(mutablePos, Blocks.CHEST.getDefaultState());

        mutablePos.set(origin.getX(), origin.getY() + 1, origin.getZ() + 2);
        template.setBlockState(mutablePos, Blocks.ENDER_CHEST.getDefaultState());

        BlockState bed = ColoredBlocks.bed(this.team.getDye()).getDefaultState()
                .with(BedBlock.FACING, Direction.EAST);

        mutablePos.set(origin.getX() + 5, origin.getY() + 1, origin.getZ());
        template.setBlockState(mutablePos, bed.with(BedBlock.PART, BedPart.FOOT));
        mutablePos.move(Direction.EAST, 1);
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
        BlockBounds bed = new BlockBounds(
                this.origin.add(5, 1, 0),
                this.origin.add(6, 1, 0)
        );

        map.addProtectedBlocks(this.bounds);
        map.addProtectedBlocks(chest);
        map.addProtectedBlocks(enderChest);
        map.addProtectedBlocks(bed);

        map.addTeamRegions(this.team, new BwMap.TeamRegions(spawn, bed, base, itemShop, teamShop, chest));
    }
}
