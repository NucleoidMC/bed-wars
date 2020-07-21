package net.gegy1000.gl.mixin.rule;

import net.gegy1000.gl.game.Game;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.game.rule.GameRule;
import net.gegy1000.gl.game.rule.RuleResult;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CraftingResultInventory.class)
public abstract class CraftingResultInventoryMixin implements RecipeUnlocker {
    @Override
    public boolean shouldCraftRecipe(World world, ServerPlayerEntity player, Recipe<?> recipe) {
        Game game = GameManager.openGame();
        if (game != null && game.containsPlayer(player)) {
            RuleResult result = game.testRule(GameRule.ALLOW_CRAFTING);
            if (result == RuleResult.DENY) {
                return false;
            }
        }

        // [VanillaCopy]
        if (recipe.isIgnoredInRecipeBook() || !world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING) || player.getRecipeBook().contains(recipe)) {
            this.setLastRecipe(recipe);
            return true;
        } else {
            return false;
        }
    }
}
