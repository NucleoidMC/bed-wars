package net.gegy1000.bedwars;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.gegy1000.bedwars.custom.BwCustomEntities;
import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.bedwars.game.BedWars;
import net.gegy1000.bedwars.game.generator.ProceduralMapProvider;
import net.gegy1000.bedwars.game.modifiers.BwGameModifiers;
import net.gegy1000.bedwars.game.modifiers.BwGameTriggers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BedWarsMod implements ModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        Reflection.initialize(BwCustomItems.class, BwCustomEntities.class);

        BedWars.initialize();

        ProceduralMapProvider.register();

        BwGameTriggers.register();
        BwGameModifiers.register();
    }
}
