package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record IgniteEffect(Optional<HolderSet<EntityType<?>>> entity_type, int chance, int duration) implements WeatherProfile.WeatherEffect {
    public static final Codec<IgniteEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registry.ENTITY_TYPE_REGISTRY)
                    .optionalFieldOf("entity_type")
                    .forGetter(IgniteEffect::entity_type),
            Codec.INT.optionalFieldOf("chance", 1).forGetter(IgniteEffect::chance),
            Codec.INT.optionalFieldOf("duration", 100).forGetter(IgniteEffect::duration)
    ).apply(instance, IgniteEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.IGNITE.get();
    }
}
