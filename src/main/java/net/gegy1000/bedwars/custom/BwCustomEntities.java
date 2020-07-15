package net.gegy1000.bedwars.custom;

import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.shop.BwItemShop;
import net.gegy1000.gl.shop.ShopUi;
import net.gegy1000.bedwars.game.shop.BwTeamShop;
import net.gegy1000.gl.entity.CustomEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public final class BwCustomEntities {
    public static final CustomEntity TEAM_SHOP = CustomEntity.builder()
            .id(new Identifier(BedWarsMod.ID, "team_shop"))
            .name(new LiteralText("Team Shop"))
            .interact((player, world, hand, entity, hitResult) -> {
                if (player instanceof ServerPlayerEntity) {
                    ShopUi shop = BwTeamShop.create((ServerPlayerEntity) player);
                    player.openHandledScreen(shop);
                }
                return ActionResult.SUCCESS;
            })
            .register();

    public static final CustomEntity ITEM_SHOP = CustomEntity.builder()
            .id(new Identifier(BedWarsMod.ID, "item_shop"))
            .name(new LiteralText("Item Shop"))
            .interact((player, world, hand, entity, hitResult) -> {
                if (player instanceof ServerPlayerEntity) {
                    ShopUi shop = BwItemShop.create((ServerPlayerEntity) player);
                    player.openHandledScreen(shop);
                }
                return ActionResult.SUCCESS;
            })
            .register();

    public static final CustomEntity BRIDGE_EGG = CustomEntity.builder()
            .id(new Identifier(BedWarsMod.ID, "bridge_egg"))
            .register();
}
