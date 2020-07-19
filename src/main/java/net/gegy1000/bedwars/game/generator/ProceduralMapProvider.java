package net.gegy1000.bedwars.game.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.bedwars.game.BwConfig;
import net.gegy1000.bedwars.game.generator.island.CenterIslandGen;
import net.gegy1000.bedwars.game.generator.island.DiamondIslandGen;
import net.gegy1000.bedwars.game.generator.island.RandomSmallIslandGen;
import net.gegy1000.bedwars.game.generator.island.TeamIslandGen;
import net.gegy1000.gl.game.map.GameMap;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.gegy1000.gl.game.map.provider.MapProvider;
import net.gegy1000.gl.world.BlockBounds;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class ProceduralMapProvider implements MapProvider<BwConfig> {
    public static final Codec<ProceduralMapProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Generator.CODEC.fieldOf("generator").forGetter(provider -> provider.generator)
    ).apply(instance, ProceduralMapProvider::new));

    private static final int HORIZONTAL_RADIUS = 150;
    private static final int VERTICAL_RADIUS = 40;
    private final Generator generator;

    public ProceduralMapProvider(Generator generator) {
        this.generator = generator;
    }

    public static void register() {
        MapProvider.REGISTRY.register(new Identifier(BedWarsMod.ID, "procedural"), CODEC);
    }

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, BwConfig config) {
        // TODO: abstract away the origin to avoid mistakes in coordinate systems
        BlockBounds bounds = new BlockBounds(
                new BlockPos(-HORIZONTAL_RADIUS, -VERTICAL_RADIUS, -HORIZONTAL_RADIUS),
                new BlockPos(HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS)
        );

        GameMapBuilder builder = GameMapBuilder.open(world, origin, bounds);
        return this.generator.generate(builder, config.getTeams(), world.getServer(), world.random);
    }

    @Override
    public Codec<? extends MapProvider<BwConfig>> getCodec() {
        return CODEC;
    }

    private static class Generator {
        public static final Codec<Generator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                NoiseIslandGen.CODEC.fieldOf("diamond_generator").forGetter(generator -> generator.diamondGenerator),
                NoiseIslandGen.CODEC.fieldOf("center_generator").forGetter(generator -> generator.centerGenerator),
                NoiseIslandGen.CODEC.fieldOf("small_island_generator").forGetter(generator -> generator.smallIslandGenerator),
                Codec.DOUBLE.fieldOf("spawn_island_distance").forGetter(generator -> generator.spawnIslandDistance),
                Codec.DOUBLE.fieldOf("diamond_island_distance").forGetter(generator -> generator.diamondIslandDistance),
                Codec.DOUBLE.fieldOf("small_island_count").forGetter(generator -> generator.smallIslandCount),
                Codec.INT.fieldOf("small_island_horizontal_spread").forGetter(generator -> generator.smallIslandHorizontalSpread),
                Codec.INT.fieldOf("small_island_vertical_spread").forGetter(generator -> generator.smallIslandVerticalSpread),
                Codec.INT.fieldOf("small_island_cutoff").forGetter(generator -> generator.smallIslandCutoff)
        ).apply(instance, Generator::new));

        private final List<MapGen> islands = new ArrayList<>();
        private final NoiseIslandGen diamondGenerator;
        private final NoiseIslandGen centerGenerator;
        private final NoiseIslandGen smallIslandGenerator;
        private final double spawnIslandDistance;
        private final double diamondIslandDistance;
        private final double smallIslandCount;
        private final int smallIslandHorizontalSpread;
        private final int smallIslandVerticalSpread;
        private final int smallIslandCutoff;

        private Generator(NoiseIslandGen diamondGenerator, NoiseIslandGen centerGenerator, NoiseIslandGen smallIslandGenerator, double spawnIslandDistance, double diamondIslandDistance, double smallIslandCount, int smallIslandHorizontalSpread, int smallIslandVerticalSpread, int smallIslandCutoff) {
            this.diamondGenerator = diamondGenerator;
            this.centerGenerator = centerGenerator;
            this.smallIslandGenerator = smallIslandGenerator;
            this.spawnIslandDistance = spawnIslandDistance;
            this.diamondIslandDistance = diamondIslandDistance;
            this.smallIslandCount = smallIslandCount;
            this.smallIslandHorizontalSpread = smallIslandHorizontalSpread;
            this.smallIslandVerticalSpread = smallIslandVerticalSpread;
            this.smallIslandCutoff = smallIslandCutoff;
        }

        private void addIslands(List<GameTeam> teams, Random random) {
            // Add team islands
            for (int i = 0; i < teams.size(); i++) {
                GameTeam team = teams.get(i);

                double theta = ((double) i / teams.size()) * (2 * Math.PI);
                double x = Math.cos(theta) * spawnIslandDistance;
                double z = Math.sin(theta) * spawnIslandDistance;

                BlockPos pos = new BlockPos(x, 8, z);
                islands.add(new TeamIslandGen(pos, team));
            }

            // Add center island
            islands.add(new CenterIslandGen(centerGenerator, new BlockPos(0, 8, 0), random.nextLong()));

            islands.add(new DiamondIslandGen(diamondGenerator, new BlockPos(diamondIslandDistance, 8, 0), random.nextLong()));
            islands.add(new DiamondIslandGen(diamondGenerator, new BlockPos(-diamondIslandDistance, 8, 0), random.nextLong()));
            islands.add(new DiamondIslandGen(diamondGenerator, new BlockPos(0, 8, diamondIslandDistance), random.nextLong()));
            islands.add(new DiamondIslandGen(diamondGenerator, new BlockPos(0, 8, -diamondIslandDistance), random.nextLong()));

            for (int i = 0; i < smallIslandCount; i++) {
                int aX = random.nextInt(this.smallIslandHorizontalSpread) - random.nextInt(this.smallIslandHorizontalSpread);
                int aY = random.nextInt(this.smallIslandVerticalSpread) - random.nextInt(this.smallIslandVerticalSpread);
                int aZ = random.nextInt(this.smallIslandHorizontalSpread) - random.nextInt(this.smallIslandHorizontalSpread);
                long seed = random.nextLong();

                // Avoid generating at the center
                if (Math.abs(aX) < this.smallIslandCutoff && Math.abs(aZ) < this.smallIslandCutoff) {
                    continue;
                }

                // Add symmetrical islands
                islands.add(new RandomSmallIslandGen(smallIslandGenerator, new BlockPos(aX, 8 + aY, aZ), seed));
                islands.add(new RandomSmallIslandGen(smallIslandGenerator, new BlockPos(-aX, 8 + aY, -aZ), seed));
            }
        }

        private CompletableFuture<GameMap> generate(GameMapBuilder builder, List<GameTeam> teams, MinecraftServer server, Random random) {
            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
            this.addIslands(teams, random);

            System.out.println("Starting island generation...");
            for (MapGen island : islands) {
                future = future.thenAcceptAsync(v -> island.addTo(builder), server);
            }

            return future.thenApplyAsync(v -> builder.build(), server);
        }
    }
}
