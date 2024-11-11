package xyz.nucleoid.bedwars.game.active.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.bedwars.game.active.BwActive;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class JumpBoostGameModifier implements GameModifier {
    public static final MapCodec<JumpBoostGameModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> {
        return instance.group(
                GameTrigger.CODEC.fieldOf("trigger").forGetter(JumpBoostGameModifier::getTrigger)
        ).apply(instance, JumpBoostGameModifier::new);
    });

    private final GameTrigger trigger;

    public JumpBoostGameModifier(GameTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public GameTrigger getTrigger() {
        return this.trigger;
    }

    @Override
    public void init(BwActive game) {
    }

    @Override
    public void tick(BwActive game) {
        if (game.world.getTime() % 20 == 0) {
            game.players().forEach(this::addEffect);
        }
    }

    @Override
    public MapCodec<? extends GameModifier> getCodec() {
        return CODEC;
    }

    private void addEffect(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 20 * 2, 1, false, false));
    }
}
