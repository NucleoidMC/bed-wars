package xyz.nucleoid.bedwars.game;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapBuilder;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.game.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.game.player.GameTeam;

import java.util.concurrent.CompletableFuture;

public final class BwMapBuilder {
    private final BwConfig config;

    public BwMapBuilder(BwConfig config) {
        this.config = config;
    }

    public CompletableFuture<BwMap> create(MinecraftServer server) {
        return this.config.map.map(
                skyConfig -> new BwSkyMapBuilder(this.config, skyConfig).create(server),
                path -> MapTemplateSerializer.INSTANCE.load(path)
                        .thenApplyAsync(template -> {
                            return this.buildFromTemplate(server, template);
                        }, Util.getMainWorkerExecutor())
        );
    }

    private BwMap buildFromTemplate(MinecraftServer server, MapTemplate template) {
        BwMap map = new BwMap();

        template.getRegions("diamond_spawn").forEach(map::addDiamondGenerator);
        template.getRegions("emerald_spawn").forEach(map::addEmeraldGenerator);

        for (GameTeam team : this.config.teams) {
            BwMap.TeamRegions regions = BwMap.TeamRegions.fromTemplate(team, template);
            map.addTeamRegions(team, regions);
        }

        for (BlockPos pos : template.getBounds().iterate()) {
            BlockState state = template.getBlockState(pos);
            if (!state.isAir()) {
                map.addProtectedBlock(pos.asLong());
            }
        }

        map.setChunkGenerator(new TemplateChunkGenerator(server, template, BlockPos.ORIGIN));

        return map;
    }
}
