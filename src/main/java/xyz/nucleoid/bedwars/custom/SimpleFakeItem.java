package xyz.nucleoid.bedwars.custom;

import net.minecraft.item.Item;
import xyz.nucleoid.plasmid.fake.FakeItem;

public final class SimpleFakeItem extends Item implements FakeItem {
    private final Item proxy;

    public SimpleFakeItem(Item proxy) {
        super(new Item.Settings());
        this.proxy = proxy;
    }

    @Override
    public Item asProxy() {
        return this.proxy;
    }
}
