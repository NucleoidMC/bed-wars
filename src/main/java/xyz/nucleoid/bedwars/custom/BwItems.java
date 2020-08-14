package xyz.nucleoid.bedwars.custom;

import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.bedwars.BedWars;

public final class BwItems {
    public static final Item BRIDGE_EGG = register("bridge_egg", new SimpleFakeItem(Items.EGG));
    public static final Item CHORUS_FRUIT = register("chorus_fruit", new BwChorusFruitItem(
            new Item.Settings().food(FoodComponents.CHORUS_FRUIT)
    ));

    private static <T extends Item> T register(String identifier, T item) {
        return Registry.register(Registry.ITEM, new Identifier(BedWars.ID, identifier), item);
    }
}
