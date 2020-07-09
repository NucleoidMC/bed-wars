package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.util.ItemUtil;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public final class GameTeam {
    public static final GameTeam WHITE = new GameTeam(new LiteralText("White"), "white", DyeColor.WHITE, Formatting.WHITE);
    public static final GameTeam ORANGE = new GameTeam(new LiteralText("Orange"), "orange", DyeColor.ORANGE, Formatting.GOLD);
    public static final GameTeam MAGENTA = new GameTeam(new LiteralText("Magenta"), "magenta", DyeColor.MAGENTA, Formatting.LIGHT_PURPLE);
    public static final GameTeam LIGHT_BLUE = new GameTeam(new LiteralText("Light Blue"), "light_blue", DyeColor.LIGHT_BLUE, Formatting.AQUA);
    public static final GameTeam YELLOW = new GameTeam(new LiteralText("Yellow"), "yellow", DyeColor.YELLOW, Formatting.YELLOW);
    public static final GameTeam LIME = new GameTeam(new LiteralText("Lime"), "lime", DyeColor.LIME, Formatting.GREEN);
    public static final GameTeam PINK = new GameTeam(new LiteralText("Pink"), "pink", DyeColor.PINK, Formatting.LIGHT_PURPLE);
    public static final GameTeam GRAY = new GameTeam(new LiteralText("Gray"), "gray", DyeColor.GRAY, Formatting.DARK_GRAY);
    public static final GameTeam LIGHT_GRAY = new GameTeam(new LiteralText("Light Gray"), "light_gray", DyeColor.LIGHT_GRAY, Formatting.GRAY);
    public static final GameTeam CYAN = new GameTeam(new LiteralText("Cyan"), "cyan", DyeColor.CYAN, Formatting.DARK_AQUA);
    public static final GameTeam PURPLE = new GameTeam(new LiteralText("Purple"), "purple", DyeColor.PURPLE, Formatting.DARK_PURPLE);
    public static final GameTeam BLUE = new GameTeam(new LiteralText("Blue"), "blue", DyeColor.BLUE, Formatting.BLUE);
    public static final GameTeam BROWN = new GameTeam(new LiteralText("Brown"), "brown", DyeColor.BROWN, Formatting.DARK_RED);
    public static final GameTeam GREEN = new GameTeam(new LiteralText("Green"), "green", DyeColor.GREEN, Formatting.DARK_GREEN);
    public static final GameTeam RED = new GameTeam(new LiteralText("Red"), "red", DyeColor.RED, Formatting.RED);
    public static final GameTeam BLACK = new GameTeam(new LiteralText("Black"), "black", DyeColor.BLACK, Formatting.BLACK);

    private final Text name;
    private final String key;
    private final DyeColor color;
    private final Formatting formatting;

    public GameTeam(Text name, String key, DyeColor color, Formatting formatting) {
        this.name = name;
        this.key = key;
        this.color = color;
        this.formatting = formatting;
    }

    public Text getName() {
        return this.name;
    }

    public String getKey() {
        return this.key;
    }

    public DyeColor getColor() {
        return this.color;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }

    public ItemStack createFirework(int flight, FireworkItem.Type type) {
        return ItemUtil.createFirework(this.getFireworkColor(), flight, type);
    }

    public ItemStack dye(ItemStack stack) {
        return ItemUtil.dye(stack, this.getDyeColor());
    }

    public int getDyeColor() {
        float[] components = this.color.getColorComponents();
        int red = MathHelper.floor(components[0] * 255.0F) & 0xFF;
        int green = MathHelper.floor(components[1] * 255.0F) & 0xFF;
        int blue = MathHelper.floor(components[2] * 255.0F) & 0xFF;
        return (red << 16) | (green << 8) | blue;
    }

    public int getFireworkColor() {
        return this.color.getFireworkColor();
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof GameTeam) {
            return ((GameTeam) obj).key.equals(this.key);
        }

        return false;
    }
}
