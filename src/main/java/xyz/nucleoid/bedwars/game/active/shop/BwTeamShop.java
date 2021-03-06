package xyz.nucleoid.bedwars.game.active.shop;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;

public final class BwTeamShop {
    public static ShopUi create(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(new TranslatableText("text.bedwars.shop.type.team"), shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant == null) return;

            BwActive.TeamState teamState = game.getTeam(participant.team);
            if (teamState != null) {
                String baseTrapName = "base_trap";
                Cost baseTrapCost = !teamState.trapSet ? Cost.ofDiamonds(1) : Cost.no();
                shop.add(ShopEntry.ofIcon(Items.REDSTONE_TORCH)
                        .withName(new TranslatableText("text.bedwars.shop.upgrade." + baseTrapName))
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade." + baseTrapName + ".description"))
                        .withCost(baseTrapCost)
                        .onBuy(p -> {
                            teamState.trapSet = true;
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade."+ baseTrapName + ".buy", p.getDisplayName().shallowCopy()).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                String healPoolName = "heal_pool";
                Cost healPoolCost = !teamState.healPool ? Cost.ofDiamonds(3) : Cost.no();
                shop.add(ShopEntry.ofIcon(Blocks.BEACON)
                        .withName(new TranslatableText("text.bedwars.shop.upgrade." + healPoolName))
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade." + healPoolName + ".description"))
                        .withCost(healPoolCost)
                        .onBuy(p -> {
                            teamState.healPool = true;
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade."+ healPoolName + ".buy", p.getDisplayName().shallowCopy()).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                String hasteName = "haste";
                Cost hasteCost = !teamState.hasteEnabled ? Cost.ofDiamonds(3) : Cost.no();
                shop.add(ShopEntry.ofIcon(Items.GOLDEN_PICKAXE)
                        .withName(new TranslatableText("text.bedwars.shop.upgrade." + hasteName))
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade." + hasteName + ".description"))
                        .withCost(hasteCost)
                        .onBuy(p -> {
                            teamState.hasteEnabled = true;
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade."+ hasteName + ".buy", p.getDisplayName().shallowCopy()).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                int sharpness = teamState.swordSharpness;
                int nextSharpness = Math.min(sharpness + 1, BwActive.TeamState.MAX_SHARPNESS);

                String sharpnessName = "sharpness";
                Cost sharpnessCost = sharpness != nextSharpness ? Cost.ofDiamonds(stagedUpgrade(4, sharpness)) : Cost.no();
                shop.add(ShopEntry.ofIcon(Items.DIAMOND_SWORD)
                        .withName(new TranslatableText("text.bedwars.shop.upgrade." + sharpnessName, new TranslatableText("enchantment.level." + nextSharpness)))
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade." + sharpnessName + ".description", new TranslatableText("enchantment.level." + nextSharpness)))
                        .withCost(sharpnessCost)
                        .onBuy(p -> {
                            teamState.swordSharpness = Math.max(nextSharpness, teamState.swordSharpness);
                            game.teamLogic.applyEnchantments(participant.team);
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade."+ sharpnessName + ".buy", p.getDisplayName().shallowCopy(), new TranslatableText("enchantment.level." + teamState.swordSharpness)).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );

                int protection = teamState.armorProtection;
                int nextProtection = Math.min(protection + 1, BwActive.TeamState.MAX_PROTECTION);

                String protectionName = "protection";
                Cost protectionCost = protection != nextProtection ? Cost.ofDiamonds(stagedUpgrade(4, protection)) : Cost.no();
                shop.add(ShopEntry.ofIcon(Items.DIAMOND_CHESTPLATE)
                        .withName(new TranslatableText("text.bedwars.shop.upgrade." + protectionName, new TranslatableText("enchantment.level." + nextProtection)))
                        .addLore(new TranslatableText("text.bedwars.shop.upgrade." + protectionName + ".description", new TranslatableText("enchantment.level." + nextProtection)))
                        .withCost(protectionCost)
                        .onBuy(p -> {
                            teamState.armorProtection = Math.max(nextProtection, teamState.armorProtection);
                            game.teamLogic.applyEnchantments(participant.team);
                            game.broadcast.broadcastToTeam(participant.team, new TranslatableText("text.bedwars.shop.upgrade."+ protectionName + ".buy", p.getDisplayName().shallowCopy(), new TranslatableText("enchantment.level." + teamState.armorProtection)).formatted(Formatting.BOLD, Formatting.AQUA));
                        })
                );
            }

            BwMap.TeamSpawn teamSpawn = game.map.getTeamSpawn(participant.team);
            if (teamSpawn != null) {
                int level = teamSpawn.getLevel();
                int nextLevel = Math.min(level + 1, BwMap.TeamSpawn.MAX_LEVEL);

                Text generatorName = new TranslatableText("text.bedwars.shop.upgrade.generator", new TranslatableText("enchantment.level." + teamSpawn.getLevel()));
                Cost generatorCost = level != nextLevel ? Cost.ofDiamonds(stagedUpgrade(2, level)) : Cost.no();
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
