package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;

public record DamageArmorEffect(List<EquipmentSlot> slots, int damage, int chance) implements WeatherProfile.WeatherEffect {
    public static final Codec<EquipmentSlot> SLOT_CODEC = Codec.STRING.xmap(
            EquipmentSlot::byName,
            EquipmentSlot::getName
    );

    public static final Codec<DamageArmorEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SLOT_CODEC.listOf().fieldOf("slots").forGetter(DamageArmorEffect::slots),
            Codec.INT.fieldOf("damage").forGetter(DamageArmorEffect::damage),
            Codec.INT.fieldOf("chance").forGetter(DamageArmorEffect::chance)
    ).apply(instance, DamageArmorEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.DAMAGE_ARMOR.get();
    }
}
