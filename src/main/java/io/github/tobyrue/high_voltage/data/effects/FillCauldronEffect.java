package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;

public record FillCauldronEffect(String fluid, boolean surface_only, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<FillCauldronEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("fluid").forGetter(FillCauldronEffect::fluid),
            Codec.BOOL.optionalFieldOf("surface_only", true).forGetter(FillCauldronEffect::surface_only),
            Codec.INT.fieldOf("chance").forGetter(FillCauldronEffect::chance)
    ).apply(instance, FillCauldronEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.FILL_CAULDRON.get();
    }
}