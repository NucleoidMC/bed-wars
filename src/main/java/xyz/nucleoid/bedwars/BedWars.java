package xyz.nucleoid.bedwars;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.bedwars.custom.BwItems;
import xyz.nucleoid.bedwars.game.config.BwConfig;
import xyz.nucleoid.bedwars.game.BwWaiting;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameModifiers;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.bedwars.game.generator.theme.MapThemes;
import xyz.nucleoid.plasmid.api.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;

public final class BedWars implements ModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<BwConfig> TYPE = GameType.register(
            Identifier.of(BedWars.ID, "bed_wars"),
            BwConfig.CODEC,
            BwWaiting::open
    );

    public static final GameRuleType BLAST_PROOF_GLASS_RULE = GameRuleType.create();
    public static final GameRuleType LEAVES_DROP_GOLDEN_APPLES = GameRuleType.create();
    public static final GameRuleType FAST_TREE_GROWTH = GameRuleType.create();

    @Override
    public void onInitialize() {
        Reflection.initialize(BwItems.class);

        BwGameTriggers.register();
        BwGameModifiers.register();
        MapThemes.register();
    }
}
