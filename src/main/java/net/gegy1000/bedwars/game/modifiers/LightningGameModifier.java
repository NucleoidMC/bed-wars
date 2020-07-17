package net.gegy1000.bedwars.game.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.game.BwActive;
import net.gegy1000.gl.game.modifier.GameModifier;
import net.gegy1000.gl.game.modifier.GameTrigger;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class LightningGameModifier implements GameModifier {
    public static final Codec<LightningGameModifier> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                GameTrigger.CODEC.fieldOf("trigger").forGetter(LightningGameModifier::getTrigger),
                Codec.BOOL.fieldOf("cosmetic").withDefault(false).forGetter(modifier -> modifier.cosmetic)
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
            ServerWorld world = game.map.getWorld();

            LightningEntity entity = EntityType.LIGHTNING_BOLT.create(world);
            if (entity == null) {
                return;
            }

            entity.method_29495(Vec3d.ofBottomCenter(player.getBlockPos()));
            entity.method_29498(this.cosmetic);
            world.spawnEntity(entity);
        });
    }

    @Override
    public Codec<? extends GameModifier> getCodec() {
        return CODEC;
    }
}
