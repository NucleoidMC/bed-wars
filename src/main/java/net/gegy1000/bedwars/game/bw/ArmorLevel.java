package net.gegy1000.bedwars.game.bw;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public enum ArmorLevel {
    LEATHER(0, Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
    IRON(1, Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
    DIAMOND(2, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS);

    public final int index;
    public final Item head;
    public final Item chest;
    public final Item legs;
    public final Item feet;

    ArmorLevel(int index, Item head, Item chest, Item legs, Item feet) {
        this.index = index;
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.feet = feet;
    }

    public boolean isUpgradeTo(ArmorLevel to) {
        return this.index < to.index;
    }
}
