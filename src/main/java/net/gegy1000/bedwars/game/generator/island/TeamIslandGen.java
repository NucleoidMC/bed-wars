package net.gegy1000.bedwars.game.generator.island;

import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.gegy1000.gl.world.BlockBounds;
import net.gegy1000.gl.util.ColoredBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class TeamIslandGen implements MapGen {
    private static final int RAD = 10;

    final BlockPos origin;
    final GameTeam team;

    final BlockState terracotta;

    public TeamIslandGen(BlockPos origin, GameTeam team) {
        this.origin = origin;
        this.team = team;

        this.terracotta = ColoredBlocks.terracotta(team.getDye()).getDefaultState();
    }

    @Override
    public void addTo(GameMapBuilder builder) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int z = -RAD; z <= RAD; z++) {
            for (int x = -RAD; x <= RAD; x++) {
                mutablePos.set(this.origin.getX() + x, this.origin.getY(), this.origin.getZ() + z);

                int radius = Math.max(Math.abs(x), Math.abs(z));
                if (radius <= 1) {
                    builder.setBlockState(mutablePos, Blocks.IRON_BLOCK.getDefaultState());
                } else {
                    builder.setBlockState(mutablePos, this.terracotta);
                }
            }
        }

        mutablePos.set(this.origin.getX() - 2, this.origin.getY() + 1, this.origin.getZ());
        builder.setBlockState(mutablePos, Blocks.CHEST.getDefaultState());

        mutablePos.set(this.origin.getX() + 5, this.origin.getY() + 1, this.origin.getZ());
        builder.setBlockState(mutablePos, Blocks.RESPAWN_ANCHOR.getDefaultState());

        this.addRegionsTo(builder);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {
        builder.addRegion(this.marker("base"), new BlockBounds(
                this.origin.add(-RAD, -1, -RAD),
                this.origin.add(RAD, 5, RAD)
        ));

        builder.addRegion(this.marker("spawn"), new BlockBounds(
                this.origin.add(-1, 1, -1),
                this.origin.add(1, 1, 1)
        ));

        builder.addRegion(this.marker("chest"), new BlockBounds(
                this.origin.add(-2, 1, 0)
        ));

        builder.addRegion(this.marker("team_shop"), new BlockBounds(
                this.origin.add(-2, 1, -1)
        ));

        builder.addRegion(this.marker("item_shop"), new BlockBounds(
                this.origin.add(-2, 1, 1)
        ));

        builder.addRegion(this.marker("bed"), new BlockBounds(
                this.origin.add(5, 1, 0)
        ));
    }

    String marker(String name) {
        return this.team.getKey() + "_" + name;
    }
}
