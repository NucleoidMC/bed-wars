package net.gegy1000.bedwars;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.bedwars.game.BwConfig;
import net.gegy1000.bedwars.game.BwWaiting;
import net.gegy1000.bedwars.game.active.modifiers.BwGameModifiers;
import net.gegy1000.bedwars.game.active.modifiers.BwGameTriggers;
import net.gegy1000.plasmid.game.GameType;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BedWars implements ModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<BwConfig> TYPE = GameType.register(
            new Identifier(BedWars.ID, "bed_wars"),
            BwWaiting::open,
            BwConfig.CODEC
    );

    public static final GameRule BLAST_PROOF_GLASS_RULE = new GameRule();

    @Override
    public void onInitialize() {
        Reflection.initialize(BwCustomItems.class);

        BwGameTriggers.register();
        BwGameModifiers.register();
    }
}
