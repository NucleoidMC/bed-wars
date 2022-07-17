package xyz.nucleoid.bedwars.game.active;

import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.holograms.WorldHologram;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.map_templates.BlockBounds;

import java.util.List;

public final class BwItemGenerator {
    private final BlockBounds bounds;
    private ItemGeneratorPool pool;

    private long lastItemSpawn;

    private int maxItems = 4;
    private boolean allowDuplication;

    private boolean hasTimerText;
    private WorldHologram timerHologram;

    public BwItemGenerator(BlockBounds bounds) {
        this.bounds = bounds;
    }

    public BwItemGenerator setPool(ItemGeneratorPool pool) {
        this.pool = pool;
        return this;
    }

    public BwItemGenerator allowDuplication() {
        this.allowDuplication = true;
        return this;
    }

    public BwItemGenerator maxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    public BwItemGenerator addTimerText() {
        this.hasTimerText = true;
        return this;
    }

    public void tick(ServerWorld world, BwActive game) {
        if (this.pool == null) return;

        long time = world.getTime();

        if (this.hasTimerText) {
            this.tickTimerHologram(world);
        }

        if (time - this.lastItemSpawn > this.pool.getSpawnInterval()) {
            this.spawnItems(world, game);
            this.lastItemSpawn = time;
        }
    }

    private void tickTimerHologram(ServerWorld world) {
        long time = world.getTime();

        if (time % 20 == 0) {
            var hologram = this.timerHologram;
            if (hologram != null) {
                hologram.setText(0, this.getTimerText(time));
            } else {
                Vec3d textPos = this.bounds.center().add(0.0, 1.0, 0.0);
                this.timerHologram = Holograms.create(world, textPos, this.getTimerText(time));
                this.timerHologram.show();
            }
        }
    }

    private Text getTimerText(long time) {
        long timeSinceSpawn = time - this.lastItemSpawn;

        long timeUntilSpawn = this.pool.getSpawnInterval() - timeSinceSpawn;
        timeUntilSpawn = Math.max(0, timeUntilSpawn);

        // TODO: duplication with scoreboard
        long seconds = (timeUntilSpawn / 20) % 60;
        long minutes = timeUntilSpawn / (20 * 60);

        Formatting numberFormatting = Formatting.WHITE;

        long secondsUntilSpawn = timeUntilSpawn / 20;
        if (secondsUntilSpawn < 5) {
            if ((secondsUntilSpawn & 1) == 0) {
                numberFormatting = Formatting.AQUA;
            }
        }
        return Text.translatable("text.bedwars.floating.spawn_cooldown", Text.literal(String.format("%02d:%02d", minutes, seconds)).formatted(numberFormatting)).formatted(Formatting.GOLD);
    }

    private void spawnItems(ServerWorld world, BwActive game) {
        Random random = world.random;
        ItemStack stack = this.pool.sample();

        Box box = this.bounds.asBox();

        int itemCount = 0;
        for (ItemEntity entity : world.getEntitiesByType(EntityType.ITEM, box.expand(1.0), entity -> true)) {
            itemCount += entity.getStack().getCount();
        }

        if (itemCount >= this.maxItems) {
            return;
        }

        Box spawnBox = box.expand(-0.5, 0.0, -0.5);
        double x = spawnBox.minX + (spawnBox.maxX - spawnBox.minX) * random.nextDouble();
        double y = spawnBox.minY + 0.5;
        double z = spawnBox.minZ + (spawnBox.maxZ - spawnBox.minZ) * random.nextDouble();

        ItemEntity itemEntity = new ItemEntity(world, x, y, z, stack);
        itemEntity.setVelocity(Vec3d.ZERO);

        if (this.allowDuplication) {
            if (this.giveItems(world, game, itemEntity)) {
                return;
            }
        }

        world.spawnEntity(itemEntity);
    }

    private boolean giveItems(ServerWorld world, BwActive game, ItemEntity entity) {
        List<ServerPlayerEntity> players = world.getEntitiesByClass(ServerPlayerEntity.class, this.bounds.asBox(), game::isParticipant);
        for (ServerPlayerEntity player : players) {
            // Don't gen split to spectator or creative players
            if (player.getAbilities().allowFlying) {
                continue;
            }

            ItemStack stack = entity.getStack();

            player.giveItemStack(stack.copy());
            player.networkHandler.sendPacket(entity.createSpawnPacket());
            player.networkHandler.sendPacket(new ItemPickupAnimationS2CPacket(entity.getId(), player.getId(), stack.getCount()));

            player.getInventory().markDirty();
        }

        return !players.isEmpty();
    }
}
