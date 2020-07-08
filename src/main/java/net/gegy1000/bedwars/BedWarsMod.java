package net.gegy1000.bedwars;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.gegy1000.bedwars.api.CustomizableEntity;
import net.gegy1000.bedwars.api.MapViewer;
import net.gegy1000.bedwars.api.RegionConstructor;
import net.gegy1000.bedwars.command.GameCommand;
import net.gegy1000.bedwars.command.MagicCommand;
import net.gegy1000.bedwars.command.MapCommand;
import net.gegy1000.bedwars.entity.CustomEntities;
import net.gegy1000.bedwars.entity.CustomEntity;
import net.gegy1000.bedwars.game.GameRegion;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.game.map.StagingMap;
import net.gegy1000.bedwars.item.CustomItem;
import net.gegy1000.bedwars.item.CustomItems;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BedWarsMod implements ModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        Reflection.initialize(CustomItems.class, CustomEntities.class);

        BedWars.initialize();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            MagicCommand.register(dispatcher);
            MapCommand.register(dispatcher);
            GameCommand.register(dispatcher);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            CustomItem custom = CustomItem.match(stack);
            if (custom != null) {
                return custom.onUse(player, world, hand);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof CustomizableEntity) {
                CustomizableEntity customizable = (CustomizableEntity) entity;
                CustomEntity customEntity = customizable.getCustomEntity();
                if (customEntity != null) {
                    return customEntity.interact(player, world, hand, entity, hitResult);
                }
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() % 10 != 0) {
                return;
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player instanceof MapViewer) {
                    MapViewer viewer = (MapViewer) player;
                    StagingMap viewing = viewer.getViewing();
                    if (viewing != null) {
                        this.displayMap(player, viewing);
                    }
                }

                if (player instanceof RegionConstructor) {
                    RegionConstructor regionConstructor = (RegionConstructor) player;
                    this.displayTracing(player, regionConstructor);
                }
            }
        });
    }

    private void displayMap(ServerPlayerEntity player, StagingMap viewing) {
        BlockBounds bounds = viewing.getBounds();
        renderOutline(player, bounds.getMin(), bounds.getMax(), 1.0F, 0.0F, 0.0F);

        for (GameRegion region : viewing.getRegions()) {
            BlockBounds regionBounds = region.getBounds();
            BlockPos min = regionBounds.getMin();
            BlockPos max = regionBounds.getMax();
            double distance = player.squaredDistanceTo(
                    (min.getX() + max.getX()) / 2.0,
                    (min.getY() + max.getY()) / 2.0,
                    (min.getZ() + max.getZ()) / 2.0
            );
            if (distance < 32 * 32) {
                renderOutline(player, min, max, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    private void displayTracing(ServerPlayerEntity player, RegionConstructor regionConstructor) {
        if (regionConstructor.isTracing()) {
            HitResult result = player.rayTrace(64.0, 1.0F, true);
            if (result.getType() == HitResult.Type.BLOCK) {
                regionConstructor.trace(new BlockPos(result.getPos()));
            }
        }

        PartialRegion tracing = regionConstructor.getTracing();
        if (tracing != null) {
            renderOutline(player, tracing.getMin(), tracing.getMax(), 0.0F, 1.0F, 0.0F);
        }
    }

    private static void renderOutline(ServerPlayerEntity player, BlockPos min, BlockPos max, float red, float green, float blue) {
        ServerWorld world = player.getServerWorld();

        DustParticleEffect effect = new DustParticleEffect(red, green, blue, 2.0F);

        if (min.equals(max)) {
            world.spawnParticles(
                    player, effect, true,
                    min.getX() + 0.5, min.getY() + 0.5, min.getZ() + 0.5,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
            return;
        }

        Edge[] edges = edges(min, max);

        for (Edge edge : edges) {
            int length = edge.length();
            for (int i = 0; i <= length; i++) {
                double m = (double) i / length;
                world.spawnParticles(
                        player, effect, true,
                        edge.projX(m), edge.projY(m), edge.projZ(m),
                        1,
                        0.0, 0.0, 0.0,
                        0.0
                );
            }
        }
    }

    private static Edge[] edges(BlockPos min, BlockPos max) {
        int minX = min.getX();
        int minY = min.getY();
        int minZ = min.getZ();
        int maxX = max.getX();
        int maxY = max.getY();
        int maxZ = max.getZ();

        return new Edge[] {
                // edges
                new Edge(minX, minY, minZ, minX, minY, maxZ),
                new Edge(minX, maxY, minZ, minX, maxY, maxZ),
                new Edge(maxX, minY, minZ, maxX, minY, maxZ),
                new Edge(maxX, maxY, minZ, maxX, maxY, maxZ),

                // front
                new Edge(minX, minY, minZ, minX, maxY, minZ),
                new Edge(maxX, minY, minZ, maxX, maxY, minZ),
                new Edge(minX, minY, minZ, maxX, minY, minZ),
                new Edge(minX, maxY, minZ, maxX, maxY, minZ),

                // back
                new Edge(minX, minY, maxZ, minX, maxY, maxZ),
                new Edge(maxX, minY, maxZ, maxX, maxY, maxZ),
                new Edge(minX, minY, maxZ, maxX, minY, maxZ),
                new Edge(minX, maxY, maxZ, maxX, maxY, maxZ),
        };
    }

    private static class Edge {
        final int startX, startY, startZ;
        final int endX, endY, endZ;

        Edge(int startX, int startY, int startZ, int endX, int endY, int endZ) {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.endX = endX;
            this.endY = endY;
            this.endZ = endZ;
        }

        double projX(double m) {
            return this.startX + (this.endX - this.startX) * m + 0.5;
        }

        double projY(double m) {
            return this.startY + (this.endY - this.startY) * m + 0.5;
        }

        double projZ(double m) {
            return this.startZ + (this.endZ - this.startZ) * m + 0.5;
        }

        int length() {
            int dx = this.endX - this.startX;
            int dy = this.endY - this.startY;
            int dz = this.endZ - this.startZ;
            return MathHelper.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz));
        }
    }
}
