package xyz.nucleoid.bedwars.game.active;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.bedwars.game.BwMap;

import java.util.function.Predicate;

public final class BwPlayerLogic {
    private final BwActive game;

    private long lastEnchantmentCheck;

    BwPlayerLogic(BwActive game) {
        this.game = game;
    }

    public void tick() {
        long time = this.game.world.getGameTime();

        this.game.participants().forEach(participant -> {
            ServerPlayer player = participant.player();
            if (player == null) return;

            if (participant.isRespawning() && time >= participant.respawnTime) {
                this.spawnPlayer(player, participant.respawningAt);
                participant.stopRespawning();
            }

            // Instakill players when below y0
            if (player.getY() <= 0) {

                // Don't kill spectators and creative players
                if (!player.getAbilities().mayfly) {
                    player.kill(player.level());
                }
            }
        });

        if (time - this.lastEnchantmentCheck > 20) {
            this.game.participants().forEach(participant -> {
                ServerPlayer player = participant.player();
                if (player != null) {
                    this.applyEnchantments(player, participant);
                }
            });

            this.lastEnchantmentCheck = time;
        }
    }

    public void spawnPlayer(ServerPlayer player, BwMap.TeamSpawn spawn) {
        this.game.spawnLogic.respawnPlayer(player, GameType.SURVIVAL);

        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 5, 2));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 5, 2));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 2));

        if (!this.game.config.keepInventory()) {
            player.getInventory().clearContent();
        }

        BwParticipant participant = this.game.participantBy(player);
        if (participant != null) {
            this.equipDefault(player, participant);
        }

        spawn.placePlayer(player, this.game.world);
    }

    // TODO: integrate enchantment system as "modifiers" to upgrades
    public void applyEnchantments(ServerPlayer player, BwParticipant participant) {
        BwActive.TeamState teamState = this.game.teamState(participant.team.key());
        if (teamState == null) {
            return;
        }

        this.applyEnchantments(player, stack -> stack.is(ItemTags.SWORDS), Enchantments.SHARPNESS, teamState.swordSharpness);
        this.applyEnchantments(player, stack -> stack.is(ItemTags.SPEARS), Enchantments.LUNGE, teamState.spearLunge);
        this.applyEnchantments(player, stack -> stack.is(ItemTags.ARMOR_ENCHANTABLE), Enchantments.PROTECTION, teamState.armorProtection);
    }

    private void applyEnchantments(ServerPlayer player, Predicate<ItemStack> predicate, ResourceKey<Enchantment> enchantment, int level) {
        if (level <= 0) return;

        var ench = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                int existingLevel = stack.getEnchantments().getLevel(ench);
                if (existingLevel != level) {
                    stack.enchant(ench, level);
                }
            }
        }
    }

    public void equipDefault(ServerPlayer player, BwParticipant participant) {
        participant.upgrades.applyAll();
        this.applyEnchantments(player, participant);
    }

    public void startRespawning(ServerPlayer player, BwMap.TeamSpawn spawn) {
        BwParticipant participant = this.game.participantBy(player);
        if (participant != null) {
            participant.startRespawning(spawn);
            player.displayClientMessage(Component.translatable("text.bedwars.respawn_cooldown", BwActive.RESPAWN_TIME_SECONDS).withStyle(ChatFormatting.BOLD), false);
        }
    }
}
