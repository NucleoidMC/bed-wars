package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;
import net.gegy1000.bedwars.game.bw.gen.MapGen;
import net.gegy1000.bedwars.game.bw.gen.island.CenterIslandGen;
import net.gegy1000.bedwars.game.bw.gen.island.DiamondIslandGen;
import net.gegy1000.bedwars.game.bw.gen.island.TeamIslandGen;
import net.gegy1000.bedwars.map.GameMap;
import net.gegy1000.bedwars.map.GameMapBuilder;
import net.gegy1000.bedwars.map.provider.MapProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TestProceduralMapProvider implements MapProvider {
    private static final double SPAWN_ISLAND_DISTANCE = 120.0;
    private static final double DIAMOND_ISLAND_DISTANCE = 65.0;

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

    private static class Generator {
        private final GameMapBuilder builder;
        private final GameTeam[] teams;

        private final List<MapGen> islands = new ArrayList<>();

        private Generator(GameMapBuilder builder, GameTeam[] teams) {
            this.builder = builder;
            this.teams = teams;
        }

        private void addIslands() {
            // Add team islands
            for (int i = 0; i < this.teams.length; i++) {
                GameTeam team = this.teams[i];

                double theta = ((double) i / this.teams.length) * (2 * Math.PI);
                double x = Math.cos(theta) * SPAWN_ISLAND_DISTANCE;
                double z = Math.sin(theta) * SPAWN_ISLAND_DISTANCE;

                BlockPos pos = new BlockPos(x, 8, z);
                islands.add(new TeamIslandGen(pos, team));
            }

            // Add center island
            islands.add(new CenterIslandGen(new BlockPos(0, 8, 0), System.currentTimeMillis()));

            islands.add(new DiamondIslandGen(new BlockPos(DIAMOND_ISLAND_DISTANCE, 8, 0), System.currentTimeMillis()));
            islands.add(new DiamondIslandGen(new BlockPos(-DIAMOND_ISLAND_DISTANCE, 8, 0), System.currentTimeMillis()));
            islands.add(new DiamondIslandGen(new BlockPos(0, 8, DIAMOND_ISLAND_DISTANCE), System.currentTimeMillis()));
            islands.add(new DiamondIslandGen(new BlockPos(0, 8, -DIAMOND_ISLAND_DISTANCE), System.currentTimeMillis()));
        }

        private GameMap generate() {
            this.addIslands();

            for (MapGen island : islands) {
                island.addTo(this.builder);
            }

            return this.builder.build();
        }
    }
}
