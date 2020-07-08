package net.gegy1000.bedwars.mixin;

import net.gegy1000.bedwars.event.CraftCheckCallback;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CraftingResultInventory.class)
public abstract class MixinCraftingResultInventory implements RecipeUnlocker {
    @Override
    public boolean shouldCraftRecipe(World world, ServerPlayerEntity player, Recipe<?> recipe) {
        if (!CraftCheckCallback.EVENT.invoker().canCraft(world, player, recipe)) {
            return false;
        }

        // duplicate of default function in interface
        if (recipe.isIgnoredInRecipeBook() || !world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) || player.getRecipeBook().contains(recipe)) {
            this.setLastRecipe(recipe);
            return true;
        } else {
            return false;
        }
    }
}
