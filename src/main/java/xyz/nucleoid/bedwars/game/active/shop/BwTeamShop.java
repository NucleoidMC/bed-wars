package xyz.nucleoid.bedwars.game.active.shop;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;
import xyz.nucleoid.plasmid.util.Guis;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public final class BwTeamShop {

    private static final Text ACTIVE_TEXT = Text.translatable("text.bedwars.shop.active").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));
    private static final Text MAX_LEVEL_TEXT = Text.translatable("text.bedwars.shop.max_level").setStyle(Style.EMPTY.withColor(Formatting.YELLOW));

    public static void open(ServerPlayerEntity player, BwActive game) {
        List<GuiElementInterface> shop = new ArrayList<>();

        BwParticipant participant = game.participantBy(player);
        if (participant == null) return;

        BwActive.TeamState teamState = game.teamState(participant.team.key());
        GameTeam team = participant.team;
        if (teamState != null) {
            String baseTrapName = "base_trap";
            ItemStack baseTrapIcon = createIcon(Items.REDSTONE_TORCH, baseTrapName);
            shop.add(ShopEntry.ofIcon((p, e) -> createIconFor(p, baseTrapIcon.copy(), e, teamState.trapSet))
                    .withCost(Cost.ofDiamonds(teamScaledCost(game, team, 1D)))
                    .onBuyCheck((p, e) -> !teamState.trapSet && e.getCost(p).takeItems(p))
                    .onBuy(p -> {
                        teamState.trapSet = true;
                        game.broadcast.broadcastToTeam(participant.team, Text.translatable("text.bedwars.shop.upgrade." + baseTrapName + ".buy", p.getDisplayName().copy()).formatted(Formatting.BOLD, Formatting.AQUA));
                    })
            );

            String healPoolName = "heal_pool";
            ItemStack healPoolIcon = createIcon(Items.BEACON, healPoolName);
            shop.add(ShopEntry.ofIcon((p, e) -> createIconFor(p, healPoolIcon.copy(), e, teamState.healPool))
                    .withCost(Cost.ofDiamonds(teamScaledCost(game, team, 1.5D)))
                    .onBuyCheck((p, e) -> !teamState.healPool && e.getCost(p).takeItems(p))
                    .onBuy(p -> {
                        teamState.healPool = true;
                        game.broadcast.broadcastToTeam(participant.team, Text.translatable("text.bedwars.shop.upgrade." + healPoolName + ".buy", p.getDisplayName().copy()).formatted(Formatting.BOLD, Formatting.AQUA));
                    })
            );

            String hasteName = "haste";
            ItemStack hasteIcon = createIcon(Items.GOLDEN_PICKAXE, hasteName);
            shop.add(ShopEntry.ofIcon((p, e) -> createIconFor(p, hasteIcon.copy(), e, teamState.hasteEnabled))
                    .withCost(Cost.ofDiamonds(teamScaledCost(game, team, 1D)))
                    .onBuyCheck((p, e) -> !teamState.hasteEnabled && e.getCost(p).takeItems(p))
                    .onBuy(p -> {
                        teamState.hasteEnabled = true;
                        game.broadcast.broadcastToTeam(participant.team, Text.translatable("text.bedwars.shop.upgrade." + hasteName + ".buy", p.getDisplayName().copy()).formatted(Formatting.BOLD, Formatting.AQUA));
                    })
            );

            String sharpnessName = "sharpness";
            shop.add(ShopEntry.ofIcon((p, e) -> createIconLvlFor(p, e, Items.DIAMOND_SWORD, sharpnessName, Math.min(teamState.swordSharpness + 1, BwActive.TeamState.MAX_SHARPNESS),
                    teamState.swordSharpness >= BwActive.TeamState.MAX_SHARPNESS)
                    )
                            .withCost((p, e) -> Cost.ofDiamonds(teamScaledCost(game, team, stagedUpgrade(4, teamState.swordSharpness))))
                            .onBuyCheck((p, e) -> teamState.swordSharpness < BwActive.TeamState.MAX_SHARPNESS && e.getCost(p).takeItems(p))
                            .onBuy(p -> {
                                teamState.swordSharpness++;
                                game.teamLogic.applyEnchantments(participant.team);
                                game.broadcast.broadcastToTeam(participant.team, Text.translatable("text.bedwars.shop.upgrade." + sharpnessName + ".buy", p.getDisplayName().copy(), Text.translatable("enchantment.level." + teamState.swordSharpness)).formatted(Formatting.BOLD, Formatting.AQUA));
                            })
            );

            String protectionName = "protection";
            shop.add(ShopEntry.ofIcon((p, e) -> createIconLvlFor(p, e, Items.DIAMOND_CHESTPLATE, protectionName, Math.min(teamState.armorProtection + 1, BwActive.TeamState.MAX_PROTECTION),
                    teamState.armorProtection >= BwActive.TeamState.MAX_PROTECTION)
                    )
                            .withCost((p, e) -> Cost.ofDiamonds(teamScaledCost(game, team, stagedUpgrade(2, teamState.armorProtection))))
                            .onBuyCheck((p, e) -> teamState.armorProtection < BwActive.TeamState.MAX_PROTECTION && e.getCost(p).takeItems(p))
                            .onBuy(p -> {
                                teamState.armorProtection++;
                                game.teamLogic.applyEnchantments(participant.team);
                                game.broadcast.broadcastToTeam(participant.team, Text.translatable("text.bedwars.shop.upgrade." + protectionName + ".buy", p.getDisplayName().copy(), Text.translatable("enchantment.level." + teamState.armorProtection)).formatted(Formatting.BOLD, Formatting.AQUA));
                            })
            );
        }

        BwMap.TeamSpawn teamSpawn = game.map.getTeamSpawn(participant.team.key());
        if (teamSpawn != null) {

            String generatorName = "generator";
            shop.add(ShopEntry.ofIcon((p, e) -> createIconLvlFor(p, e, Items.FURNACE, generatorName, 1, teamSpawn.getLevel() >= BwMap.TeamSpawn.MAX_LEVEL))
                    .withCost((p, e) -> Cost.ofDiamonds(teamScaledCost(game, team, stagedUpgrade(1, teamSpawn.getLevel()))))
                    .onBuyCheck((p, e) -> teamSpawn.getLevel() < BwMap.TeamSpawn.MAX_LEVEL && e.getCost(p).takeItems(p))
                    .onBuy(p -> {
                        teamSpawn.setLevel(teamSpawn.getLevel() + 1, game.map.pools);
                        game.broadcast.broadcastToTeam(participant.team, Text.translatable("text.bedwars.shop.upgrade.generator.buy", p.getDisplayName().copy(), teamSpawn.getLevel()).formatted(Formatting.BOLD, Formatting.AQUA));
                    })
            );
        }

        var ui = Guis.createSelectorGui(player, Text.translatable("text.bedwars.shop.type.team"), shop);
        ui.open();
    }

    private static int stagedUpgrade(int first, int level) {
        return MathHelper.floor(Math.pow(2, level) * first);
    }

    private static int teamScaledCost(BwActive game, GameTeam team, double original) {
        return game.playersFor(team.key()).size() <= 2 ? (int) original : (int) (original * 2);
    }

    private static ItemStack createIcon(Item item, String id) {
        return ItemStackBuilder.of(item)
                .setName(Text.translatable("text.bedwars.shop.upgrade." + id))
                .addLore(Text.translatable("text.bedwars.shop.upgrade." + id + ".description.1").formatted(Formatting.GRAY))
                .addLore(Text.translatable("text.bedwars.shop.upgrade." + id + ".description.2").formatted(Formatting.GRAY))
                .hideFlags()
                .build();
    }

    private static ItemStack createIconFor(ServerPlayerEntity player, ItemStack icon, ShopEntry entry, boolean active) {
        boolean canBuy = entry.canBuy(player);

        var style = Style.EMPTY.withItalic(false).withColor(canBuy && !active ? Formatting.BLUE : Formatting.RED);
        var name = icon.getName().copy().setStyle(style);

        if (active) {
            name.append(Text.literal(" (").append(ACTIVE_TEXT).append(")").setStyle(ACTIVE_TEXT.getStyle()));
        } else if (entry.getCost(player) != null) {
            var costText = entry.getCost(player).getDisplay();
            costText = Text.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
            name.append(costText);
        }

        icon.setCustomName(name);

        return icon;
    }

    private static ItemStack createIconLvlFor(ServerPlayerEntity player, ShopEntry entry, Item icon, String id, int level, boolean maxLvl) {
        boolean canBuy = entry.canBuy(player);

        var itemStackBuilder = ItemStackBuilder.of(icon)
                .addLore(Text.translatable("text.bedwars.shop.upgrade." + id + ".description.1", Text.translatable("enchantment.level." + level)).formatted(Formatting.GRAY))
                .addLore(Text.translatable("text.bedwars.shop.upgrade." + id + ".description.2", Text.translatable("enchantment.level." + level)).formatted(Formatting.GRAY))
                .hideFlags()
                .setCount(level);


        var style = Style.EMPTY.withItalic(false).withColor(canBuy && !maxLvl ? Formatting.BLUE : Formatting.RED);
        var name = Text.translatable("text.bedwars.shop.upgrade." + id, Text.translatable("enchantment.level." + level)).setStyle(style);

        if (maxLvl) {
            name.append(Text.literal(" (").append(MAX_LEVEL_TEXT).append(")").setStyle(MAX_LEVEL_TEXT.getStyle()));
        } else if (entry.getCost(player) != null) {
            var costText = entry.getCost(player).getDisplay();
            costText = Text.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
            name.append(costText);
        }

        itemStackBuilder.setName(name);

        return itemStackBuilder.build();
    }
}
