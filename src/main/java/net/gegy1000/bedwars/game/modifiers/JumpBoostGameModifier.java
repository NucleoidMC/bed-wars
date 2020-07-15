package net.gegy1000.bedwars.game.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.game.BedWars;
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
    public void init(BedWars game) {
    }

    @Override
    public void tick(BedWars game) {
        if (game.world.getTime() % 20 == 0) {
            game.state.players().forEach(this::addEffect);
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
