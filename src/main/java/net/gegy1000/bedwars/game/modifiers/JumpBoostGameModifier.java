package net.gegy1000.bedwars.game.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.game.BwActive;
import net.gegy1000.gl.game.modifier.GameModifier;
import net.gegy1000.gl.game.modifier.GameTrigger;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class JumpBoostGameModifier implements GameModifier {
    public static final Codec<JumpBoostGameModifier> CODEC = RecordCodecBuilder.create(instance -> {
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
        if (game.map.getWorld().getTime() % 20 == 0) {
            game.players().forEach(this::addEffect);
        }
    }

    @Override
    public Codec<? extends GameModifier> getCodec() {
        return CODEC;
    }

    private void addEffect(ServerPlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 20 * 2, 1, false, false));
    }
}
