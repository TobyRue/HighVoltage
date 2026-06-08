package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

public record WetEntitiesEffect() implements WeatherProfile.WeatherEffect {
    public static final Codec<WetEntitiesEffect> CODEC = Codec.unit(new WetEntitiesEffect());

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.WET_ENTITIES.get();
    }
}
