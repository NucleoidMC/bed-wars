package net.gegy1000.bedwars.game.bw;

import com.mojang.serialization.Codec;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.GameTeam;
import net.gegy1000.bedwars.game.bw.gen.MapGen;
import net.gegy1000.bedwars.game.bw.gen.island.CenterIslandGen;
import net.gegy1000.bedwars.game.bw.gen.island.DiamondIslandGen;
import net.gegy1000.bedwars.game.bw.gen.island.RandomSmallIslandGen;
import net.gegy1000.bedwars.game.bw.gen.island.TeamIslandGen;
import net.gegy1000.bedwars.map.GameMap;
import net.gegy1000.bedwars.map.GameMapBuilder;
import net.gegy1000.bedwars.map.provider.MapProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class TestProceduralMapProvider implements MapProvider<BedWarsConfig> {
    public static final Codec<TestProceduralMapProvider> CODEC = Codec.unit(TestProceduralMapProvider::new);

    private static final double SPAWN_ISLAND_DISTANCE = 120.0;
    private static final double DIAMOND_ISLAND_DISTANCE = 65.0;

    public static void register() {
        MapProvider.REGISTRY.register(new Identifier(BedWarsMod.ID, "procedural"), CODEC);
    }

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, BedWarsConfig config) {
        Generator generator = new Generator(
                new GameMapBuilder(world, origin),
                config.getTeams()
        );

        return generator.generate(world.getServer(), world.random);
    }

    @Override
    public Codec<? extends MapProvider<BedWarsConfig>> getCodec() {
        return Codec.unit(TestProceduralMapProvider::new);
    }

    private static class Generator {
        private final GameMapBuilder builder;
        private final List<GameTeam> teams;

        private final List<MapGen> islands = new ArrayList<>();

        private Generator(GameMapBuilder builder, List<GameTeam> teams) {
            this.builder = builder;
            this.teams = teams;
        }

        private void addIslands(Random random) {
            // Add team islands
            for (int i = 0; i < this.teams.size(); i++) {
                GameTeam team = this.teams.get(i);

                double theta = ((double) i / this.teams.size()) * (2 * Math.PI);
                double x = Math.cos(theta) * SPAWN_ISLAND_DISTANCE;
                double z = Math.sin(theta) * SPAWN_ISLAND_DISTANCE;

                BlockPos pos = new BlockPos(x, 8, z);
                islands.add(new TeamIslandGen(pos, team));
            }

            // Add center island
            islands.add(new CenterIslandGen(new BlockPos(0, 8, 0), random.nextLong()));

            islands.add(new DiamondIslandGen(new BlockPos(DIAMOND_ISLAND_DISTANCE, 8, 0), random.nextLong()));
            islands.add(new DiamondIslandGen(new BlockPos(-DIAMOND_ISLAND_DISTANCE, 8, 0), random.nextLong()));
            islands.add(new DiamondIslandGen(new BlockPos(0, 8, DIAMOND_ISLAND_DISTANCE), random.nextLong()));
            islands.add(new DiamondIslandGen(new BlockPos(0, 8, -DIAMOND_ISLAND_DISTANCE), random.nextLong()));

            for (int i = 0; i < 10; i++) {
                int aX = random.nextInt(80) - random.nextInt(80);
                int aY = random.nextInt(10) - random.nextInt(10);
                int aZ = random.nextInt(80) - random.nextInt(80);
                long seed = random.nextLong();

                // Avoid generating at the center
                if (Math.abs(aX) < 25 && Math.abs(aZ) < 25) {
                    continue;
                }

                // Add symmetrical islands
                islands.add(new RandomSmallIslandGen(new BlockPos(aX, 8 + aY, aZ), seed));
                islands.add(new RandomSmallIslandGen(new BlockPos(-aX, 8 + aY, -aZ), seed));
            }
        }

        private CompletableFuture<GameMap> generate(MinecraftServer server, Random random) {
            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
            this.addIslands(random);

            System.out.println("Starting island generation...");
            for (MapGen island : islands) {
                future = future.thenAcceptAsync(v -> island.addTo(this.builder), server);
            }

            return future.thenApplyAsync(v -> this.builder.build(), server);
        }
    }
}
