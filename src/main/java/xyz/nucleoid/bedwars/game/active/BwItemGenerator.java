package xyz.nucleoid.bedwars.game.active;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import xyz.nucleoid.map_templates.BlockBounds;

import java.util.List;

public final class BwItemGenerator {
    private final BlockBounds bounds;
    private ItemGeneratorPool pool;

    private long lastItemSpawn;

    private int maxItems = 4;
    private boolean allowDuplication;

    private boolean hasTimerText;
    private ElementHolder timerHologram;
    private TextDisplayElement timerTextElement;

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

    public void tick(ServerLevel world, BwActive game) {
        if (this.pool == null) return;

        long time = world.getGameTime();

        if (this.hasTimerText) {
            this.tickTimerHologram(world);
        }

        if (time - this.lastItemSpawn > this.pool.getSpawnInterval()) {
            this.spawnItems(world, game);
            this.lastItemSpawn = time;
        }
    }

    private void tickTimerHologram(ServerLevel world) {
        long time = world.getGameTime();

        if (time % 20 == 0) {
            var hologram = this.timerHologram;
            if (hologram != null) {
                this.timerTextElement.setText(this.getTimerText(time));
                hologram.tick();
            } else {
                this.timerTextElement = new TextDisplayElement(this.getTimerText(time));
                this.timerTextElement.setBillboardMode(BillboardConstraints.CENTER);
                this.timerTextElement.setViewRange(0.3f);

                Vec3 textPos = this.bounds.center().add(0.0, 1.0, 0.0);
                this.timerHologram = new ElementHolder();
                this.timerHologram.addElement(this.timerTextElement);
                ChunkAttachment.of(this.timerHologram, world, textPos);
            }
        }
    }

    private Component getTimerText(long time) {
        long timeSinceSpawn = time - this.lastItemSpawn;

        long timeUntilSpawn = this.pool.getSpawnInterval() - timeSinceSpawn;
        timeUntilSpawn = Math.max(0, timeUntilSpawn);

        // TODO: duplication with scoreboard
        long seconds = (timeUntilSpawn / 20) % 60;
        long minutes = timeUntilSpawn / (20 * 60);

        ChatFormatting numberFormatting = ChatFormatting.WHITE;

        long secondsUntilSpawn = timeUntilSpawn / 20;
        if (secondsUntilSpawn < 5) {
            if ((secondsUntilSpawn & 1) == 0) {
                numberFormatting = ChatFormatting.AQUA;
            }
        }
        return Component.translatable("text.bedwars.floating.spawn_cooldown", Component.literal(String.format("%02d:%02d", minutes, seconds)).withStyle(numberFormatting)).withStyle(ChatFormatting.GOLD);
    }

    private void spawnItems(ServerLevel world, BwActive game) {
        RandomSource random = world.random;
        ItemStack stack = this.pool.sample();

        AABB box = this.bounds.asBox();

        int itemCount = 0;
        for (ItemEntity entity : world.getEntities(EntityType.ITEM, box.inflate(1.0), entity -> true)) {
            itemCount += entity.getItem().getCount();
        }

        if (itemCount >= this.maxItems) {
            return;
        }

        AABB spawnBox = box.inflate(-0.5, 0.0, -0.5);
        double x = spawnBox.minX + (spawnBox.maxX - spawnBox.minX) * random.nextDouble();
        double y = spawnBox.minY + 0.5;
        double z = spawnBox.minZ + (spawnBox.maxZ - spawnBox.minZ) * random.nextDouble();

        ItemEntity itemEntity = new ItemEntity(world, x, y, z, stack);
        itemEntity.setDeltaMovement(Vec3.ZERO);

        if (this.allowDuplication) {
            if (this.giveItems(world, game, itemEntity)) {
                return;
            }
        }

        world.addFreshEntity(itemEntity);
    }

    private boolean giveItems(ServerLevel world, BwActive game, ItemEntity entity) {
        List<ServerPlayer> players = world.getEntitiesOfClass(ServerPlayer.class, this.bounds.asBox(), game::isParticipant);
        for (ServerPlayer player : players) {
            // Don't gen split to spectator or creative players
            if (player.getAbilities().mayfly) {
                continue;
            }

            ItemStack stack = entity.getItem();

            player.addItem(stack.copy());
            player.connection.send(
                        new ClientboundTakeItemEntityPacket(
                                entity.getId(),
                                player.getId(),
                                stack.getCount()
                        )
            );

            player.getInventory().setChanged();
        }

        return !players.isEmpty();
    }
}
