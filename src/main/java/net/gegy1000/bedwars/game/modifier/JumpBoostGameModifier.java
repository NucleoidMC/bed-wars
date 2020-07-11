package net.gegy1000.bedwars.game.modifier;

import net.gegy1000.bedwars.game.bw.BedWars;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

public class JumpBoostGameModifier implements GameModifier {
	@Override
	public Trigger getTrigger() {
		return Trigger.GAME_START;
	}

	@Override
	public void init(BedWars game) {

	}

	@Override
	public void tick(BedWars game) {
		if (game.world.getTime() % 20 == 0) {
			game.state.participants().forEach(p -> addEffect(p.player()));
		}
	}

	private void addEffect(ServerPlayerEntity player) {
		if (player != null) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 20 * 2, 1, false, false));
		}
	}
}
