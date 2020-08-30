package xyz.nucleoid.bedwars.game.active.shop;

import fr.catcore.server.translations.api.LocalizableText;
import fr.catcore.server.translations.api.LocalizationTarget;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.shop.ShopUi;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

public final class BwTeamShop {
    public static ShopUi create(ServerPlayerEntity player, BwActive game) {
        return ShopUi.create(LocalizableText.asLocalizedFor(
                new TranslatableText("text.bedwars.game.gui.team_shop"), (LocalizationTarget) player),
                shop -> {
            BwParticipant participant = game.getParticipant(player);
            if (participant == null) return;

            // TODO: generic team upgrade system
            BwActive.TeamState teamState = game.getTeam(participant.team);
            if (teamState != null) {
                Cost trapCost = !teamState.trapSet ? Cost.ofDiamonds(1) : Cost.no();

                shop.add(ShopEntry.ofIcon(Items.REDSTONE_TORCH)
                        .withName(new LiteralText("Activate Base Trap"))
                        .addLore(new LiteralText("When an enemy player enters the base,"))
                        .addLore(new LiteralText("they will receive blindness and slowness"))
                        .withCost(trapCost)
                        .onBuy(p -> {
                            teamState.trapSet = true;
                            game.broadcast.broadcastTeamUpgrade(participant, new LiteralText("activated the base trap!"));
                        })
                );

                Cost healPoolCost = !teamState.healPool ? Cost.ofDiamonds(3) : Cost.no();
                shop.add(ShopEntry.ofIcon(Blocks.BEACON)
                        .withName(new LiteralText("Activate Heal Pool"))
                        .addLore(new LiteralText("Friendly players will receive"))
                        .addLore(new LiteralText("regeneration in their base"))
                        .withCost(healPoolCost)
                        .onBuy(p -> {
                            teamState.healPool = true;
                            game.broadcast.broadcastTeamUpgrade(participant, new LiteralText("activated a heal pool!"));
                        })
                );

                Cost hasteCost = !teamState.hasteEnabled ? Cost.ofDiamonds(3) : Cost.no();
                shop.add(ShopEntry.ofIcon(Items.GOLDEN_PICKAXE)
                        .withName(new LiteralText("Activate Haste"))
                        .addLore(new LiteralText("All team members will get"))
                        .addLore(new LiteralText("a permanent haste effect"))
                        .withCost(hasteCost)
                        .onBuy(p -> {
                            teamState.hasteEnabled = true;
                            game.broadcast.broadcastTeamUpgrade(participant, new LiteralText("activated haste!"));
                        })
                );

                int sharpness = teamState.swordSharpness;
                int nextSharpness = Math.min(sharpness + 1, BwActive.TeamState.MAX_SHARPNESS);

                Cost sharpnessCost = sharpness != nextSharpness ? Cost.ofDiamonds(stagedUpgrade(4, sharpness)) : Cost.no();
                shop.add(ShopEntry.ofIcon(Items.DIAMOND_SWORD)
                        .withName(new LiteralText("Sword Sharpness " + nextSharpness))
                        .addLore(new LiteralText("All team members will get"))
                        .addLore(new LiteralText("sharpness applied to their swords"))
                        .withCost(sharpnessCost)
                        .onBuy(p -> {
                            teamState.swordSharpness = Math.max(nextSharpness, teamState.swordSharpness);
                            game.teamLogic.applyEnchantments(participant.team);
                            game.broadcast.broadcastTeamUpgrade(participant, new LiteralText("added Sword Sharpness " + teamState.swordSharpness));
                        })
                );

                int protection = teamState.armorProtection;
                int nextProtection = Math.min(protection + 1, BwActive.TeamState.MAX_PROTECTION);

                Cost protectionCost = protection != nextProtection ? Cost.ofDiamonds(stagedUpgrade(4, protection)) : Cost.no();
                shop.add(ShopEntry.ofIcon(Items.DIAMOND_CHESTPLATE)
                        .withName(new LiteralText("Armor Protection " + nextProtection))
                        .addLore(new LiteralText("All team members will get"))
                        .addLore(new LiteralText("protection applied to their armor"))
                        .withCost(protectionCost)
                        .onBuy(p -> {
                            teamState.armorProtection = Math.max(nextProtection, teamState.armorProtection);
                            game.teamLogic.applyEnchantments(participant.team);
                            game.broadcast.broadcastTeamUpgrade(participant, new LiteralText("added Armor Protection " + teamState.armorProtection));
                        })
                );
            }

            BwMap.TeamSpawn teamSpawn = game.map.getTeamSpawn(participant.team);
            if (teamSpawn != null) {
                int level = teamSpawn.getLevel();
                int nextLevel = Math.min(level + 1, BwMap.TeamSpawn.MAX_LEVEL);
                Cost generatorCost = level != nextLevel ? Cost.ofDiamonds(stagedUpgrade(2, level)) : Cost.no();

                shop.add(ShopEntry.ofIcon(Blocks.FURNACE)
                        .withName(new LiteralText("Upgrade Generator"))
                        .addLore(new LiteralText("The generator in your team base"))
                        .addLore(new LiteralText("will spawn items faster"))
                        .withCost(generatorCost)
                        .onBuy(p -> {
                            teamSpawn.setLevel(nextLevel);
                            game.broadcast.broadcastTeamUpgrade(participant, new LiteralText("upgraded to Generator " + teamSpawn.getLevel()));
                        })
                );
            }
        });
    }

    private static int stagedUpgrade(int first, int level) {
        return MathHelper.floor(Math.pow(2, level) * first);
    }
}
