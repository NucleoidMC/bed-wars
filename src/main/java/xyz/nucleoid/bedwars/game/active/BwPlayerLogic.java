package xyz.nucleoid.bedwars.game.active;

import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.plasmid.util.ItemUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.function.Predicate;

public final class BwPlayerLogic {
    private final BwActive game;

    private long lastEnchantmentCheck;

    BwPlayerLogic(BwActive game) {
        this.game = game;
    }

    public void tick() {
        long time = this.game.world.getTime();

        this.game.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) return;

            if (participant.isRespawning() && time >= participant.respawnTime) {
                this.spawnPlayer(player, participant.respawningAt);
                participant.stopRespawning();
            }

            // Instakill players when below y0
            if (player.getY() <= 0) {

                // Don't kill spectators and creative players
                if (!player.abilities.allowFlying) {
                    player.kill();
                }
            }
        });

        if (time - this.lastEnchantmentCheck > 20) {
            this.game.participants().forEach(participant -> {
                ServerPlayerEntity player = participant.player();
                if (player != null) {
                    this.applyEnchantments(player, participant);
                }
            });

            this.lastEnchantmentCheck = time;
        }
    }

    public void spawnPlayer(ServerPlayerEntity player, BwMap.TeamSpawn spawn) {
        this.game.spawnLogic.respawnPlayer(player, GameMode.SURVIVAL);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 5, 2));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 20 * 5, 2));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 5, 2));

        if (!this.game.config.keepInventory) {
            player.inventory.clear();
        }

        BwParticipant participant = this.game.getParticipant(player);
        if (participant != null) {
            this.equipDefault(player, participant);
        }

        spawn.placePlayer(player, this.game.world);
    }

    // TODO: integrate enchantment system as "modifiers" to upgrades
    public void applyEnchantments(ServerPlayerEntity player, BwParticipant participant) {
        BwActive.TeamState teamState = this.game.getTeam(participant.team);
        if (teamState == null) {
            return;
        }

        this.applyEnchantments(player, stack -> stack.getItem().isIn(FabricToolTags.SWORDS), Enchantments.SHARPNESS, teamState.swordSharpness);
        this.applyEnchantments(player, stack -> stack.getItem() instanceof ArmorItem, Enchantments.PROTECTION, teamState.armorProtection);
    }

    private void applyEnchantments(ServerPlayerEntity player, Predicate<ItemStack> predicate, Enchantment enchantment, int level) {
        if (level <= 0) return;

        PlayerInventory inventory = player.inventory;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                int existingLevel = ItemUtil.getEnchantLevel(stack, enchantment);
                if (existingLevel != level) {
                    ItemUtil.removeEnchant(stack, enchantment);
                    stack.addEnchantment(enchantment, level);
                }
            }
        }
    }

    public void equipDefault(ServerPlayerEntity player, BwParticipant participant) {
        participant.upgrades.applyAll();
        this.applyEnchantments(player, participant);
    }

    public void respawnOnTimer(ServerPlayerEntity player, BwMap.TeamSpawn spawn) {
        this.game.spawnLogic.respawnPlayer(player, GameMode.SPECTATOR);
        this.game.spawnLogic.spawnAtCenter(player);

        BwParticipant participant = this.game.getParticipant(player);
        if (participant != null) {
            participant.startRespawning(spawn);
            player.sendMessage(new TranslatableText("text.bedwars.game.respawn", BwActive.RESPAWN_TIME_SECONDS).formatted(Formatting.BOLD), false);
        }
    }
}
