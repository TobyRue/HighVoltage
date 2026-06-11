package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.entity.EntityType;

public record DamageEffect(HolderSet<EntityType<?>> entity_predicate, float damage, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<DamageEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registry.ENTITY_TYPE_REGISTRY)
                    .fieldOf("entity_predicate")
                    .forGetter(DamageEffect::entity_predicate),
            Codec.FLOAT.fieldOf("damage").forGetter(DamageEffect::damage),
            Codec.INT.fieldOf("chance").forGetter(DamageEffect::chance)
    ).apply(instance, DamageEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.DAMAGE.get();
    }
}