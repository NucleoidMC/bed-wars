package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;
import net.gegy1000.bedwars.map.GameMap;
import net.gegy1000.bedwars.map.GameMapBuilder;
import net.gegy1000.bedwars.map.provider.MapProvider;
import net.gegy1000.bedwars.util.BlockBounds;
import net.gegy1000.bedwars.util.ColoredBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class TestProceduralMapProvider implements MapProvider {
    private static final double ISLAND_DISTANCE = 20.0;

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin) {
        return CompletableFuture.supplyAsync(() -> {
            Generator generator = new Generator(
                    new GameMapBuilder(world, origin),
                    BedWars.TEAMS
            );

            return generator.generate();
        }, world.getServer());
    }

    static class Generator {
        final GameMapBuilder builder;
        final GameTeam[] teams;

        final Map<GameTeam, TeamIsland> teamIslands = new HashMap<>();

        Generator(GameMapBuilder builder, GameTeam[] teams) {
            this.builder = builder;
            this.teams = teams;
        }

        void addTeamIslands() {
            for (int i = 0; i < this.teams.length; i++) {
                GameTeam team = this.teams[i];

                double theta = ((double) i / this.teams.length) * (2 * Math.PI);
                double x = Math.cos(theta) * ISLAND_DISTANCE;
                double z = Math.sin(theta) * ISLAND_DISTANCE;

                BlockPos pos = new BlockPos(x, 0, z);
                this.teamIslands.put(team, new TeamIsland(pos, team));
            }
        }

        GameMap generate() {
            this.addTeamIslands();

            for (TeamIsland island : this.teamIslands.values()) {
                island.addTo(this.builder);
            }

            return this.builder.build();
        }
    }

    static class TeamIsland {
        final BlockPos origin;
        final GameTeam team;

        final BlockState terracotta;

        TeamIsland(BlockPos origin, GameTeam team) {
            this.origin = origin;
            this.team = team;

            this.terracotta = ColoredBlocks.terracotta(team.getColor()).getDefaultState();
        }

        void addTo(GameMapBuilder builder) {
            BlockPos.Mutable mutablePos = new BlockPos.Mutable();

            for (int z = -5; z <= 5; z++) {
                for (int x = -5; x <= 5; x++) {
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

            mutablePos.set(this.origin.getX() + 2, this.origin.getY() + 1, this.origin.getZ());
            builder.setBlockState(mutablePos, Blocks.RESPAWN_ANCHOR.getDefaultState());

            this.addRegionsTo(builder);
        }

        void addRegionsTo(GameMapBuilder builder) {
            builder.addRegion(this.marker("base"), new BlockBounds(
                    this.origin.add(-5, -1, -5),
                    this.origin.add(5, 5, 5)
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
                    this.origin.add(2, 1, 0)
            ));
        }

        String marker(String name) {
            return this.team.getKey() + "_" + name;
        }
    }
}
