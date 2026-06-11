package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;

public record LayerEffect(
        ResourceLocation block,
        String property,
        int max_level,
        boolean noisy,
        boolean surface_only,
        int chance
) implements WeatherProfile.WeatherEffect {

    public static final Codec<LayerEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(LayerEffect::block),
            Codec.STRING.fieldOf("property").forGetter(LayerEffect::property),
            Codec.INT.fieldOf("max_level").forGetter(LayerEffect::max_level),
            Codec.BOOL.fieldOf("noisy").forGetter(LayerEffect::noisy),
            Codec.BOOL.optionalFieldOf("surface_only", true).forGetter(LayerEffect::surface_only),
            Codec.INT.fieldOf("chance").forGetter(LayerEffect::chance)
    ).apply(instance, LayerEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.LAYER.get();
    }
}