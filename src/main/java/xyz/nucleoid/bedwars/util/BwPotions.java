package xyz.nucleoid.bedwars.util;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.util.registry.Registry;

public class BwPotions {
    public static final Potion JUMP_BOOST_V = register("jump_boost_v", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 5)}));
    public static final Potion INVISIBILITY = register("bw_invisibility", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.INVISIBILITY, 600)}));
    public static final Potion SPEED = register("bw_speed", new Potion(new StatusEffectInstance[]{new StatusEffectInstance(StatusEffects.SPEED, 600, 2)}));

    private static Potion register(String name, Potion potion) {
        return (Potion)Registry.register(Registry.POTION, (String)name, potion);
     }
}
