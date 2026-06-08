package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;

public record CommandEffect(String command, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<CommandEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("run").forGetter(CommandEffect::command),
            Codec.INT.fieldOf("chance").forGetter(CommandEffect::chance)
    ).apply(instance, CommandEffect::new));


    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.COMMAND.get();
    }
}
