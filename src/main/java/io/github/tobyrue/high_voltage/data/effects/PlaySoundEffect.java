package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.IRandomTickWeatherEffect;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.ForgeRegistries;

public record PlaySoundEffect(
        ResourceLocation sound,
        float volume,
        float pitch,
        int chance
) implements WeatherProfile.WeatherEffect, IRandomTickWeatherEffect {

    public static final Codec<PlaySoundEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sound").forGetter(PlaySoundEffect::sound),
            Codec.FLOAT.fieldOf("volume").forGetter(PlaySoundEffect::volume),
            Codec.FLOAT.fieldOf("pitch").forGetter(PlaySoundEffect::pitch),
            Codec.INT.fieldOf("chance").forGetter(PlaySoundEffect::chance)
    ).apply(instance, PlaySoundEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.PLAY_SOUND.get();
    }

    @Override
    public int getChance() {
        return this.chance();
    }

    @Override
    public void execute(ServerLevel world, ServerPlayer player, BlockPos targetPos, boolean isOutside) {
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(this.sound());
        if (soundEvent != null) {
            world.playSound(null, targetPos, soundEvent, SoundSource.WEATHER, this.volume(), this.pitch());
        }
    }
}