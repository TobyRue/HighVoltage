package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;

public record PlaySoundEffect(
        ResourceLocation soundId,
        float volume,
        float pitch,
        int chance
) implements WeatherProfile.WeatherEffect {

    public static final Codec<PlaySoundEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sound").forGetter(PlaySoundEffect::soundId),
            Codec.FLOAT.fieldOf("volume").forGetter(PlaySoundEffect::volume),
            Codec.FLOAT.fieldOf("pitch").forGetter(PlaySoundEffect::pitch),
            Codec.INT.fieldOf("chance").forGetter(PlaySoundEffect::chance)
    ).apply(instance, PlaySoundEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.PLAY_SOUND.get();
    }
}