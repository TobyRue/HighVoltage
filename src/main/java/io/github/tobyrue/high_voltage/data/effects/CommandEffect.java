package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;

public record CommandEffect(String run, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<CommandEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("run").forGetter(CommandEffect::run),
            Codec.INT.optionalFieldOf("chance", 1).forGetter(CommandEffect::chance)
    ).apply(instance, CommandEffect::new));


    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.COMMAND.get();
    }
}
