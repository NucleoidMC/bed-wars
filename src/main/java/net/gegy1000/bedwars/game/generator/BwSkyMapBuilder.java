package net.gegy1000.bedwars.game.generator;

import net.gegy1000.bedwars.game.BwConfig;
import net.gegy1000.bedwars.game.BwMap;
import net.gegy1000.bedwars.game.generator.island.BwCenterIsland;
import net.gegy1000.bedwars.game.generator.island.BwDiamondIsland;
import net.gegy1000.bedwars.game.generator.island.BwTeamIsland;
import net.gegy1000.bedwars.game.generator.island.NoiseIslandConfig;
import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.game.player.GameTeam;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biomes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class BwSkyMapBuilder {
    private final BwConfig config;

    public BwSkyMapBuilder(BwConfig config) {
        this.config = config;
    }

    public CompletableFuture<BwMap> create() {
        return CompletableFuture.supplyAsync(this::build, Util.getServerWorkerExecutor());
    }

    private BwMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        BwMap map = new BwMap();

        BwSkyMapConfig mapConfig = this.config.map;

        BwCenterIsland centerIsland = this.buildCenterIsland(mapConfig);
        List<BwDiamondIsland> diamondIslands = this.buildDiamondIslands(mapConfig);
        List<BwTeamIsland> teamIslands = this.buildTeamIslands(mapConfig);

        Random random = new Random();

        centerIsland.addTo(map, template, random.nextLong());

        for (BwDiamondIsland diamondIsland : diamondIslands) {
            diamondIsland.addTo(map, template, random.nextLong());
        }

        for (BwTeamIsland teamIsland : teamIslands) {
            teamIsland.addTo(map, template);
        }

        this.addSmallIslands(template, mapConfig, random);

        template.setBiome(Biomes.PLAINS);

        BwSkyChunkGenerator generator = new BwSkyChunkGenerator(template);
        map.setChunkGenerator(generator);

        return map;
    }

    private BwCenterIsland buildCenterIsland(BwSkyMapConfig mapConfig) {
        return new BwCenterIsland(mapConfig.centerGenerator, new BlockPos(0, 72, 0));
    }

    private List<BwDiamondIsland> buildDiamondIslands(BwSkyMapConfig mapConfig) {
        List<BwDiamondIsland> diamondIslands = new ArrayList<>();

        // TODO: vary based on team count
        diamondIslands.add(new BwDiamondIsland(mapConfig.diamondGenerator, new BlockPos(mapConfig.diamondIslandDistance, 72, mapConfig.diamondIslandDistance)));
        diamondIslands.add(new BwDiamondIsland(mapConfig.diamondGenerator, new BlockPos(-mapConfig.diamondIslandDistance, 72, mapConfig.diamondIslandDistance)));
        diamondIslands.add(new BwDiamondIsland(mapConfig.diamondGenerator, new BlockPos(mapConfig.diamondIslandDistance, 72, -mapConfig.diamondIslandDistance)));
        diamondIslands.add(new BwDiamondIsland(mapConfig.diamondGenerator, new BlockPos(-mapConfig.diamondIslandDistance, 72, -mapConfig.diamondIslandDistance)));

        return diamondIslands;
    }

    private List<BwTeamIsland> buildTeamIslands(BwSkyMapConfig mapConfig) {
        List<BwTeamIsland> teamIslands = new ArrayList<>();

        List<GameTeam> teams = this.config.teams;
        for (int i = 0; i < teams.size(); i++) {
            GameTeam team = teams.get(i);

            double theta = ((double) i / teams.size()) * (2 * Math.PI);
            double x = Math.cos(theta) * mapConfig.spawnIslandDistance;
            double z = Math.sin(theta) * mapConfig.spawnIslandDistance;

            BlockPos pos = new BlockPos(x, 72, z);
            teamIslands.add(new BwTeamIsland(pos, team));
        }

        return teamIslands;
    }

    private void addSmallIslands(MapTemplate template, BwSkyMapConfig config, Random random) {
        for (int i = 0; i < config.smallIslandCount; i++) {
            int x = random.nextInt(config.smallIslandHorizontalSpread) - random.nextInt(config.smallIslandHorizontalSpread);
            int y = random.nextInt(config.smallIslandVerticalSpread) - random.nextInt(config.smallIslandVerticalSpread);
            int z = random.nextInt(config.smallIslandHorizontalSpread) - random.nextInt(config.smallIslandHorizontalSpread);

            // Avoid generating at the center
            if (Math.abs(x) < config.smallIslandCutoff && Math.abs(z) < config.smallIslandCutoff) {
                continue;
            }

            long seed = random.nextLong();

            // Add symmetrical islands
            NoiseIslandConfig smallIsland = config.smallIslandGenerator;
            smallIsland.createGenerator(new BlockPos(x, 72 + y, z), seed).addTo(template);
            smallIsland.createGenerator(new BlockPos(-x, 72 + y, -z), seed).addTo(template);
        }
    }
}
