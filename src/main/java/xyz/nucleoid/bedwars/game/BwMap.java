package xyz.nucleoid.bedwars.game;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.custom.ShopVillagerEntity;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwItemGenerator;
import xyz.nucleoid.bedwars.game.active.ItemGeneratorPool;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BwMap {
    private ChunkGenerator chunkGenerator;

    private final Map<GameTeam, TeamSpawn> teamSpawns = new HashMap<>();
    private final Map<GameTeam, TeamRegions> teamRegions = new HashMap<>();

    private final Collection<BwItemGenerator> itemGenerators = new ArrayList<>();

    private BlockPos centerSpawn = BlockPos.ORIGIN;

    private final LongSet protectedBlocks = new LongOpenHashSet();

    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    public void addDiamondGenerator(BlockBounds bounds) {
        this.itemGenerators.add(new BwItemGenerator(bounds)
                .setPool(ItemGeneratorPool.DIAMOND)
                .maxItems(6)
                .addTimerText()
        );

        this.addProtectedBlocks(bounds);
    }

    public void addEmeraldGenerator(BlockBounds bounds) {
        this.itemGenerators.add(new BwItemGenerator(bounds)
                .setPool(ItemGeneratorPool.EMERALD)
                .maxItems(3)
                .addTimerText()
        );

        this.addProtectedBlocks(bounds);
    }

    public void addTeamRegions(GameTeam team, TeamRegions regions) {
        this.teamRegions.put(team, regions);

        if (regions.spawn != null) {
            TeamSpawn teamSpawn = new TeamSpawn(regions.spawn);
            this.teamSpawns.put(team, teamSpawn);
            this.itemGenerators.add(teamSpawn.generator);
        } else {
            BedWars.LOGGER.warn("Missing spawn for {}", team.getKey());
        }
    }

    public void setCenterSpawn(BlockPos pos) {
        this.centerSpawn = pos;
    }

    public void addProtectedBlock(long pos) {
        this.protectedBlocks.add(pos);
    }

    public void addProtectedBlocks(BlockBounds bounds) {
        for (BlockPos pos : bounds.iterate()) {
            this.protectedBlocks.add(pos.asLong());
        }
    }

    public void spawnShopkeepers(ServerWorld world, BwActive game, BwConfig config) {
        for (GameTeam team : config.teams) {
            TeamRegions regions = this.getTeamRegions(team);

            if (regions.teamShop != null) {
                this.trySpawnEntity(ShopVillagerEntity.team(world, game), regions.teamShop);
            } else {
                BedWars.LOGGER.warn("Missing team shop for {}", team.getDisplay());
            }

            if (regions.itemShop != null) {
                this.trySpawnEntity(ShopVillagerEntity.item(world, game), regions.itemShop);
            } else {
                BedWars.LOGGER.warn("Missing item shop for {}", team.getDisplay());
            }
        }
    }

    private void trySpawnEntity(Entity entity, BlockBounds bounds) {
        Vec3d center = bounds.getCenter();

        entity.refreshPositionAndAngles(center.x, bounds.getMin().getY(), center.z, 0.0F, 0.0F);

        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;

            LocalDifficulty difficulty = entity.world.getLocalDifficulty(mob.getBlockPos());
            mob.initialize((ServerWorld) entity.world, difficulty, SpawnReason.COMMAND, null, null);
        }

        if (!entity.world.spawnEntity(entity)) {
            BedWars.LOGGER.warn("Tried to spawn entity ({}) but the chunk was not loaded", entity);
        }
    }

    @Nullable
    public TeamSpawn getTeamSpawn(GameTeam team) {
        return this.teamSpawns.get(team);
    }

    @Nonnull
    public TeamRegions getTeamRegions(GameTeam team) {
        return this.teamRegions.getOrDefault(team, TeamRegions.EMPTY);
    }

    public Collection<BwItemGenerator> getItemGenerators() {
        return this.itemGenerators;
    }

    public boolean isProtectedBlock(BlockPos pos) {
        return this.protectedBlocks.contains(pos.asLong());
    }

    public BlockPos getCenterSpawn() {
        return this.centerSpawn;
    }

    public ChunkGenerator getChunkGenerator() {
        return this.chunkGenerator;
    }

    public static class TeamSpawn {
        public static final int MAX_LEVEL = 3;

        private final BlockBounds region;
        private final BwItemGenerator generator;

        private int level = 1;

        TeamSpawn(BlockBounds region) {
            this.region = region;
            this.generator = new BwItemGenerator(region)
                    .setPool(poolForLevel(this.level))
                    .maxItems(64)
                    .allowDuplication();
        }

        public void placePlayer(ServerPlayerEntity player, ServerWorld world) {
            player.fallDistance = 0.0F;

            Vec3d center = this.region.getCenter();
            player.teleport(world, center.x, center.y + 0.5, center.z, 0.0F, 0.0F);
        }

        public void setLevel(int level) {
            this.level = Math.max(level, this.level);
            this.generator.setPool(poolForLevel(this.level));
        }

        public int getLevel() {
            return this.level;
        }

        private static ItemGeneratorPool poolForLevel(int level) {
            if (level == 1) {
                return ItemGeneratorPool.TEAM_LVL_1;
            } else if (level == 2) {
                return ItemGeneratorPool.TEAM_LVL_2;
            } else if (level == 3) {
                return ItemGeneratorPool.TEAM_LVL_3;
            }
            return ItemGeneratorPool.TEAM_LVL_1;
        }
    }

    public static class TeamRegions {
        public static final TeamRegions EMPTY = new TeamRegions(null, null, null, null, null, null);

        public final BlockBounds base;
        public final BlockBounds spawn;
        public final BlockBounds bed;
        public final BlockBounds itemShop;
        public final BlockBounds teamShop;
        public final BlockBounds teamChest;

        public TeamRegions(BlockBounds spawn, BlockBounds bed, BlockBounds base, BlockBounds itemShop, BlockBounds teamShop, BlockBounds teamChest) {
            this.spawn = spawn;
            this.bed = bed;
            this.base = base;
            this.itemShop = itemShop;
            this.teamShop = teamShop;
            this.teamChest = teamChest;
        }

        public static TeamRegions fromTemplate(GameTeam team, MapTemplate template) {
            String teamKey = team.getKey();

            BlockBounds base = template.getFirstRegion(teamKey + "_base");
            BlockBounds spawn = template.getFirstRegion(teamKey + "_spawn");
            BlockBounds bed = template.getFirstRegion(teamKey + "_bed");
            BlockBounds itemShop = template.getFirstRegion(teamKey + "_item_shop");
            BlockBounds teamShop = template.getFirstRegion(teamKey + "_team_shop");
            BlockBounds teamChest = template.getFirstRegion(teamKey + "_chest");

            return new TeamRegions(spawn, bed, base, itemShop, teamShop, teamChest);
        }
    }
}
