package xyz.nucleoid.bedwars.game;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.game.active.ItemGeneratorPools;
import xyz.nucleoid.bedwars.game.config.BwConfig;
import xyz.nucleoid.bedwars.custom.ShopVillagerEntity;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwItemGenerator;
import xyz.nucleoid.bedwars.game.active.ItemGeneratorPool;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.*;

public final class BwMap {
    private ChunkGenerator chunkGenerator;

    private final Map<GameTeamKey, TeamSpawn> teamSpawns = new Reference2ObjectOpenHashMap<>();
    private final Map<GameTeamKey, TeamRegions> teamRegions = new Reference2ObjectOpenHashMap<>();

    private final Collection<BwItemGenerator> itemGenerators = new ArrayList<>();

    private final List<BlockBounds> illegalBounds = new ArrayList<>();

    private BlockPos centerSpawn = BlockPos.ORIGIN;

    private final LongSet protectedBlocks = new LongOpenHashSet();

    public ItemGeneratorPools pools;

    public BwMap(BwConfig config) {
        this.pools = new ItemGeneratorPools(config);
    }

    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
    }

    public void addDiamondGenerator(BlockBounds bounds) {
        this.itemGenerators.add(new BwItemGenerator(bounds)
                .setPool(pools.DIAMOND)
                .maxItems(6)
                .addTimerText()
        );

        this.addProtectedBlocks(bounds);
    }

    public void addEmeraldGenerator(BlockBounds bounds) {
        this.itemGenerators.add(new BwItemGenerator(bounds)
                .setPool(pools.EMERALD)
                .maxItems(3)
                .addTimerText()
        );

        this.addProtectedBlocks(bounds);
    }

    public void addTeamRegions(GameTeamKey team, TeamRegions regions, ItemGeneratorPools pools) {
        this.teamRegions.put(team, regions);

        if (regions.spawn != null) {
            TeamSpawn teamSpawn = new TeamSpawn(regions.spawn, pools);
            this.teamSpawns.put(team, teamSpawn);
            this.itemGenerators.add(teamSpawn.generator);
        } else {
            BedWars.LOGGER.warn("Missing spawn for {}", team.id());
        }
    }

    public void setCenterSpawn(BlockPos pos) {
        this.centerSpawn = pos;
    }

    public void addProtectedBlock(long pos) {
        this.protectedBlocks.add(pos);
    }

    public void addProtectedBlocks(BlockBounds bounds) {
        for (BlockPos pos : bounds) {
            this.protectedBlocks.add(pos.asLong());
        }
    }

    public void addIllegalRegion(BlockBounds bounds) {
        this.illegalBounds.add(bounds);
    }

    public void spawnShopkeepers(ServerWorld world, BwActive game, BwConfig config) {
        for (GameTeam team : config.teams()) {
            TeamRegions regions = this.getTeamRegions(team.key());

            if (regions.teamShop != null) {
                this.trySpawnEntity(ShopVillagerEntity.team(world, game), regions.teamShop, regions.teamShopDirection);
            } else {
                BedWars.LOGGER.warn("Missing team shop for {}", team.key().id());
            }

            if (regions.itemShop != null) {
                this.trySpawnEntity(ShopVillagerEntity.item(world, game), regions.itemShop, regions.itemShopDirection);
            } else {
                BedWars.LOGGER.warn("Missing item shop for {}", team.key().id());
            }
        }
    }

    private void trySpawnEntity(Entity entity, BlockBounds bounds, Direction direction) {
        Vec3d center = bounds.center();

        float yaw = direction.asRotation();
        entity.refreshPositionAndAngles(center.x, bounds.min().getY(), center.z, yaw, 0.0F);

        if (entity instanceof MobEntity mob) {

            LocalDifficulty difficulty = entity.getWorld().getLocalDifficulty(mob.getBlockPos());
            mob.initialize((ServerWorld) entity.getWorld(), difficulty, SpawnReason.COMMAND, null);

            mob.headYaw = yaw;
            mob.bodyYaw = yaw;
        }

        // force-load the chunk before trying to spawn
        entity.getWorld().getChunk(MathHelper.floor(center.x) >> 4, MathHelper.floor(center.z) >> 4);
        entity.getWorld().spawnEntity(entity);
    }

    @Nullable
    public TeamSpawn getTeamSpawn(GameTeamKey team) {
        return this.teamSpawns.get(team);
    }

    @NotNull
    public TeamRegions getTeamRegions(GameTeamKey team) {
        return this.teamRegions.getOrDefault(team, TeamRegions.EMPTY);
    }

    public Map<GameTeamKey, TeamRegions> getAllTeamRegions() {
        return this.teamRegions;
    }

    public Collection<BwItemGenerator> getItemGenerators() {
        return this.itemGenerators;
    }

    public boolean isProtectedBlock(BlockPos pos) {
        return this.protectedBlocks.contains(pos.asLong());
    }

    public boolean isLegalAt(BlockPos pos) {
        for (BlockBounds bounds : this.illegalBounds) {
            if (bounds.contains(pos)) {
                return false;
            }
        }
        return true;
    }

    public Vec3d getCenterSpawn() {
        return Vec3d.ofBottomCenter(this.centerSpawn);
    }

    public ChunkGenerator getChunkGenerator() {
        return this.chunkGenerator;
    }

    public static class TeamSpawn {
        public static final int MAX_LEVEL = 3;

        private final BlockBounds region;
        private final BwItemGenerator generator;

        private int level = 1;

        TeamSpawn(BlockBounds region, ItemGeneratorPools pools) {
            this.region = region;
            this.generator = new BwItemGenerator(region)
                    .setPool(poolForLevel(this.level, pools))
                    .maxItems(64)
                    .allowDuplication();
        }

        public void placePlayer(ServerPlayerEntity player, ServerWorld world) {
            player.fallDistance = 0.0F;

            Vec3d center = this.region.center();
            player.teleport(world, center.x, center.y + 0.5, center.z, Set.of(), 0.0F, 0.0F, false);
        }

        public void setLevel(int level, ItemGeneratorPools pools) {
            this.level = Math.max(level, this.level);
            this.generator.setPool(poolForLevel(this.level, pools));
        }

        public int getLevel() {
            return this.level;
        }

        private static ItemGeneratorPool poolForLevel(int level, ItemGeneratorPools pools) {
            if (level == 2) {
                return pools.teamLvl2;
            } else if (level == 3) {
                return pools.teamLvl3;
            }
            return pools.teamLvl1;
        }
    }

    public record TeamRegions(
            BlockBounds spawn, BlockBounds bed,
            BlockBounds base, BlockBounds teamChest,
            BlockBounds itemShop,
            BlockBounds teamShop,
            Direction itemShopDirection,
            Direction teamShopDirection
    ) {
        public static final TeamRegions EMPTY = new TeamRegions(null, null, null, null, null, null, Direction.NORTH, Direction.NORTH);

        public static TeamRegions fromTemplate(GameTeamKey team, MapTemplateMetadata metadata) {
            String teamKey = team.id();

            BlockBounds base = metadata.getFirstRegionBounds(teamKey + "_base");
            BlockBounds spawn = metadata.getFirstRegionBounds(teamKey + "_spawn");
            BlockBounds bed = metadata.getFirstRegionBounds(teamKey + "_bed");
            BlockBounds teamChest = metadata.getFirstRegionBounds(teamKey + "_chest");

            BlockBounds itemShop = null;
            Direction itemShopDirection = Direction.NORTH;
            TemplateRegion itemShopRegion = metadata.getFirstRegion(teamKey + "_item_shop");
            if (itemShopRegion != null) {
                itemShop = itemShopRegion.getBounds();
                itemShopDirection = getDirectionForRegion(itemShopRegion);
            }

            BlockBounds teamShop = null;
            Direction teamShopDirection = Direction.NORTH;
            TemplateRegion teamShopRegion = metadata.getFirstRegion(teamKey + "_team_shop");
            if (teamShopRegion != null) {
                teamShop = teamShopRegion.getBounds();
                teamShopDirection = getDirectionForRegion(teamShopRegion);
            }

            return new TeamRegions(spawn, bed, base, teamChest, itemShop, teamShop, itemShopDirection, teamShopDirection);
        }

        private static Direction getDirectionForRegion(TemplateRegion region) {
            String key = region.getData().getString("direction");
            for (Direction direction : Direction.values()) {
                if (direction.getName().equalsIgnoreCase(key)) {
                    return direction;
                }
            }
            return Direction.NORTH;
        }
    }
}
