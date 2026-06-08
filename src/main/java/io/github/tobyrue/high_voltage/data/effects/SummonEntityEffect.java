package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.EntityType;
import java.util.Optional;

public record SummonEntityEffect(EntityType<?> entity, Optional<CompoundTag> data, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<SummonEntityEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(SummonEntityEffect::entity),
            CompoundTag.CODEC.optionalFieldOf("data").forGetter(SummonEntityEffect::data),
            Codec.INT.fieldOf("chance").forGetter(SummonEntityEffect::chance)
    ).apply(instance, SummonEntityEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.SUMMON_ENTITY.get();
    }
}