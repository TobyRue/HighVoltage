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
        Vec3 minVelocity,
        Vec3 maxVelocity,
        Optional<HolderSet<EntityType<?>>> entities,
        int chance
) implements WeatherProfile.WeatherEffect {

    public static final Codec<VelocityEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("velocity").forGetter(VelocityEffect::minVelocity),
            Vec3.CODEC.optionalFieldOf("max_velocity").forGetter(effect -> Optional.of(effect.maxVelocity)),
            RegistryCodecs.homogeneousList(Registry.ENTITY_TYPE_REGISTRY)
                    .optionalFieldOf("entities")
                    .forGetter(VelocityEffect::entities),
            Codec.INT.optionalFieldOf("chance", 1).forGetter(VelocityEffect::chance)
    ).apply(instance, (vel, maxVelOpt, entitiesOpt, chance) -> new VelocityEffect(
            vel,
            maxVelOpt.orElse(vel),
            entitiesOpt,
            chance
    )));

    /**
     * Helper method to compute a random velocity between minVelocity and maxVelocity vectors.
     */
    public Vec3 getRandomVelocity(RandomSource random) {
        double x = Mth.lerp(random.nextDouble(), minVelocity.x, maxVelocity.x);
        double y = Mth.lerp(random.nextDouble(), minVelocity.y, maxVelocity.y);
        double z = Mth.lerp(random.nextDouble(), minVelocity.z, maxVelocity.z);
        return new Vec3(x, y, z);
    }

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.VELOCITY.get();
    }
}