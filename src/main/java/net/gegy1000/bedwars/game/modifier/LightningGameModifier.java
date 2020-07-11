package net.gegy1000.bedwars.game.modifier;

import net.gegy1000.bedwars.game.bw.BedWars;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.Vec3d;

public class LightningGameModifier implements GameModifier {
	@Override
	public Trigger getTrigger() {
		return Trigger.PLAYER_DEATH;
	}

	@Override
	public void init(BedWars game) {
		game.state.participants().forEach(p -> {
			LightningEntity entity = EntityType.LIGHTNING_BOLT.create(game.world);
			entity.method_29495(Vec3d.ofBottomCenter(p.player().getBlockPos()));
			entity.method_29498(false);
			game.world.spawnEntity(entity);
		});
	}

	@Override
	public void tick(BedWars game) {

	}
}
