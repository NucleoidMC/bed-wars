package net.gegy1000.bedwars.item;

import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.api.RegionConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CustomItems {
    public static final CustomItem ADD_REGION = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "add_region"))
            .name(new LiteralText("Add Region"))
            .onUse(CustomItems::addRegion)
            .register();

    private static TypedActionResult<ItemStack> addRegion(PlayerEntity player, World world, Hand hand) {
        if (player instanceof RegionConstructor) {
            RegionConstructor constructor = (RegionConstructor) player;

            HitResult result = player.rayTrace(64.0, 1.0F, true);
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = new BlockPos(result.getPos());
                if (constructor.isTracing()) {
                    constructor.finishTracing(pos);
                    player.sendMessage(new LiteralText("Use /map commit region <identifier> to add this region"));
                } else {
                    constructor.startTracing(pos);
                }
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }
}
