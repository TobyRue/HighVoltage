package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record VelocityEffect(
        Vec3 velocity,
        Vec3 max_velocity,
        Optional<HolderSet<EntityType<?>>> entity_type,
        int chance
) implements WeatherProfile.WeatherEffect {

    public static final Codec<VelocityEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("velocity").forGetter(VelocityEffect::velocity),
            Vec3.CODEC.optionalFieldOf("max_velocity").forGetter(effect -> Optional.of(effect.max_velocity)),
            RegistryCodecs.homogeneousList(Registry.ENTITY_TYPE_REGISTRY)
                    .optionalFieldOf("entity_type")
                    .forGetter(VelocityEffect::entity_type),
            Codec.INT.optionalFieldOf("chance", 1).forGetter(VelocityEffect::chance)
    ).apply(instance, (vel, maxVelOpt, entitiesOpt, chance) -> new VelocityEffect(
            vel,
            maxVelOpt.orElse(vel),
            entitiesOpt,
            chance
    )));

    /**
     * Helper method to compute a random velocity between velocity and max_velocity vectors.
     */
    public Vec3 getRandomVelocity(RandomSource random) {
        double x = Mth.lerp(random.nextDouble(), velocity.x, max_velocity.x);
        double y = Mth.lerp(random.nextDouble(), velocity.y, max_velocity.y);
        double z = Mth.lerp(random.nextDouble(), velocity.z, max_velocity.z);
        return new Vec3(x, y, z);
    }

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.VELOCITY.get();
    }
}