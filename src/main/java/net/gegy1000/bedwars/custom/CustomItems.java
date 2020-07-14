package net.gegy1000.bedwars.custom;

import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.GameManager;
import net.gegy1000.bedwars.game.GameTeam;
import net.gegy1000.bedwars.game.bw.BedWars;
import net.gegy1000.bedwars.map.trace.RegionTraceMode;
import net.gegy1000.bedwars.map.trace.RegionTracer;
import net.gegy1000.bedwars.util.BlockStateHolder;
import net.gegy1000.bedwars.util.ColoredBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public final class CustomItems {
    public static final CustomItem ADD_REGION = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "add_region"))
            .name(new LiteralText("Add Region"))
            .onUse(CustomItems::addRegion)
            .onSwingHand(CustomItems::changeRegionMode)
            .register();

    public static final CustomItem BRIDGE_EGG = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "bridge_egg"))
            .name(new LiteralText("Bridge Egg"))
            .onUse(CustomItems::throwBridgeEgg)
            .register();

    public static final CustomItem BW_CHORUS_FRUIT = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "bw_chorus_fruit"))
            .register();

    private static TypedActionResult<ItemStack> addRegion(PlayerEntity player, World world, Hand hand) {
        if (player instanceof RegionTracer) {
            RegionTracer constructor = (RegionTracer) player;

            RegionTraceMode traceMode = constructor.getMode();

            BlockPos pos = traceMode.tryTrace(player);
            if (pos != null) {
                if (constructor.isTracing()) {
                    constructor.finishTracing(pos);
                    player.sendMessage(new LiteralText("Use /map region commit <name> to add this region"), true);
                } else {
                    constructor.startTracing(pos);
                }
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    private static void changeRegionMode(PlayerEntity player, Hand hand) {
        if (player instanceof RegionTracer) {
            RegionTracer constructor = (RegionTracer) player;

            RegionTraceMode nextMode = constructor.getMode().next();
            constructor.setMode(nextMode);

            player.sendMessage(new LiteralText("Changed trace mode to: ").append(nextMode.getName()), true);
        }
    }

    private static TypedActionResult<ItemStack> throwBridgeEgg(PlayerEntity player, World world, Hand hand) {
        if (!world.isClient) {
            BedWars game = GameManager.openFor(BedWars.TYPE);
            if (game != null) {
                ItemStack stack = player.getStackInHand(hand);

                Random random = player.world.random;
                world.playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS,
                        0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F)
                );

                // Get player wool color
                GameTeam team = game.state.getTeam(player.getUuid());
                if (team == null) {
                    return TypedActionResult.pass(stack);
                }

                BlockState state = ColoredBlocks.wool(team.getDye()).getDefaultState();

                // Spawn egg
                EggEntity eggEntity = new EggEntity(world, player);
                eggEntity.setItem(stack);
                eggEntity.setProperties(player, player.pitch, player.yaw, 0.0F, 1.5F, 1.0F);

                CustomEntities.BRIDGE_EGG.applyTo(eggEntity);

                if (eggEntity instanceof BlockStateHolder) {
                    ((BlockStateHolder) eggEntity).setBlockState(state);
                }

                world.spawnEntity(eggEntity);

                if (!player.abilities.creativeMode) {
                    stack.decrement(1);
                }

                return TypedActionResult.consume(stack);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }
}
