package io.github.tobyrue.high_voltage.mixin;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributeMap.class)
public interface AttributeMapAccessor {

    @Accessor("attributes")
    Map<Attribute, AttributeInstance> high_voltage$getAttributesMap();
}
