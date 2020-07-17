package net.gegy1000.bedwars.game.upgrade;

import net.gegy1000.bedwars.game.BwActive;
import net.gegy1000.bedwars.game.BwParticipant;
import net.gegy1000.gl.shop.Cost;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class WeaponUpgrade implements Upgrade {
    public final ItemStack stack;
    public final Cost cost;

    public WeaponUpgrade(ItemStack stack, Cost cost) {
        this.stack = stack;
        this.cost = cost;
    }

    public WeaponUpgrade(ItemConvertible item, Cost cost) {
        this.stack = new ItemStack(item);
        this.cost = cost;
    }

    @Override
    public void applyTo(BwActive game, ServerPlayerEntity player, BwParticipant participant) {
        player.inventory.offerOrDrop(player.world, game.createTool(this.stack.copy()));
    }

    @Override
    public void removeFrom(BwActive game, ServerPlayerEntity player) {
        for (int slot = 0; slot < player.inventory.size(); slot++) {
            ItemStack stack = player.inventory.getStack(slot);
            if (stack.getItem() == this.stack.getItem()) {
                player.inventory.removeStack(slot);
                break;
            }
        }
    }

    @Override
    public Item getIcon() {
        return this.stack.getItem();
    }

    @Override
    public Cost getCost() {
        return this.cost;
    }
}
