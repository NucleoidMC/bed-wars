package net.gegy1000.bedwars.game.upgrade;

import net.gegy1000.bedwars.game.BwActive;
import net.gegy1000.bedwars.game.BwParticipant;
import net.gegy1000.gl.shop.Cost;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ArmorUpgrade implements Upgrade {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static final ArmorUpgrade LEATHER = new ArmorUpgrade(Items.LEATHER_CHESTPLATE, Items.LEATHER_BOOTS, Cost.no());
    public static final ArmorUpgrade IRON = new ArmorUpgrade(Items.IRON_CHESTPLATE, Items.IRON_BOOTS, Cost.ofGold(12));
    public static final ArmorUpgrade DIAMOND = new ArmorUpgrade(Items.DIAMOND_CHESTPLATE, Items.DIAMOND_BOOTS, Cost.ofEmeralds(6));

    public final Item chest;
    public final Item feet;
    public final Cost cost;

    public ArmorUpgrade(Item chest, Item feet, Cost cost) {
        this.chest = chest;
        this.feet = feet;
        this.cost = cost;
    }

    @Override
    public void applyTo(BwActive game, ServerPlayerEntity player, BwParticipant participant) {
        ItemStack[] armorStacks = new ItemStack[] {
                new ItemStack(Items.LEATHER_HELMET),
                new ItemStack(this.chest),
                new ItemStack(Items.LEATHER_LEGGINGS),
                new ItemStack(this.feet)
        };

        for (int i = 0; i < armorStacks.length; i++) {
            int slot = ARMOR_SLOTS[i].getEntitySlotId();
            ItemStack stack = game.createArmor(participant.team.dye(armorStacks[i]));
            player.inventory.armor.set(slot, stack);
        }
    }

    @Override
    public void removeFrom(BwActive game, ServerPlayerEntity player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            player.inventory.armor.set(slot.getEntitySlotId(), ItemStack.EMPTY);
        }
    }

    @Override
    public Item getIcon() {
        return this.chest;
    }

    @Override
    public Cost getCost() {
        return this.cost;
    }
}
