package io.github.tobyrue.high_voltage.data;

import com.mojang.datafixers.types.templates.Tag;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.nbt.TagType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record WeatherProfile(
        List<String> biomes, //TODO FINISH?
        @Nullable Precipitation precipitation,
        @Nullable Fog fog,
        List<String> effects, //TODO FINISH WeatherEffect
        int baseLightningChance
) {

    public static final Codec<WeatherProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("biomes").forGetter(WeatherProfile::biomes), //TODO
            Precipitation.CODEC.fieldOf("precipitation").forGetter(WeatherProfile::precipitation),
            Fog.CODEC.fieldOf("fog").forGetter(WeatherProfile::fog),
            Codec.STRING.listOf().fieldOf("effects").forGetter(WeatherProfile::effects), //TODO
            Codec.INT.fieldOf("base_lightning_chance").forGetter(WeatherProfile::baseLightningChance)
    ).apply(instance, WeatherProfile::new));


    public static Integer hexStringToInt(final String hex) {
        try {
            return Integer.parseInt(hex.substring(1), 16);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            return 0;
        }
    }
    public static String intToHexString(final Integer hex) {
        try {
            return "#" + Integer.toString(hex, 16).substring(2);
        } catch (IndexOutOfBoundsException ignored) {
            return "#FFFFFF";
        }
    }


    record Precipitation(ResourceLocation texture, int tint, float vx, float vy, ParticleType<?> particle, boolean sound) {
        public static final Codec<Precipitation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(Precipitation::texture),
                Codec.STRING.fieldOf("tint").xmap(WeatherProfile::hexStringToInt, WeatherProfile::intToHexString).forGetter(Precipitation::tint),
                Codec.FLOAT.fieldOf("vx").forGetter(Precipitation::vx),
                Codec.FLOAT.fieldOf("vx").forGetter(Precipitation::vy),
                ForgeRegistries.PARTICLE_TYPES.getCodec().fieldOf("particle").forGetter(Precipitation::particle),
                Codec.BOOL.fieldOf("sound").forGetter(Precipitation::sound)
            ).apply(instance, Precipitation::new));
    }
    record Fog(int color, int start, int end) {
        public static final Codec<Fog> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("color").xmap(WeatherProfile::hexStringToInt, WeatherProfile::intToHexString).forGetter(Fog::color),
            Codec.INT.fieldOf("start").forGetter(Fog::start),
            Codec.INT.fieldOf("end").forGetter(Fog::end)
        ).apply(instance, Fog::new));
    }
    abstract static class WeatherEffect {
//        public static final Codec<WeatherEffect> WEATHER_EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
//
//        ).apply(instance, WeatherEffect::new));
    }
}
