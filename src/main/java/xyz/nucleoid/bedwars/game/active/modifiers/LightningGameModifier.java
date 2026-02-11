package xyz.nucleoid.bedwars.game.active.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EntitySpawnReason;
import xyz.nucleoid.bedwars.game.active.BwActive;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class LightningGameModifier implements GameModifier {
    public static final MapCodec<LightningGameModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GameTrigger.CODEC.fieldOf("trigger").forGetter(LightningGameModifier::trigger),
            Codec.BOOL.fieldOf("cosmetic").orElse(false).forGetter(modifier -> modifier.cosmetic)
    ).apply(instance, LightningGameModifier::new));

    private final GameTrigger trigger;
    private final boolean cosmetic;

    public LightningGameModifier(GameTrigger trigger, boolean cosmetic) {
        this.trigger = trigger;
        this.cosmetic = cosmetic;
    }

    @Override
    public GameTrigger trigger() {
        return this.trigger;
    }

    @Override
    public void init(BwActive game) {
        game.players().forEach(player -> {
            ServerLevel world = game.world;

            LightningBolt entity = EntityType.LIGHTNING_BOLT.create(world, EntitySpawnReason.LOAD);
            if (entity == null) {
                return;
            }

            entity.snapTo(Vec3.atBottomCenterOf(player.blockPosition()));
            entity.setVisualOnly(this.cosmetic);
            world.addFreshEntity(entity);
        });
    }

    @Override
    public MapCodec<? extends GameModifier> getCodec() {
        return CODEC;
    }
}
