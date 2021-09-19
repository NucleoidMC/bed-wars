package xyz.nucleoid.bedwars.game.generator;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.bedwars.game.BwConfig;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.generator.island.BwCenterIsland;
import xyz.nucleoid.bedwars.game.generator.island.BwDiamondIsland;
import xyz.nucleoid.bedwars.game.generator.island.BwTeamIsland;
import xyz.nucleoid.bedwars.game.generator.island.NoiseIslandConfig;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BwSkyMapBuilder {
    private final BwConfig config;
    private final BwSkyMapConfig skyConfig;

    public BwSkyMapBuilder(BwConfig config, BwSkyMapConfig skyConfig) {
        this.config = config;
        this.skyConfig = skyConfig;
    }

    public BwMap build(MinecraftServer server) {
        BwMap map = new BwMap();

        MapTemplate template = MapTemplate.createEmpty();

        BwCenterIsland centerIsland = this.buildCenterIsland();
        List<BwDiamondIsland> diamondIslands = this.buildDiamondIslands();
        List<BwTeamIsland> teamIslands = this.buildTeamIslands();

        Random random = new Random();
        long seed = random.nextLong();

        centerIsland.addTo(map, template, seed, this.config.teams().list().size(), this.skyConfig.emeraldSpawnerDistance);

        for (BwDiamondIsland diamondIsland : diamondIslands) {
            diamondIsland.addTo(map, template, seed);
        }

        for (BwTeamIsland teamIsland : teamIslands) {
            teamIsland.addTo(this.skyConfig, map, template);
        }

        this.addSmallIslands(template, random, seed);

        template.setBiome(this.skyConfig.theme.getFakingBiome());

        BwSkyChunkGenerator generator = new BwSkyChunkGenerator(map, this.skyConfig, server, template);
        map.setChunkGenerator(generator);

        return map;
    }

    private BwCenterIsland buildCenterIsland() {
        return new BwCenterIsland(this.skyConfig.centerGenerator, new BlockPos(0, 72, 0));
    }

    private List<BwDiamondIsland> buildDiamondIslands() {
        List<BwDiamondIsland> diamondIslands = new ArrayList<>();

        // TODO: vary based on team count
        NoiseIslandConfig diamondGenerator = this.skyConfig.diamondGenerator;
        double diamondIslandDistance = this.skyConfig.diamondIslandDistance;

        diamondIslands.add(new BwDiamondIsland(diamondGenerator, new BlockPos(diamondIslandDistance, 72, diamondIslandDistance)));
        diamondIslands.add(new BwDiamondIsland(diamondGenerator, new BlockPos(-diamondIslandDistance, 72, diamondIslandDistance)));
        diamondIslands.add(new BwDiamondIsland(diamondGenerator, new BlockPos(diamondIslandDistance, 72, -diamondIslandDistance)));
        diamondIslands.add(new BwDiamondIsland(diamondGenerator, new BlockPos(-diamondIslandDistance, 72, -diamondIslandDistance)));

        return diamondIslands;
    }

    private List<BwTeamIsland> buildTeamIslands() {
        List<BwTeamIsland> teamIslands = new ArrayList<>();

        var teams = this.config.teams().list();

        for (int i = 0; i < teams.size(); i++) {
            var team = teams.get(i);

            double theta = ((double) i / teams.size()) * (2 * Math.PI);
            double x = Math.cos(theta) * this.skyConfig.spawnIslandDistance;
            double z = Math.sin(theta) * this.skyConfig.spawnIslandDistance;

            BlockPos pos = new BlockPos(x, 72, z);
            teamIslands.add(new BwTeamIsland(pos, team, theta));

        }

        return teamIslands;
    }

    private void addSmallIslands(MapTemplate template, Random random, long seed) {
        for (int i = 0; i < this.skyConfig.smallIslandCount; i++) {
            int x = random.nextInt(this.skyConfig.smallIslandHorizontalSpread) - random.nextInt(this.skyConfig.smallIslandHorizontalSpread);
            int y = random.nextInt(this.skyConfig.smallIslandVerticalSpread) - random.nextInt(this.skyConfig.smallIslandVerticalSpread);
            int z = random.nextInt(this.skyConfig.smallIslandHorizontalSpread) - random.nextInt(this.skyConfig.smallIslandHorizontalSpread);

            // Avoid generating at the center
            if (Math.abs(x) < this.skyConfig.smallIslandCutoff && Math.abs(z) < this.skyConfig.smallIslandCutoff) {
                continue;
            }

            // Add symmetrical islands
            NoiseIslandConfig smallIsland = this.skyConfig.smallIslandGenerator;
            smallIsland.createGenerator(new BlockPos(x, 72 + y, z), seed).addTo(template);
            smallIsland.createGenerator(new BlockPos(-x, 72 + y, -z), seed).addTo(template);
        }
    }
}
