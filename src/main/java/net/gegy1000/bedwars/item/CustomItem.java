package net.gegy1000.bedwars.item;

import com.google.common.base.Preconditions;
import net.gegy1000.bedwars.BedWarsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class CustomItem {
    private static final Map<Identifier, CustomItem> REGISTRY = new HashMap<>();

    private final Identifier id;
    private final Text name;

    private Use use;

    private CustomItem(Identifier identifier, Text name) {
        this.id = identifier;
        this.name = name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void apply(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(BedWarsMod.ID + ":custom_item", this.id.toString());

        if (this.name != null && !stack.hasCustomName()) {
            stack.setCustomName(this.name);
        }
    }

    public ItemStack create(Item item) {
        ItemStack stack = new ItemStack(item);
        this.apply(stack);
        return stack;
    }

    @Nullable
    public static CustomItem match(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;

        String customItem = tag.getString(BedWarsMod.ID + ":custom_item");
        if (customItem != null) {
            return REGISTRY.get(new Identifier(customItem));
        }

        return null;
    }

    @Nullable
    public static CustomItem get(Identifier identifier) {
        return REGISTRY.get(identifier);
    }

    public TypedActionResult<ItemStack> onUse(PlayerEntity player, World world, Hand hand) {
        if (this.use == null) {
            return TypedActionResult.pass(ItemStack.EMPTY);
        }
        return this.use.onUse(player, world, hand);
    }

    public static class Builder {
        private Identifier id;
        private Text name;
        private Use use;

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

        public Builder onUse(Use use) {
            this.use = use;
            return this;
        }

        public CustomItem register() {
            Preconditions.checkNotNull(this.id, "id not set");
            if (REGISTRY.containsKey(this.id)) {
                throw new IllegalArgumentException(this.id + " already registered");
            }

            CustomItem item = new CustomItem(this.id, this.name);
            item.use = this.use;

            REGISTRY.put(this.id, item);

            return item;
        }
    }

    public interface Use {
        TypedActionResult<ItemStack> onUse(PlayerEntity player, World world, Hand hand);
    }
}
