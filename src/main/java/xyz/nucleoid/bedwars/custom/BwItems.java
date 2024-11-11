package xyz.nucleoid.bedwars.custom;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import xyz.nucleoid.bedwars.BedWars;

import java.util.function.Function;

public final class BwItems {
    public static final Item BRIDGE_EGG = register("bridge_egg", settings -> new SimplePolymerItem(settings, Items.EGG));
    public static final Item CHORUS_FRUIT = register("chorus_fruit", settings -> new BwChorusFruitItem(
            settings.food(FoodComponents.CHORUS_FRUIT).modelId(Identifier.of("chorus_fruit")).translationKey("item.minecraft.chorus_fruit")
    ));
    public static final Item MOVING_CLOUD = register("moving_cloud", settings -> new SimplePolymerItem(settings, Items.COBWEB));

    private static <T extends Item> T register(String identifier, Function<Item.Settings, T> function) {
        var id = Identifier.of(BedWars.ID, identifier);
        var item = function.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id)));
        return Registry.register(Registries.ITEM, id, item);
    }
}
