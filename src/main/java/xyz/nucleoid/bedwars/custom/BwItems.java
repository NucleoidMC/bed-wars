package xyz.nucleoid.bedwars.custom;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.bedwars.BedWars;

import java.util.function.Function;

public final class BwItems {
    public static final Item BRIDGE_EGG = register("bridge_egg", settings -> new SimplePolymerItem(settings, Items.EGG));
    public static final Item CHORUS_FRUIT = register("chorus_fruit", settings -> new BwChorusFruitItem(
            settings.food(Foods.CHORUS_FRUIT)
    ));
    public static final Item MOVING_CLOUD = register("moving_cloud", settings -> new SimplePolymerItem(settings, Items.COBWEB));

    private static <T extends Item> T register(String identifier, Function<Item.Properties, T> function) {
        var id = Identifier.fromNamespaceAndPath(BedWars.ID, identifier);
        var item = function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }
}
