package net.gegy1000.bedwars.custom;

import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.game.BedWars;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.item.CustomItem;
import net.gegy1000.gl.util.ColoredBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Random;

public final class BwCustomItems {
    public static final CustomItem BRIDGE_EGG = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "bridge_egg"))
            .name(new LiteralText("Bridge Egg"))
            .onUse(BwCustomItems::throwBridgeEgg)
            .register();

    public static final CustomItem BW_CHORUS_FRUIT = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "bw_chorus_fruit"))
            .register();

    // TODO: a more proper way for custom items to store additional data
    public static final CustomItem TEAM_SELECTOR = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "team_selector"))
            .register();

    private static TypedActionResult<ItemStack> throwBridgeEgg(PlayerEntity player, World world, Hand hand) {
        if (!world.isClient) {
            BedWars game = GameManager.openFor(BedWars.TYPE);
            if (game != null) {
                ItemStack stack = player.getStackInHand(hand);

                Random random = player.world.random;
                world.playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS,
                        0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F)
                );

                // Get player wool color
                GameTeam team = game.state.getTeam(player.getUuid());
                if (team == null) {
                    return TypedActionResult.pass(stack);
                }

                BlockState state = ColoredBlocks.wool(team.getDye()).getDefaultState();

                // Spawn egg
                BridgeEggEntity eggEntity = new BridgeEggEntity(world, player, state);
                eggEntity.setItem(stack);
                eggEntity.setProperties(player, player.pitch, player.yaw, 0.0F, 1.5F, 1.0F);

                world.spawnEntity(eggEntity);

                if (!player.abilities.creativeMode) {
                    stack.decrement(1);
                }

                return TypedActionResult.consume(stack);
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }
}
