package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;

public record FreezeEffect(int freeze_ticks) implements WeatherProfile.WeatherEffect {
    public static final Codec<FreezeEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("freeze_ticks").forGetter(FreezeEffect::freeze_ticks)
    ).apply(instance, FreezeEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.FREEZE.get();
    }
}
