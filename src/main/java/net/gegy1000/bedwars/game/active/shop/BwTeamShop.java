package net.gegy1000.bedwars.game.active.shop;

import net.gegy1000.bedwars.game.BedWars;
import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.bedwars.game.BwMap;
import net.gegy1000.bedwars.game.active.BwParticipant;
import net.gegy1000.gl.game.GameManager;
import net.gegy1000.gl.shop.Cost;
import net.gegy1000.gl.shop.ShopUi;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

public final class BwTeamShop {
    public static ShopUi create(ServerPlayerEntity player) {
        return ShopUi.create(new LiteralText("Team Shop"), shop -> {
            BedWars bedWars = GameManager.openFor(BedWars.TYPE);
            if (bedWars == null || !(bedWars.phase instanceof BwActive)) return;

            BwActive active = (BwActive) bedWars.phase;

            BwParticipant participant = active.getParticipant(player);
            if (participant == null) return;

            // TODO: generic team upgrade system
            BwActive.TeamState teamState = active.getTeam(participant.team);
            if (teamState != null) {
                Cost trapCost = !teamState.trapSet ? Cost.ofDiamonds(1) : Cost.no();
                shop.add(Items.REDSTONE_TORCH, trapCost, new LiteralText("Activate Base Trap"), () -> {
                    teamState.trapSet = true;
                    active.broadcast.broadcastTeamUpgrade(participant, new LiteralText("activated the base trap!"));
                });

                Cost healPoolCost = !teamState.healPool ? Cost.ofDiamonds(3) : Cost.no();
                shop.add(Blocks.BEACON, healPoolCost, new LiteralText("Activate Heal Pool"), () -> {
                    teamState.healPool = true;
                    active.broadcast.broadcastTeamUpgrade(participant, new LiteralText("activated a heal pool!"));
                });

                Cost hasteCost = !teamState.hasteEnabled ? Cost.ofDiamonds(3) : Cost.no();
                shop.add(Items.GOLDEN_PICKAXE, hasteCost, new LiteralText("Haste"), () -> {
                    teamState.hasteEnabled = true;
                    active.broadcast.broadcastTeamUpgrade(participant, new LiteralText("activated haste!"));
                });

                int sharpness = teamState.swordSharpness;
                int nextSharpness = Math.min(sharpness + 1, BwActive.TeamState.MAX_SHARPNESS);

                Cost sharpnessCost = sharpness != nextSharpness ? Cost.ofDiamonds(stagedUpgrade(4, sharpness)) : Cost.no();
                shop.add(Items.DIAMOND_SWORD, sharpnessCost, new LiteralText("Sword Sharpness " + nextSharpness), () -> {
                    teamState.swordSharpness = Math.max(nextSharpness, teamState.swordSharpness);
                    active.teamLogic.applyEnchantments(participant.team);
                    active.broadcast.broadcastTeamUpgrade(participant, new LiteralText("added Sword Sharpness " + teamState.swordSharpness));
                });

                int protection = teamState.armorProtection;
                int nextProtection = Math.min(protection + 1, BwActive.TeamState.MAX_PROTECTION);

                Cost protectionCost = protection != nextProtection ? Cost.ofDiamonds(stagedUpgrade(4, protection)) : Cost.no();
                shop.add(Items.DIAMOND_CHESTPLATE, protectionCost, new LiteralText("Armor Protection " + nextProtection), () -> {
                    teamState.armorProtection = Math.max(nextProtection, teamState.armorProtection);
                    active.teamLogic.applyEnchantments(participant.team);
                    active.broadcast.broadcastTeamUpgrade(participant, new LiteralText("added Armor Protection " + teamState.armorProtection));
                });
            }

            BwMap.TeamSpawn teamSpawn = bedWars.map.getTeamSpawn(participant.team);
            if (teamSpawn != null) {
                int level = teamSpawn.getLevel();
                int nextLevel = Math.min(level + 1, BwMap.TeamSpawn.MAX_LEVEL);
                Cost cost = level != nextLevel ? Cost.ofDiamonds(stagedUpgrade(2, level)) : Cost.no();
                shop.add(Items.FURNACE, cost, new LiteralText("Upgrade Generator"), () -> {
                    teamSpawn.setLevel(nextLevel);
                    active.broadcast.broadcastTeamUpgrade(participant, new LiteralText("upgraded to Generator " + teamSpawn.getLevel()));
                });
            }
        });
    }

    private static int stagedUpgrade(int first, int level) {
        return MathHelper.floor(Math.pow(2, level) * first);
    }
}
