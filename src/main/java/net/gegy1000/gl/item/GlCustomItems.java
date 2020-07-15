package net.gegy1000.gl.item;

import net.gegy1000.gl.GameLib;
import net.gegy1000.gl.game.map.trace.RegionTraceMode;
import net.gegy1000.gl.game.map.trace.RegionTracer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class GlCustomItems {
    public static final CustomItem ADD_REGION = CustomItem.builder()
            .id(new Identifier(GameLib.ID, "add_region"))
            .name(new LiteralText("Add Region"))
            .onUse(GlCustomItems::addRegion)
            .onSwingHand(GlCustomItems::changeRegionMode)
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
}
