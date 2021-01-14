package xyz.nucleoid.bedwars;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.game.BwConfig;
import xyz.nucleoid.bedwars.game.BwWaiting;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameModifiers;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.bedwars.game.generator.theme.MapThemes;
import xyz.nucleoid.plasmid.game.GameType;
import xyz.nucleoid.plasmid.game.rule.GameRule;
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
    public static final GameRule LEAVES_DROP_GOLDEN_APPLES = new GameRule();
    public static final GameRule FAST_TREE_GROWTH = new GameRule();

    @Override
    public void onInitialize() {
        Reflection.initialize(BwItems.class);

        BwGameTriggers.register();
        BwGameModifiers.register();
        MapThemes.register();
    }
}
