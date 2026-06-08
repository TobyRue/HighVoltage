package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;

public record ExtinguishBlocksEffect(boolean netherrack) implements WeatherProfile.WeatherEffect {
    public static final Codec<ExtinguishBlocksEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("netherrack").forGetter(ExtinguishBlocksEffect::netherrack)
    ).apply(instance, ExtinguishBlocksEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.EXTINGUISH_BLOCKS.get();
    }
}