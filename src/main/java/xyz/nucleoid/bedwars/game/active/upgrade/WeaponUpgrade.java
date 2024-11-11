package xyz.nucleoid.bedwars.game.active.upgrade;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.api.shop.Cost;

import java.util.function.Function;

public final class WeaponUpgrade implements Upgrade {
    public final Function<MinecraftServer, ItemStack> stack;
    public final Cost cost;
    private final Item icon;

    public WeaponUpgrade(ItemStack stack, Cost cost) {
        this.icon = stack.getItem();
        this.stack = (s) -> stack;
        this.cost = cost;
    }

    public WeaponUpgrade(ItemConvertible item, Cost cost) {
        this.icon = item.asItem();
        this.stack = (s) -> new ItemStack(item);
        this.cost = cost;
    }

    public WeaponUpgrade(Item icon, Function<MinecraftServer, ItemStack> stack, Cost cost) {
        this.icon = icon;
        this.stack = stack;
        this.cost = cost;
    }

    @Override
    public void applyTo(BwActive game, ServerPlayerEntity player, BwParticipant participant) {
        player.getInventory().offerOrDrop(game.createTool(this.stack.apply(player.server)));
    }

    @Override
    public void removeFrom(BwActive game, ServerPlayerEntity player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.getItem() == this.icon) {
                inventory.removeStack(slot);
                break;
            }
        }
    }

    @Override
    public Item getIcon() {
        return this.icon;
    }

    @Override
    public Cost getCost() {
        return this.cost;
    }
}
