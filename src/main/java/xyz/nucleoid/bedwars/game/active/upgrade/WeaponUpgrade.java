package xyz.nucleoid.bedwars.game.active.upgrade;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.shop.Cost;

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
        player.getInventory().offerOrDrop(game.createTool(this.stack.copy()));
    }

    @Override
    public void removeFrom(BwActive game, ServerPlayerEntity player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.getItem() == this.stack.getItem()) {
                inventory.removeStack(slot);
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
