package xyz.nucleoid.bedwars.game.active.shop;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public final class BwTeamShop {
    public static ShopUi create(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new TranslatableText("text.bedwars.shop.type.team"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant == null) return;

            BwActive.TeamState teamState = game.getTeam(participant.team);
            if (teamState != null) {
                Text baseTrapName = new TranslatableText("text.bedwars.shop.upgrade.base_trap");
                shop.add(ShopEntry.ofIcon(Items.REDSTONE_TORCH)
                        .withName(baseTrapName)
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade.base_trap.description"))
                        .withCost(!teamState.trapSet ? Cost.ofDiamonds(1) : Cost.no())
                        .onBuy(p -> {
                            teamState.trapSet = true;
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade.base_trap.buy", p.getDisplayName().shallowCopy()).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                Text healPoolName = new TranslatableText("text.bedwars.shop.upgrade.heal_pool");
                shop.add(ShopEntry.ofIcon(Blocks.BEACON)
                        .withName(healPoolName)
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade.heal_pool.description"))
                        .withCost(!teamState.healPool ? Cost.ofDiamonds(3) : Cost.no())
                        .onBuy(p -> {
                            teamState.healPool = true;
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade.heal_pool.buy", p.getDisplayName().shallowCopy()).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                Text hasteName = new TranslatableText("text.bedwars.shop.upgrade.haste");
                shop.add(ShopEntry.ofIcon(Items.GOLDEN_PICKAXE)
                        .withName(hasteName)
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade.haste.description"))
                        .withCost(!teamState.hasteEnabled ? Cost.ofDiamonds(3) : Cost.no())
                        .onBuy(p -> {
                            teamState.hasteEnabled = true;
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade.haste.buy", p.getDisplayName().shallowCopy()).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                int sharpness = teamState.swordSharpness;
                int nextSharpness = Math.min(sharpness + 1, BwActive.TeamState.MAX_SHARPNESS);

                Text sharpnessName = new TranslatableText("text.bedwars.shop.upgrade.sharpness", new TranslatableText("enchantment.level." + nextSharpness));
                shop.add(ShopEntry.ofIcon(Items.DIAMOND_SWORD)
                        .withName(sharpnessName)
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade.sharpness.description", new TranslatableText("enchantment.level." + nextSharpness)))
                        .withCost(sharpness != nextSharpness ? Cost.ofDiamonds(stagedUpgrade(4, sharpness)) : Cost.no())
                        .onBuy(p -> {
                            teamState.swordSharpness = Math.max(nextSharpness, teamState.swordSharpness);
                            game.teamLogic.applyEnchantments(participant.team);
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade.sharpness.buy", p.getDisplayName().shallowCopy(), new TranslatableText("enchantment.level." + teamState.swordSharpness)).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                int protection = teamState.armorProtection;
                int nextProtection = Math.min(protection + 1, BwActive.TeamState.MAX_PROTECTION);

                Text protectionName = new TranslatableText("text.bedwars.shop.upgrade.protection", new TranslatableText("enchantment.level." + nextProtection));
                shop.add(ShopEntry.ofIcon(Items.DIAMOND_CHESTPLATE)
                        .withName(protectionName)
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade.description", new TranslatableText("enchantment.level." + nextProtection)))
                        .withCost(protection != nextProtection ? Cost.ofDiamonds(stagedUpgrade(4, protection)) : Cost.no())
                        .onBuy(p -> {
                            teamState.armorProtection = Math.max(nextProtection, teamState.armorProtection);
                            game.teamLogic.applyEnchantments(participant.team);
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade.buy", p.getDisplayName().shallowCopy(), new TranslatableText("enchantment.level." + teamState.armorProtection)).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );
            }

            BwMap.TeamSpawn teamSpawn = game.map.getTeamSpawn(participant.team);
            if (teamSpawn != null) {
                int level = teamSpawn.getLevel();
                int nextLevel = Math.min(level + 1, BwMap.TeamSpawn.MAX_LEVEL);
                Cost generatorCost = level != nextLevel ? Cost.ofDiamonds(stagedUpgrade(2, level)) : Cost.no();

                Text generatorName = new TranslatableText("text.bedwars.shop.upgrade.generator", new TranslatableText("enchantment.level." + teamSpawn.getLevel()));
                shop.add(ShopEntry.ofIcon(Blocks.FURNACE)
                        .withName(generatorName)
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade.generator.description"))
                        .withCost(generatorCost)
                        .onBuy(p -> {
                            teamSpawn.setLevel(nextLevel);
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade.generator.buy", p.getDisplayName().shallowCopy(), teamSpawn.getLevel()).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );
            }
        });
    }

    private static int stagedUpgrade(int first, int level) {
        return MathHelper.floor(Math.pow(2, level) * first);
    }
}
