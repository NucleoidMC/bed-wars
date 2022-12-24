package xyz.nucleoid.bedwars.custom;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.bedwars.BedWars;

public final class BwItems {
    public static final Item BRIDGE_EGG = register("bridge_egg", new SimplePolymerItem(new Item.Settings(), Items.EGG));
    public static final Item CHORUS_FRUIT = register("chorus_fruit", new BwChorusFruitItem(
            new Item.Settings().food(FoodComponents.CHORUS_FRUIT)
    ));
    public static final Item MOVING_CLOUD = register("moving_cloud", new SimplePolymerItem(new Item.Settings(), Items.COBWEB));

    private static <T extends Item> T register(String identifier, T item) {
        return Registry.register(Registries.ITEM, new Identifier(BedWars.ID, identifier), item);
    }
}
