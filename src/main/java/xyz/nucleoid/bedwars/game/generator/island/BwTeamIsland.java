package xyz.nucleoid.bedwars.game.generator.island;

import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapConfig;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.util.ColoredBlocks;

import java.util.Random;

public final class BwTeamIsland {
    private static final int RADIUS = 10;

    final BlockPos origin;
    final BlockBounds bounds;
    final GameTeam team;
    private final Direction direction;

    public BwTeamIsland(BlockPos origin, GameTeam team, double angle) {
        this.bounds = BlockBounds.of(
                origin.offset(-RADIUS, 0, -RADIUS),
                origin.offset(RADIUS, 0, RADIUS)
        );

        this.origin = origin;
        this.team = team;

        this.direction = Direction.fromYRot(Math.toDegrees(angle) + 90.0);
    }

    public void addTo(BwSkyMapConfig config, BwMap map, MapTemplate template) {
        BlockPos origin = this.origin;
        BlockState terracotta = ColoredBlocks.terracotta(this.team.config().blockDyeColor()).defaultBlockState();
        Random random = new Random();

        for (BlockPos pos : this.bounds) {
            int deltaX = pos.getX() - origin.getX();
            int deltaZ = pos.getZ() - origin.getZ();

            BlockState state = config.theme.teamIslandState(random, terracotta);

            if (Math.abs(deltaX) == RADIUS || Math.abs(deltaZ) == RADIUS) {
                state = terracotta;
            }

            int radius = Math.max(Math.abs(deltaX), Math.abs(deltaZ));

            if (radius <= 1) {
                template.setBlockState(pos, Blocks.IRON_BLOCK.defaultBlockState());
            } else {
                template.setBlockState(pos, state);
            }
        }

        template.setBlockState(this.transformPosition(-1, 1, -2), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, this.direction));
        template.setBlockState(this.transformPosition(1, 1, -2), Blocks.ENDER_CHEST.defaultBlockState().setValue(ChestBlock.FACING, this.direction));

        BlockState bed = ColoredBlocks.bed(this.team.config().blockDyeColor()).defaultBlockState()
                .setValue(BedBlock.FACING, this.direction);

        template.setBlockState(this.transformPosition(0, 1, 5), bed.setValue(BedBlock.PART, BedPart.FOOT));
        template.setBlockState(this.transformPosition(0, 1, 6), bed.setValue(BedBlock.PART, BedPart.HEAD));

        this.addRegionsTo(map);
    }

    private void addRegionsTo(BwMap map) {
        BlockBounds spawn = BlockBounds.of(
                this.origin.offset(-1, 1, -1),
                this.origin.offset(1, 1, 1)
        );

        BlockBounds base = BlockBounds.of(
                this.bounds.min().below(1),
                this.bounds.max().above(4)
        );

        BlockBounds chest = BlockBounds.ofBlock(this.transformPosition(-1, 1, -2));
        BlockBounds enderChest = BlockBounds.ofBlock(this.transformPosition(1, 1, -2));
        BlockBounds teamShop = BlockBounds.ofBlock(this.transformPosition(-2, 1, -1));
        BlockBounds itemShop = BlockBounds.ofBlock(this.transformPosition(-2, 1, 1));

        BlockBounds bed = BlockBounds.of(
                this.transformPosition(0, 1, 5),
                this.transformPosition(0, 1, 6)
        );

        map.addProtectedBlocks(this.bounds);
        map.addProtectedBlocks(chest);
        map.addProtectedBlocks(enderChest);
        map.addProtectedBlocks(bed);

        Direction shopDirection = this.direction.getClockWise();
        map.addTeamRegions(this.team.key(), new BwMap.TeamRegions(spawn, bed, base, chest, itemShop, teamShop, shopDirection, shopDirection), map.pools);
    }

    private BlockPos transformPosition(int x, int y, int z) {
        Direction forward = this.direction;
        Direction side = this.direction.getClockWise();

        return this.origin.offset(
                side.getStepX() * x + side.getStepZ() * z,
                y,
                forward.getStepX() * x + forward.getStepZ() * z
        );
    }
}
