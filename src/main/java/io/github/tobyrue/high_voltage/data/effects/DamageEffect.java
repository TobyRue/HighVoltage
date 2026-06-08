package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public record DamageEffect(List<String> entityPredicate, float damage, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<DamageEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("entity_predicate").forGetter(DamageEffect::entityPredicate),
            Codec.FLOAT.fieldOf("damage").forGetter(DamageEffect::damage),
            Codec.INT.fieldOf("chance").forGetter(DamageEffect::chance)
    ).apply(instance, DamageEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.DAMAGE.get();
    }
}