package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public record ModifyAttributeEffect(
        String attribute,
        String name,
        double value,
        AttributeModifier.Operation operation,
        int chance
) implements WeatherProfile.WeatherEffect {

    public ModifyAttributeEffect {
        String prefix = "[High Voltage Weather] ";
        if (!name.startsWith(prefix)) {
            name = prefix + name;
        }
    }

    public UUID getModifierId() {
        return UUID.nameUUIDFromBytes(name.getBytes());
    }

    public Attribute getAttribute() {
        return ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.parse(attribute));
    }

    public AttributeModifier createModifier() {
        return new AttributeModifier(getModifierId(), name, value, operation);
    }

    public static final Codec<ModifyAttributeEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("attribute").forGetter(ModifyAttributeEffect::attribute),
            Codec.STRING.fieldOf("name").forGetter(ModifyAttributeEffect::name),
            Codec.DOUBLE.fieldOf("value").forGetter(ModifyAttributeEffect::value),
            Codec.STRING.fieldOf("operation").xmap(
                    String::toUpperCase,
                    String::toLowerCase
            ).xmap(
                    AttributeModifier.Operation::valueOf,
                    AttributeModifier.Operation::name
            ).forGetter(ModifyAttributeEffect::operation),
            Codec.INT.fieldOf("chance").forGetter(ModifyAttributeEffect::chance)
    ).apply(instance, ModifyAttributeEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.MODIFY_ATTRIBUTE.get();
    }
}