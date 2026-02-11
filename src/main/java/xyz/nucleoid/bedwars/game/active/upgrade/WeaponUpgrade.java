package xyz.nucleoid.bedwars.game.active.upgrade;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.api.shop.Cost;

import java.util.function.Function;

public final class WeaponUpgrade implements Upgrade {
    public final Function<MinecraftServer, ItemStack> stack;
    public final Cost cost;
    private final Item icon;

    public WeaponUpgrade(ItemLike item, Cost cost) {
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
    public void applyTo(BwActive game, ServerPlayer player, BwParticipant participant) {
        player.getInventory().placeItemBackInInventory(game.createTool(this.stack.apply(player.level().getServer())));
    }

    @Override
    public void removeFrom(BwActive game, ServerPlayer player) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() == this.icon) {
                inventory.removeItemNoUpdate(slot);
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
