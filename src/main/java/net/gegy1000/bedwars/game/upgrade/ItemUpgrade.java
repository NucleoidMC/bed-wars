package net.gegy1000.bedwars.game.upgrade;

import net.gegy1000.bedwars.game.BedWars;
import net.gegy1000.bedwars.game.BwState;
import net.gegy1000.gl.shop.Cost;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ItemUpgrade implements Upgrade {
    public final ItemStack stack;
    public final Cost cost;

    public ItemUpgrade(ItemStack stack, Cost cost) {
        this.stack = stack;
        this.cost = cost;
    }

    public ItemUpgrade(ItemConvertible item, Cost cost) {
        this.stack = new ItemStack(item);
        this.cost = cost;
    }

    @Override
    public void applyTo(BedWars game, ServerPlayerEntity player, BwState.Participant participant) {
        player.inventory.offerOrDrop(player.world, this.stack.copy());
    }

    @Override
    public void removeFrom(BedWars game, ServerPlayerEntity player) {
        for (int slot = 0; slot < player.inventory.size(); slot++) {
            ItemStack stack = player.inventory.getStack(slot);
            if (ItemStack.areEqual(this.stack, stack)) {
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
