package net.gegy1000.gl.game.modifier;

import com.mojang.serialization.Codec;
import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.gl.registry.TinyRegistry;

import java.util.function.Function;

/**
 * A modifier for the current game that can have a variety of effects.
 */
public interface GameModifier {
	TinyRegistry<Codec<? extends GameModifier>> REGISTRY = TinyRegistry.newStable();
	Codec<GameModifier> CODEC = REGISTRY.dispatchStable(GameModifier::getCodec, Function.identity());

	/**
	 * @return The type of trigger used to start this modifier
	 */
	GameTrigger getTrigger();

	// TODO: make non bedwars-specific
	/**
	 * Called when starting a modifier
	 */
	void init(BwActive game);

	/**
	 * Called every tick for tickable modifiers
	 */
	default void tick(BwActive game) {
	}

	Codec<? extends GameModifier> getCodec();
}
