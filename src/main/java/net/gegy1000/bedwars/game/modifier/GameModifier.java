package net.gegy1000.bedwars.game.modifier;

import net.gegy1000.bedwars.game.bw.BedWars;

/**
 * A modifier for the current game that can have a variety of effects.
 */
public interface GameModifier {

	/**
	 * The types of triggers that can start a modifier
	 */
	enum Trigger {
		GAME_START(true),
		BED_BROKEN(false),
		PLAYER_DEATH(false),
		FINAL_DEATH(false);

		private final boolean tickable;

		Trigger(boolean tickable) {
			this.tickable = tickable;
		}

		public boolean isTickable() {
			return tickable;
		}
	}

	/**
	 * @return The type of trigger used to start this modifier
	 */
	Trigger getTrigger();

	/**
	 * Called when starting a modifier
	 */
	void init(BedWars game);

	/**
	 * Called every tick for tickable modifiers
	 */
	void tick(BedWars game);
}
