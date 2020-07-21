package net.gegy1000.bedwars;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.gegy1000.bedwars.custom.BwCustomItems;
import net.gegy1000.bedwars.game.BwConfig;
import net.gegy1000.bedwars.game.BwMap;
import net.gegy1000.bedwars.game.BwWaiting;
import net.gegy1000.bedwars.game.active.modifiers.BwGameModifiers;
import net.gegy1000.bedwars.game.active.modifiers.BwGameTriggers;
import net.gegy1000.bedwars.game.generator.ProceduralMapProvider;
import net.gegy1000.gl.game.GameType;
import net.gegy1000.gl.game.rule.GameRule;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BedWars implements ModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<BwConfig> TYPE = GameType.register(
            new Identifier(BedWars.ID, "bed_wars"),
            (server, config) -> {
                return BwMap.create(server, config).thenApply(map -> {
                    return BwWaiting.build(map, config);
                });
            },
            BwConfig.CODEC
    );

    public static final GameRule BLAST_PROOF_GLASS_RULE = new GameRule();

    @Override
    public void onInitialize() {
        Reflection.initialize(BwCustomItems.class);

        ProceduralMapProvider.register();

        BwGameTriggers.register();
        BwGameModifiers.register();
    }

    public static void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.inventory.clear();
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.setGameMode(gameMode);
    }
}
