package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.BedWars;
import net.gegy1000.bedwars.custom.ShopVillagerEntity;
import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.bedwars.game.active.BwItemGenerator;
import net.gegy1000.bedwars.game.active.ItemGeneratorPool;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.map.GameMap;
import net.gegy1000.gl.world.BlockBounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class BwMap {
    private final ServerWorld world;
    private final GameMap map;

    private final Map<GameTeam, TeamSpawn> teamSpawns = new HashMap<>();
    private final Map<GameTeam, TeamRegions> teamRegions = new HashMap<>();

    private final Collection<BwItemGenerator> generators = new ArrayList<>();

    private BwMap(GameMap map) {
        this.world = map.getWorld();
        this.map = map;
    }

    public static BwMap open(GameMap map, BwConfig config) {
        BwMap bwMap = new BwMap(map);
        bwMap.initializeMap(config);
        return bwMap;
    }

    private void initializeMap(BwConfig config) {
        this.map.getRegions("diamond_spawn").forEach(bounds -> {
            this.generators.add(new BwItemGenerator(bounds)
                    .setPool(ItemGeneratorPool.DIAMOND)
                    .maxItems(6)
                    .addTimerText()
            );
        });

        this.map.getRegions("emerald_spawn").forEach(bounds -> {
            this.generators.add(new BwItemGenerator(bounds)
                    .setPool(ItemGeneratorPool.EMERALD)
                    .maxItems(3)
                    .addTimerText()
            );
        });

        for (GameTeam team : config.getTeams()) {
            TeamRegions regions = TeamRegions.read(team, this.map);

            if (regions.spawn != null) {
                TeamSpawn teamSpawn = new TeamSpawn(regions.spawn);
                this.teamSpawns.put(team, teamSpawn);
                this.generators.add(teamSpawn.generator);
            } else {
                BedWars.LOGGER.warn("Missing spawn for {}", team.getKey());
            }

            this.teamRegions.put(team, regions);
        }
    }

    public void spawnShopkeepers(BwActive game, BwConfig config) {
        for (GameTeam team : config.getTeams()) {
            TeamRegions regions = this.getTeamRegions(team);

            if (regions.teamShop != null) {
                this.trySpawnEntity(ShopVillagerEntity.team(this.world, game), regions.teamShop);
            } else {
                BedWars.LOGGER.warn("Missing team shop for {}", team.getDisplay());
            }

            if (regions.itemShop != null) {
                this.trySpawnEntity(ShopVillagerEntity.item(this.world, game), regions.itemShop);
            } else {
                BedWars.LOGGER.warn("Missing item shop for {}", team.getDisplay());
            }
        }
    }

    private boolean trySpawnEntity(Entity entity, BlockBounds bounds) {
        Vec3d center = bounds.getCenter();

        entity.refreshPositionAndAngles(center.x, bounds.getMin().getY(), center.z, 0.0F, 0.0F);

        if (entity instanceof MobEntity) {
            MobEntity mob = (MobEntity) entity;

            LocalDifficulty difficulty = this.world.getLocalDifficulty(mob.getBlockPos());
            mob.initialize(this.world, difficulty, SpawnReason.COMMAND, null, null);
        }

        if (!this.world.spawnEntity(entity)) {
            BedWars.LOGGER.warn("Tried to spawn entity ({}) but the chunk was not loaded", entity);
            return false;
        }

        return true;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    @Nullable
    public TeamSpawn getTeamSpawn(GameTeam team) {
        return this.teamSpawns.get(team);
    }

    @Nonnull
    public TeamRegions getTeamRegions(GameTeam team) {
        return this.teamRegions.getOrDefault(team, TeamRegions.EMPTY);
    }

    public Collection<BwItemGenerator> getGenerators() {
        return this.generators;
    }

    public void delete() {
        for (BwItemGenerator generator : this.generators) {
            generator.remove();
        }
    }

    public boolean isProtectedBlock(BlockPos pos) {
        return this.map.isProtectedBlock(pos);
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
        static final TeamRegions EMPTY = new TeamRegions(null, null, null, null, null, null);

        public final BlockBounds base;
        public final BlockBounds spawn;
        public final BlockBounds bed;
        public final BlockBounds itemShop;
        public final BlockBounds teamShop;
        public final BlockBounds teamChest;

        TeamRegions(BlockBounds spawn, BlockBounds bed, BlockBounds base, BlockBounds itemShop, BlockBounds teamShop, BlockBounds teamChest) {
            this.spawn = spawn;
            this.bed = bed;
            this.base = base;
            this.itemShop = itemShop;
            this.teamShop = teamShop;
            this.teamChest = teamChest;
        }

        static TeamRegions read(GameTeam team, GameMap map) {
            String teamKey = team.getKey();

            // TODO: consolidate the team tags and check for ones contained within the team_base
            BlockBounds base = map.getFirstRegion(teamKey + "_base");
            BlockBounds spawn = map.getFirstRegion(teamKey + "_spawn");
            BlockBounds bed = map.getFirstRegion(teamKey + "_bed");
            BlockBounds itemShop = map.getFirstRegion(teamKey + "_item_shop");
            BlockBounds teamShop = map.getFirstRegion(teamKey + "_team_shop");
            BlockBounds teamChest = map.getFirstRegion(teamKey + "_chest");

            return new TeamRegions(spawn, bed, base, itemShop, teamShop, teamChest);
        }
    }
}
