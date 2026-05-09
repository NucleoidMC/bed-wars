package xyz.nucleoid.bedwars.game.active.shop;

import eu.pb4.sgui.api.elements.GuiElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.BwActive;
import xyz.nucleoid.bedwars.game.active.BwParticipant;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.shop.Cost;
import xyz.nucleoid.plasmid.api.shop.ShopEntry;
import xyz.nucleoid.plasmid.api.util.Guis;
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public final class BwTeamShop {

    private static final Component ACTIVE_TEXT = Component.translatable("text.bedwars.shop.active").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
    private static final Component MAX_LEVEL_TEXT = Component.translatable("text.bedwars.shop.max_level").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));

    public static void open(ServerPlayer player, BwActive game) {
        List<GuiElement> shop = new ArrayList<>();

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
                        game.broadcast.broadcastToTeam(participant.team, Component.translatable("text.bedwars.shop.upgrade." + baseTrapName + ".buy", p.getDisplayName().copy()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
                    })
            );

            String healPoolName = "heal_pool";
            ItemStack healPoolIcon = createIcon(Items.BEACON, healPoolName);
            shop.add(ShopEntry.ofIcon((p, e) -> createIconFor(p, healPoolIcon.copy(), e, teamState.healPool))
                    .withCost(Cost.ofDiamonds(teamScaledCost(game, team, 1.5D)))
                    .onBuyCheck((p, e) -> !teamState.healPool && e.getCost(p).takeItems(p))
                    .onBuy(p -> {
                        teamState.healPool = true;
                        game.broadcast.broadcastToTeam(participant.team, Component.translatable("text.bedwars.shop.upgrade." + healPoolName + ".buy", p.getDisplayName().copy()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
                    })
            );

            String hasteName = "haste";
            ItemStack hasteIcon = createIcon(Items.GOLDEN_PICKAXE, hasteName);
            shop.add(ShopEntry.ofIcon((p, e) -> createIconFor(p, hasteIcon.copy(), e, teamState.hasteEnabled))
                    .withCost(Cost.ofDiamonds(teamScaledCost(game, team, 1D)))
                    .onBuyCheck((p, e) -> !teamState.hasteEnabled && e.getCost(p).takeItems(p))
                    .onBuy(p -> {
                        teamState.hasteEnabled = true;
                        game.broadcast.broadcastToTeam(participant.team, Component.translatable("text.bedwars.shop.upgrade." + hasteName + ".buy", p.getDisplayName().copy()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
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
                                game.broadcast.broadcastToTeam(participant.team, Component.translatable("text.bedwars.shop.upgrade." + sharpnessName + ".buy", p.getDisplayName().copy(), Component.translatable("enchantment.level." + teamState.swordSharpness)).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
                            })
            );

            String lungeName = "lunge";
            shop.add(ShopEntry.ofIcon((p, e) -> createIconLvlFor(p, e, Items.DIAMOND_SPEAR, lungeName, Math.min(teamState.spearLunge + 1, BwActive.TeamState.MAX_LUNGE),
                                    teamState.spearLunge >= BwActive.TeamState.MAX_LUNGE)
                            )
                            .withCost((p, e) -> Cost.ofDiamonds(teamScaledCost(game, team, stagedUpgrade(4, teamState.spearLunge))))
                            .onBuyCheck((p, e) -> teamState.spearLunge < BwActive.TeamState.MAX_LUNGE && e.getCost(p).takeItems(p))
                            .onBuy(p -> {
                                teamState.spearLunge++;
                                game.teamLogic.applyEnchantments(participant.team);
                                game.broadcast.broadcastToTeam(participant.team, Component.translatable("text.bedwars.shop.upgrade." + lungeName + ".buy", p.getDisplayName().copy(), Component.translatable("enchantment.level." + teamState.spearLunge)).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
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
                                game.broadcast.broadcastToTeam(participant.team, Component.translatable("text.bedwars.shop.upgrade." + protectionName + ".buy", p.getDisplayName().copy(), Component.translatable("enchantment.level." + teamState.armorProtection)).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
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
                        game.broadcast.broadcastToTeam(participant.team, Component.translatable("text.bedwars.shop.upgrade.generator.buy", p.getDisplayName().copy(), teamSpawn.getLevel()).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA));
                    })
            );
        }

        var ui = Guis.createSelectorGui(player, Component.translatable("text.bedwars.shop.type.team"), false, shop);
        ui.open();
    }

    private static int stagedUpgrade(int first, int level) {
        return Mth.floor(Math.pow(2, level) * first);
    }

    private static int teamScaledCost(BwActive game, GameTeam team, double original) {
        return game.playersFor(team.key()).size() <= 2 ? (int) original : (int) (original * 2);
    }

    private static ItemStack createIcon(Item item, String id) {
        return ItemStackBuilder.of(Items.STONE)
                .set(DataComponents.ITEM_MODEL, item.components().get(DataComponents.ITEM_MODEL))
                .setName(Component.translatable("text.bedwars.shop.upgrade." + id))
                .addLore(Component.translatable("text.bedwars.shop.upgrade." + id + ".description.1").withStyle(ChatFormatting.GRAY))
                .addLore(Component.translatable("text.bedwars.shop.upgrade." + id + ".description.2").withStyle(ChatFormatting.GRAY))
                .build();
    }

    private static ItemStack createIconFor(ServerPlayer player, ItemStack icon, ShopEntry entry, boolean active) {
        boolean canBuy = entry.canBuy(player);

        var style = Style.EMPTY.withItalic(false).withColor(canBuy && !active ? ChatFormatting.BLUE : ChatFormatting.RED);
        var name = icon.getHoverName().copy().setStyle(style);

        if (active) {
            name.append(Component.literal(" (").append(ACTIVE_TEXT).append(")").setStyle(ACTIVE_TEXT.getStyle()));
        } else if (entry.getCost(player) != null) {
            var costText = entry.getCost(player).getDisplay();
            costText = Component.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
            name.append(costText);
        }

        icon.set(DataComponents.CUSTOM_NAME, name);

        return icon;
    }

    private static ItemStack createIconLvlFor(ServerPlayer player, ShopEntry entry, Item icon, String id, int level, boolean maxLvl) {
        boolean canBuy = entry.canBuy(player);

        var itemStackBuilder = ItemStackBuilder.of(Items.STONE)
                .set(DataComponents.ITEM_MODEL, icon.components().get(DataComponents.ITEM_MODEL))
                .addLore(Component.translatable("text.bedwars.shop.upgrade." + id + ".description.1", Component.translatable("enchantment.level." + level)).withStyle(ChatFormatting.GRAY))
                .addLore(Component.translatable("text.bedwars.shop.upgrade." + id + ".description.2", Component.translatable("enchantment.level." + level)).withStyle(ChatFormatting.GRAY))
                .setCount(level);


        var style = Style.EMPTY.withItalic(false).withColor(canBuy && !maxLvl ? ChatFormatting.BLUE : ChatFormatting.RED);
        var name = Component.translatable("text.bedwars.shop.upgrade." + id, Component.translatable("enchantment.level." + level)).setStyle(style);

        if (maxLvl) {
            name.append(Component.literal(" (").append(MAX_LEVEL_TEXT).append(")").setStyle(MAX_LEVEL_TEXT.getStyle()));
        } else if (entry.getCost(player) != null) {
            var costText = entry.getCost(player).getDisplay();
            costText = Component.literal(" (").append(costText).append(")").setStyle(costText.getStyle());
            name.append(costText);
        }

        itemStackBuilder.setName(name);

        return itemStackBuilder.build();
    }
}
