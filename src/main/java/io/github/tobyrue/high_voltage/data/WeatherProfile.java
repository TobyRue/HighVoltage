package io.github.tobyrue.high_voltage.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record WeatherProfile(
        HolderSet<Biome> biomes,
        @Nullable Precipitation precipitation,
        @Nullable Fog fog,
        List<WeatherEffect> effects,
        int baseLightningChance
) {


    public static final ResourceKey<Registry<WeatherProfile>> RESOURCE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "weather_profile"));

    public static final DeferredRegister<WeatherProfile> WEATHER_PROFILE_REGISTRY =
            DeferredRegister.create(RESOURCE_KEY, HighVoltage.MODID);

    public static void createRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<WeatherProfile>()
                .setName(RESOURCE_KEY.location()));
    }


    public static final Codec<WeatherProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(WeatherProfile::biomes),
            Precipitation.CODEC.optionalFieldOf("precipitation").forGetter(wp -> Optional.ofNullable(wp.precipitation())),
            Fog.CODEC.optionalFieldOf("fog").forGetter(wp -> Optional.ofNullable(wp.fog())),
            WeatherEffect.CODEC.listOf().optionalFieldOf("effects").forGetter(wp -> Optional.ofNullable(wp.effects)),
            Codec.INT.optionalFieldOf("base_lightning_chance").forGetter(wp -> Optional.of(wp.baseLightningChance))
    ).apply(instance,
            (biomes, precip, fog, effects, chance) ->
            new WeatherProfile(biomes, precip.orElse(null), fog.orElse(null), effects.orElse(List.of()), chance.orElse(10000))));


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


    public record Precipitation(
            ResourceLocation texture,
            int tint,
            float vx,
            float vy,
            boolean acts_like_rain,
            @Nullable ParticleType<?> particle,
            boolean sound
    ) {
        public static final Codec<Precipitation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(p -> Optional.ofNullable(p.texture)),
                Codec.STRING.optionalFieldOf("tint", "#FFFFFF")
                        .xmap(WeatherProfile::hexStringToInt, WeatherProfile::intToHexString)
                        .forGetter(Precipitation::tint),
                Codec.FLOAT.fieldOf("vx").forGetter(Precipitation::vx),
                Codec.FLOAT.fieldOf("vy").forGetter(Precipitation::vy),
                Codec.BOOL.optionalFieldOf("acts_like_rain").forGetter(p -> Optional.ofNullable(p.acts_like_rain)),
                ForgeRegistries.PARTICLE_TYPES.getCodec().optionalFieldOf("land_particle").forGetter(p -> Optional.ofNullable(p.particle())),
                Codec.BOOL.optionalFieldOf("land_sound").forGetter(p -> Optional.ofNullable(p.sound))
        ).apply(instance, (tex, tint, vx, vy, rain, part, snd) ->
                new Precipitation(tex.orElse(ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/none.png")), tint, vx, vy, rain.orElse(true), part.orElse(null), snd.orElse(false))));
    }
    public record Fog(int color, int start, int end) {
        public static final Codec<Fog> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("color").xmap(WeatherProfile::hexStringToInt, WeatherProfile::intToHexString).forGetter(Fog::color),
            Codec.INT.fieldOf("start").forGetter(Fog::start),
            Codec.INT.fieldOf("end").forGetter(Fog::end)
        ).apply(instance, Fog::new));
    }
    public interface WeatherEffect {



        Codec<WeatherEffect> CODEC = new Codec<>() {
            private Codec<WeatherEffect> cached;

            private Codec<WeatherEffect> getInternal() {
                if (cached == null) {
                    cached = WeatherEffectType.REGISTRY.get().getCodec().dispatch(
                            WeatherEffect::getType,
                            WeatherEffectType::codec
                    );
                }
                return cached;
            }

            @Override
            public <T> DataResult<Pair<WeatherEffect, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Pair<WeatherEffect, T>> result = getInternal().decode(ops, input);
                result.error().ifPresent(err -> System.out.println("Effect Parse Error: " + err.message()));
                return result;
            }

            @Override
            public <T> DataResult<T> encode(WeatherEffect input, DynamicOps<T> ops, T prefix) {
                return getInternal().encode(input, ops, prefix);
            }
        };
        WeatherEffectType<?> getType();
    }

    public record WeatherEffectType<T extends WeatherEffect>(Codec<T> codec) {

        public static final ResourceKey<Registry<WeatherEffectType<?>>> RESOURCE_KEY =
                ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "weather_effect_type"));

        public static final DeferredRegister<WeatherEffectType<?>> WEATHER_EFFECT_TYPES =
                DeferredRegister.create(RESOURCE_KEY, HighVoltage.MODID);

        public static final java.util.function.Supplier<IForgeRegistry<WeatherEffectType<?>>> REGISTRY =
                WEATHER_EFFECT_TYPES.makeRegistry(RegistryBuilder::new);

        public static void createRegistry(NewRegistryEvent event) {
            event.create(new RegistryBuilder<WeatherEffectType<?>>()
                    .setName(RESOURCE_KEY.location()));
        }
    }
}
