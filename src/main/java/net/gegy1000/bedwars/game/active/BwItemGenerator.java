package net.gegy1000.bedwars.game.active;

import net.gegy1000.plasmid.entity.FloatingText;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

public final class BwItemGenerator {
    private final BlockBounds bounds;
    private ItemGeneratorPool pool;

    private long lastItemSpawn;

    private int maxItems = 4;
    private boolean allowDuplication;

    private boolean hasTimerText;
    private FloatingText timerText;

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
            this.tickTimerText(world);
        }

        if (time - this.lastItemSpawn > this.pool.getSpawnInterval()) {
            this.spawnItems(world, game);
            this.lastItemSpawn = time;
        }
    }

    private void tickTimerText(ServerWorld world) {
        long time = world.getTime();

        if (this.timerText == null) {
            Vec3d textPos = this.bounds.getCenter();
            textPos = textPos.add(0.0, 2.0, 0.0);

            this.timerText = FloatingText.spawn(world, textPos, this.getTimerText(time));
        }

        if (this.timerText != null && time % 20 == 0) {
            this.timerText.setText(this.getTimerText(time));
        }
    }

    private Text getTimerText(long time) {
        long timeSinceSpawn = time - this.lastItemSpawn;

        long timeUntilSpawn = this.pool.getSpawnInterval() - timeSinceSpawn;
        timeUntilSpawn = Math.max(0, timeUntilSpawn);

        // TODO: duplication with scoreboard
        long seconds = (timeUntilSpawn / 20) % 60;
        long minutes = timeUntilSpawn / (20 * 60);

        Formatting titleFormatting = Formatting.GOLD;

        Formatting numberFormatting = Formatting.WHITE;

        long secondsUntilSpawn = timeUntilSpawn / 20;
        if (secondsUntilSpawn < 5) {
            if ((secondsUntilSpawn & 1) == 0) {
                numberFormatting = Formatting.AQUA;
            }
        }

        MutableText titleText = new LiteralText("Next spawn in: ");
        MutableText numberText = new LiteralText(String.format("%02d:%02d", minutes, seconds));

        return titleText.formatted(titleFormatting).append(numberText.formatted(numberFormatting));
    }

    private void spawnItems(ServerWorld world, BwActive game) {
        Random random = world.random;
        ItemStack stack = this.pool.sample(random).copy();

        Box box = this.bounds.toBox();

        int itemCount = 0;
        for (ItemEntity entity : world.getEntities(EntityType.ITEM, box.expand(1.0), entity -> true)) {
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
        List<ServerPlayerEntity> players = world.getEntities(ServerPlayerEntity.class, this.bounds.toBox(), game::isParticipant);
        for (ServerPlayerEntity player : players) {
            // Don't gen split to spectator or creative players
            if (player.abilities.allowFlying) {
                continue;
            }

            ItemStack stack = entity.getStack();

            player.giveItemStack(stack.copy());
            player.networkHandler.sendPacket(entity.createSpawnPacket());
            player.networkHandler.sendPacket(new ItemPickupAnimationS2CPacket(entity.getEntityId(), player.getEntityId(), stack.getCount()));

            player.inventory.markDirty();
        }

        return !players.isEmpty();
    }

    public void remove() {
        if (this.timerText != null) {
            this.timerText.remove();
        }
    }
}
