package net.gegy1000.bedwars.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class CustomEntity {
    private static final Map<Identifier, CustomEntity> REGISTRY = new HashMap<>();

    private final Identifier id;
    private final Text name;

    private Interact interact;

    private CustomEntity(Identifier identifier, Text name) {
        this.id = identifier;
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Identifier getIdentifier() {
        return this.id;
    }

    public Text getName() {
        return this.name;
    }

    public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (this.interact == null) {
            return ActionResult.PASS;
        }
        return this.interact.interact(player, world, hand, entity, hitResult);
    }

    @Nullable
    public static CustomEntity get(Identifier identifier) {
        return REGISTRY.get(identifier);
    }

    public static class Builder {
        private Identifier id;
        private Text name;
        private Interact interact;

        private Builder() {
        }

        public Builder id(Identifier id) {
            this.id = id;
            return this;
        }

        public Builder name(Text name) {
            this.name = name;
            return this;
        }

        public Builder interact(Interact interact) {
            this.interact = interact;
            return this;
        }

        public CustomEntity register() {
            Preconditions.checkNotNull(this.id, "id not set");
            if (REGISTRY.containsKey(this.id)) {
                throw new IllegalArgumentException(this.id + " already registered");
            }

            CustomEntity item = new CustomEntity(this.id, this.name);
            item.interact = this.interact;

            REGISTRY.put(this.id, item);

            return item;
        }
    }

    public interface Interact {
        ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult);
    }
}
