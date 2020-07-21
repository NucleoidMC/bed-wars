package net.gegy1000.gl;

import com.google.common.reflect.Reflection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.gegy1000.gl.command.CustomizeCommand;
import net.gegy1000.gl.command.GameCommand;
import net.gegy1000.gl.command.MapCommand;
import net.gegy1000.gl.entity.CustomEntity;
import net.gegy1000.gl.game.Game;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.game.config.GameConfigs;
import net.gegy1000.gl.game.event.AttackEntityListener;
import net.gegy1000.gl.game.event.UseBlockListener;
import net.gegy1000.gl.game.event.UseItemListener;
import net.gegy1000.gl.game.map.StagingBoundRenderer;
import net.gegy1000.gl.game.map.provider.GlMapProviders;
import net.gegy1000.gl.item.CustomItem;
import net.gegy1000.gl.item.GlCustomItems;
import net.gegy1000.gl.world.VoidChunkGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: working name
public final class GameLib implements ModInitializer {
    public static final String ID = "gl";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        Reflection.initialize(GlCustomItems.class);

        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(ID, "void"), VoidChunkGenerator.CODEC);

        GlMapProviders.register();

        GameConfigs.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            CustomizeCommand.register(dispatcher);
            MapCommand.register(dispatcher);
            GameCommand.register(dispatcher);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient) {
                ItemStack stack = player.getStackInHand(hand);
                CustomItem custom = CustomItem.match(stack);
                if (custom != null) {
                    TypedActionResult<ItemStack> result = custom.onUse(player, world, hand);
                    if (result.getResult().isAccepted()) {
                        return result;
                    }
                }

                Game game = GameManager.openGame();
                if (game != null && game.containsPlayer(player)) {
                    UseItemListener invoker = game.invoker(UseItemListener.EVENT);
                    return invoker.onUseItem(game, (ServerPlayerEntity) player, hand);
                }
            }

            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient) {
                Game game = GameManager.openGame();
                if (game != null && game.containsPlayer(player)) {
                    UseBlockListener invoker = game.invoker(UseBlockListener.EVENT);
                    return invoker.onUseBlock(game, (ServerPlayerEntity) player, hand, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                Game game = GameManager.openGame();
                if (game != null && game.containsPlayer(player)) {
                    AttackEntityListener invoker = game.invoker(AttackEntityListener.EVENT);
                    return invoker.onAttackEntity(game, (ServerPlayerEntity) player, hand, entity, hitResult);
                }
            }

            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient) {
                CustomEntity customEntity = CustomEntity.match(entity);
                if (customEntity != null) {
                    return customEntity.interact(player, world, hand, entity, hitResult);
                }
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            Game game = GameManager.openGame();
            if (game != null) {
                game.tick();
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(StagingBoundRenderer::onTick);
    }
}
