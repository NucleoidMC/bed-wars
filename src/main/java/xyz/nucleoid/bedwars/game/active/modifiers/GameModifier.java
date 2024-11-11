package xyz.nucleoid.bedwars.game.active.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;

import java.util.function.Function;

/**
 * A modifier for the current game that can have a variety of effects.
 */
// TODO: make non bedwars-specific and merge with event system?
public interface GameModifier {
	TinyRegistry<MapCodec<? extends GameModifier>> REGISTRY = TinyRegistry.create();
	Codec<GameModifier> CODEC = REGISTRY.<GameModifier>dispatchMap(GameModifier::getCodec, Function.identity()).codec();

	/**
	 * @return The type of trigger used to start this modifier
	 */
	GameTrigger getTrigger();

	/**
	 * Called when starting a modifier
	 */
	void init(BwActive game);

	/**
	 * Called every tick for tickable modifiers
	 */
	default void tick(BwActive game) {
	}

	MapCodec<? extends GameModifier> getCodec();
}
