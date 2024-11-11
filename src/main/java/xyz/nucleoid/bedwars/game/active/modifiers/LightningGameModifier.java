package xyz.nucleoid.bedwars.game.active.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.SpawnReason;
import xyz.nucleoid.bedwars.game.active.BwActive;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class LightningGameModifier implements GameModifier {
    public static final MapCodec<LightningGameModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                GameTrigger.CODEC.fieldOf("trigger").forGetter(LightningGameModifier::getTrigger),
                Codec.BOOL.fieldOf("cosmetic").orElse(false).forGetter(modifier -> modifier.cosmetic)
        ).apply(instance, LightningGameModifier::new);
    });

    private final GameTrigger trigger;
    private final boolean cosmetic;

    public LightningGameModifier(GameTrigger trigger, boolean cosmetic) {
        this.trigger = trigger;
        this.cosmetic = cosmetic;
    }

    @Override
    public GameTrigger getTrigger() {
        return this.trigger;
    }

    @Override
    public void init(BwActive game) {
        game.players().forEach(player -> {
            ServerWorld world = game.world;

            LightningEntity entity = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.LOAD);
            if (entity == null) {
                return;
            }

            entity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(player.getBlockPos()));
            entity.setCosmetic(this.cosmetic);
            world.spawnEntity(entity);
        });
    }

    @Override
    public MapCodec<? extends GameModifier> getCodec() {
        return CODEC;
    }
}
