package net.gegy1000.bedwars.game.bw.gen;

import net.gegy1000.bedwars.map.GameMapBuilder;

/**
 * The generator for a feature on a map, such as islands and spawns.
 */
public interface MapGen {
	void addTo(GameMapBuilder builder);
	void addRegionsTo(GameMapBuilder builder);
}
