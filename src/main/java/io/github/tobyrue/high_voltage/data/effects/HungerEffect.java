package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;

public record HungerEffect(float exhaustion, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<HungerEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("exhaustion").forGetter(HungerEffect::exhaustion),
            Codec.INT.fieldOf("chance").forGetter(HungerEffect::chance)
    ).apply(instance, HungerEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.HUNGER.get();
    }
}