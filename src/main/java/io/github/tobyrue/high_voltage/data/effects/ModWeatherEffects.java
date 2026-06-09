package io.github.tobyrue.high_voltage.data.effects;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModWeatherEffects {
    public static final DeferredRegister<WeatherProfile.WeatherEffectType<?>> EFFECTS =
            WeatherProfile.WeatherEffectType.WEATHER_EFFECT_TYPES;

    public static final RegistryObject<WeatherProfile.WeatherEffectType<CommandEffect>> COMMAND =
            EFFECTS.register("command", () -> new WeatherProfile.WeatherEffectType<>(CommandEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<RingBellEffect>> RING_BELL =
            EFFECTS.register("ring_bell", () -> new WeatherProfile.WeatherEffectType<>(RingBellEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<DamageArmorEffect>> DAMAGE_ARMOR =
            EFFECTS.register("damage_armor", () -> new WeatherProfile.WeatherEffectType<>(DamageArmorEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<SummonEntityEffect>> SUMMON_ENTITY =
            EFFECTS.register("summon_entity", () -> new WeatherProfile.WeatherEffectType<>(SummonEntityEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<DamageEffect>> DAMAGE =
            EFFECTS.register("damage", () -> new WeatherProfile.WeatherEffectType<>(DamageEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<HungerEffect>> HUNGER =
            EFFECTS.register("hunger", () -> new WeatherProfile.WeatherEffectType<>(HungerEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<FreezeEffect>> FREEZE =
            EFFECTS.register("freeze", () -> new WeatherProfile.WeatherEffectType<>(FreezeEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<LayerEffect>> LAYER =
            EFFECTS.register("layer", () -> new WeatherProfile.WeatherEffectType<>(LayerEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<BonusLightningEffect>> PLAYER_BONUS_LIGHTNING =
            EFFECTS.register("player_bonus_lightning", () -> new WeatherProfile.WeatherEffectType<>(BonusLightningEffect.CODEC));
}