package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;

public record BonusLightningEffect(int chunk_radius, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<BonusLightningEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(BonusLightningEffect::chunk_radius),
            Codec.INT.fieldOf("chance").forGetter(BonusLightningEffect::chance)
    ).apply(instance, BonusLightningEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.PLAYER_BONUS_LIGHTNING.get();
    }
}
