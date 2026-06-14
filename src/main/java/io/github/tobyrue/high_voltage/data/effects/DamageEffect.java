package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record DamageEffect(Optional<HolderSet<EntityType<?>>> entity_type, float damage, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<DamageEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registry.ENTITY_TYPE_REGISTRY)
                    .optionalFieldOf("entity_type")
                    .forGetter(DamageEffect::entity_type),
            Codec.FLOAT.fieldOf("damage").forGetter(DamageEffect::damage),
            Codec.INT.fieldOf("chance").forGetter(DamageEffect::chance)
    ).apply(instance, DamageEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.DAMAGE.get();
    }
}