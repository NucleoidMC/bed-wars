package xyz.nucleoid.bedwars.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.shop.BwItemShop;
import xyz.nucleoid.bedwars.game.active.shop.BwTeamShop;

public final class ShopVillagerEntity extends Villager {
    private final BwActive game;
    private final Type type;

    private ShopVillagerEntity(Level world, BwActive game, Type type) {
        super(EntityType.VILLAGER, world);
        this.game = game;
        this.type = type;

        this.setCustomName(type.name);

        this.setNoAi(true);
        this.setInvulnerable(true);
        this.setCustomNameVisible(true);
    }

    public static ShopVillagerEntity item(Level world, BwActive game) {
        return new ShopVillagerEntity(world, game, Type.ITEM);
    }

    public static ShopVillagerEntity team(Level world, BwActive game) {
        return new ShopVillagerEntity(world, game, Type.TEAM);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.type == Type.ITEM) {
            BwItemShop.open((ServerPlayer) player, this.game);
        } else if (this.type == Type.TEAM) {
            BwTeamShop.open((ServerPlayer) player, this.game);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    private enum Type {
        ITEM(Component.translatable("text.bedwars.shop.type.item")),
        TEAM(Component.translatable("text.bedwars.shop.type.team"));

        private final Component name;

        Type(Component name) {
            this.name = name;
        }
    }
}
