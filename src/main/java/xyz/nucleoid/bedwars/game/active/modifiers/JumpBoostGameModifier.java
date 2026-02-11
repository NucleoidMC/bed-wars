package xyz.nucleoid.bedwars.game.active.modifiers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.bedwars.game.active.BwActive;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;

public record JumpBoostGameModifier(GameTrigger trigger) implements GameModifier {
    public static final MapCodec<JumpBoostGameModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GameTrigger.CODEC.fieldOf("trigger").forGetter(JumpBoostGameModifier::trigger)
    ).apply(instance, JumpBoostGameModifier::new));

    @Override
    public void init(BwActive game) {
    }

    @Override
    public void tick(BwActive game) {
        if (game.world.getGameTime() % 20 == 0) {
            game.players().forEach(this::addEffect);
        }
    }

    @Override
    public MapCodec<? extends GameModifier> getCodec() {
        return CODEC;
    }

    private void addEffect(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 2, 1, false, false));
    }
}
