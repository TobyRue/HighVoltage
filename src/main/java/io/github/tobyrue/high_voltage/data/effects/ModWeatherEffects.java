package io.github.tobyrue.high_voltage.data.effects;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModWeatherEffects {
    public static final DeferredRegister<WeatherProfile.WeatherEffectType<?>> EFFECTS =
            WeatherProfile.WeatherEffectType.WEATHER_EFFECT_TYPES;

    public static final RegistryObject<WeatherProfile.WeatherEffectType<CommandEffect>> COMMAND =
            EFFECTS.register("run", () -> new WeatherProfile.WeatherEffectType<>(CommandEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<RingBellEffect>> RING_BELL =
            EFFECTS.register("ring_bell", () -> new WeatherProfile.WeatherEffectType<>(RingBellEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<PlaySoundEffect>> PLAY_SOUND =
            EFFECTS.register("play_sound", () -> new WeatherProfile.WeatherEffectType<>(PlaySoundEffect.CODEC));

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

    public static final RegistryObject<WeatherProfile.WeatherEffectType<IgniteEffect>> IGNITE =
            EFFECTS.register("ignite", () -> new WeatherProfile.WeatherEffectType<>(IgniteEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<VelocityEffect>> VELOCITY =
            EFFECTS.register("velocity", () -> new WeatherProfile.WeatherEffectType<>(VelocityEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<BonusLightningEffect>> PLAYER_BONUS_LIGHTNING =
            EFFECTS.register("player_bonus_lightning", () -> new WeatherProfile.WeatherEffectType<>(BonusLightningEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<DisableElytraEffect>> DISABLE_ELYTRA =
            EFFECTS.register("disable_elytra", () -> new WeatherProfile.WeatherEffectType<>(DisableElytraEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<DisableSprintingEffect>> DISABLE_SPRINTING =
            EFFECTS.register("disable_sprinting", () -> new WeatherProfile.WeatherEffectType<>(DisableSprintingEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<FillCauldronEffect>> FILL_CAULDRON =
            EFFECTS.register("fill_cauldron", () -> new WeatherProfile.WeatherEffectType<>(FillCauldronEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<ModifyAttributeEffect>> MODIFY_ATTRIBUTE =
            EFFECTS.register("modify_attribute", () -> new WeatherProfile.WeatherEffectType<>(ModifyAttributeEffect.CODEC));

    public static final RegistryObject<WeatherProfile.WeatherEffectType<StatusEffectEffect>> STATUS_EFFECT =
            EFFECTS.register("status_effect", () -> new WeatherProfile.WeatherEffectType<>(StatusEffectEffect.CODEC));
}