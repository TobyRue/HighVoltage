package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;

public record RingBellEffect(float volume, float pitch, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<RingBellEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("volume").forGetter(RingBellEffect::volume),
            Codec.FLOAT.fieldOf("pitch").forGetter(RingBellEffect::pitch),
            Codec.INT.fieldOf("chance").forGetter(RingBellEffect::chance)
    ).apply(instance, RingBellEffect::new));

//    static {
//        WeatherProfile.WeatherEffect.CODECS.put(ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "ring_bell"), CODEC);
//    }

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.RING_BELL.get();
    }
}
