package net.gegy1000.bedwars;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.gegy1000.bedwars.command.CustomizeCommand;
import net.gegy1000.bedwars.command.GameCommand;
import net.gegy1000.bedwars.command.MapCommand;
import net.gegy1000.bedwars.custom.CustomEntities;
import net.gegy1000.bedwars.custom.CustomEntity;
import net.gegy1000.bedwars.custom.CustomItem;
import net.gegy1000.bedwars.custom.CustomItems;
import net.gegy1000.bedwars.custom.CustomizableEntity;
import net.gegy1000.bedwars.event.SwingHandCallback;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.game.bw.TestProceduralMapProvider;
import net.gegy1000.bedwars.game.config.GameConfigs;
import net.gegy1000.bedwars.map.StagingBoundRenderer;
import net.gegy1000.bedwars.map.provider.MapProviders;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BedWarsMod implements ModInitializer {
    public static final String ID = "bedwars";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        Reflection.initialize(CustomItems.class, CustomEntities.class);

        BedWars.initialize();

        MapProviders.register();
        TestProceduralMapProvider.register();
        GameConfigs.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            CustomizeCommand.register(dispatcher);
            MapCommand.register(dispatcher);
            GameCommand.register(dispatcher);
        });

        SwingHandCallback.EVENT.register((player, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            CustomItem custom = CustomItem.match(stack);
            if (custom != null) {
                custom.onSwingHand(player, hand);
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            CustomItem custom = CustomItem.match(stack);
            if (custom != null) {
                return custom.onUse(player, world, hand);
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof CustomizableEntity) {
                CustomizableEntity customizable = (CustomizableEntity) entity;
                CustomEntity customEntity = customizable.getCustomEntity();
                if (customEntity != null) {
                    return customEntity.interact(player, world, hand, entity, hitResult);
                }
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.START_SERVER_TICK.register(StagingBoundRenderer::onTick);
    }
}
