package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;

public record RingBellEffect(float radius, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<RingBellEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("radius").forGetter(RingBellEffect::radius),
            Codec.INT.fieldOf("chance").forGetter(RingBellEffect::chance)
    ).apply(instance, RingBellEffect::new));



    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.RING_BELL.get();
    }
}
