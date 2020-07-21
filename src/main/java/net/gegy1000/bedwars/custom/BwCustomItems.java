package net.gegy1000.bedwars.custom;

import net.gegy1000.bedwars.BedWars;
import net.gegy1000.gl.item.CustomItem;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public final class BwCustomItems {
    public static final CustomItem BRIDGE_EGG = CustomItem.builder()
            .id(new Identifier(BedWars.ID, "bridge_egg"))
            .name(new LiteralText("Bridge Egg"))
            .register();

    public static final CustomItem BW_CHORUS_FRUIT = CustomItem.builder()
            .id(new Identifier(BedWars.ID, "bw_chorus_fruit"))
            .register();

    // TODO: a more proper way for custom items to store additional data
    public static final CustomItem TEAM_SELECTOR = CustomItem.builder()
            .id(new Identifier(BedWars.ID, "team_selector"))
            .register();
}
