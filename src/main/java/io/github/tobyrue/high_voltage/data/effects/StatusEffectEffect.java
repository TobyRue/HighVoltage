package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public record StatusEffectEffect(
        ResourceLocation effectId,
        int duration,
        int amplifier,
        boolean ambient,
        boolean visible,
        Optional<HolderSet<EntityType<?>>> entityPredicate,
        int chance
) implements WeatherProfile.WeatherEffect {

    public static final Codec<StatusEffectEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("effect").forGetter(StatusEffectEffect::effectId),
            Codec.INT.optionalFieldOf("duration", 200).forGetter(StatusEffectEffect::duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(StatusEffectEffect::amplifier),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(StatusEffectEffect::ambient),
            Codec.BOOL.optionalFieldOf("visible", false).forGetter(StatusEffectEffect::visible),
            RegistryCodecs.homogeneousList(Registry.ENTITY_TYPE_REGISTRY)
                    .optionalFieldOf("entity_predicate")
                    .forGetter(StatusEffectEffect::entityPredicate),
            Codec.INT.optionalFieldOf("chance", 1).forGetter(StatusEffectEffect::chance)
    ).apply(instance, StatusEffectEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.STATUS_EFFECT.get();
    }
}