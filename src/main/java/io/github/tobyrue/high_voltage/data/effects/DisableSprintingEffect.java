package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import io.github.tobyrue.high_voltage.data.WeatherProfile;

public record DisableSprintingEffect() implements WeatherProfile.WeatherEffect {
    public static final Codec<DisableSprintingEffect> CODEC = Codec.unit(DisableSprintingEffect::new);

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.DISABLE_SPRINTING.get();
    }
}