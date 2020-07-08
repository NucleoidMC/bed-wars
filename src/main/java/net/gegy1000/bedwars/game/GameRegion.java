package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.BlockBounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

public final class GameRegion {
    private final Identifier marker;
    private final BlockBounds bounds;

    public GameRegion(Identifier marker, BlockBounds bounds) {
        this.marker = marker;
        this.bounds = bounds;
    }

    public Identifier getMarker() {
        return this.marker;
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }

    public CompoundTag serialize(CompoundTag tag) {
        tag.putString("marker", this.marker.toString());
        this.bounds.serialize(tag);
        return tag;
    }

    public static GameRegion deserialize(CompoundTag tag) {
        Identifier marker = new Identifier(tag.getString("marker"));
        return new GameRegion(marker, BlockBounds.deserialize(tag));
    }
}
