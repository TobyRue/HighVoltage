package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import io.github.tobyrue.high_voltage.data.WeatherProfile;

public record DisableElytraEffect() implements WeatherProfile.WeatherEffect {
    public static final Codec<DisableElytraEffect> CODEC = Codec.unit(DisableElytraEffect::new);

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.DISABLE_ELYTRA.get();
    }
}