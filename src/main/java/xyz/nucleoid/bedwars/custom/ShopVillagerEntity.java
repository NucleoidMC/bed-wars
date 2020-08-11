package xyz.nucleoid.bedwars.custom;

import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.shop.BwItemShop;
import xyz.nucleoid.bedwars.game.active.shop.BwTeamShop;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public final class ShopVillagerEntity extends VillagerEntity {
    private final BwActive game;
    private final Type type;

    private ShopVillagerEntity(World world, BwActive game, Type type) {
        super(EntityType.VILLAGER, world);
        this.game = game;
        this.type = type;

        this.setCustomName(type.name);

        this.setAiDisabled(true);
        this.setInvulnerable(true);
        this.setCustomNameVisible(true);
    }

    public static ShopVillagerEntity item(World world, BwActive game) {
        return new ShopVillagerEntity(world, game, Type.ITEM);
    }

    public static ShopVillagerEntity team(World world, BwActive game) {
        return new ShopVillagerEntity(world, game, Type.TEAM);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.type == Type.ITEM) {
            player.openHandledScreen(BwItemShop.create((ServerPlayerEntity) player, this.game));
        } else if (this.type == Type.TEAM) {
            player.openHandledScreen(BwTeamShop.create((ServerPlayerEntity) player, this.game));
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    private enum Type {
        ITEM(new LiteralText("Item Shop")),
        TEAM(new LiteralText("Team Shop"));

        private final Text name;

        Type(Text name) {
            this.name = name;
        }
    }
}
