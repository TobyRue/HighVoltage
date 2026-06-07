package io.github.tobyrue.high_voltage.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public record WeatherProfile(
        List<HolderSet<Biome>> biomes, //TODO FINISH?
        @Nullable Precipitation precipitation,
        @Nullable Fog fog,
        List<WeatherEffect> effects, //TODO FINISH WeatherEffect
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
            Biome.LIST_CODEC.listOf().fieldOf("biomes").forGetter(WeatherProfile::biomes), //TODO
            Precipitation.CODEC.fieldOf("precipitation").forGetter(WeatherProfile::precipitation),
            Fog.CODEC.fieldOf("fog").forGetter(WeatherProfile::fog),
            WeatherEffect.CODEC.listOf().fieldOf("effects").forGetter(WeatherProfile::effects),
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
    public interface WeatherEffect {


        public static final HashMap<ResourceLocation, Codec<WeatherEffect>> CODECS = new HashMap<>();

        Codec<WeatherEffect> CODEC = WeatherEffectType.REGISTRY.get().getCodec()
                .dispatch(
                        WeatherEffect::getType,         // How to get the Type object from an effect
                        WeatherEffectType::codec        // How to get the Codec from the Type object
                );

        public WeatherEffectType getType();

//        public static final Codec<WeatherEffect> CODEC = Codec.pair(ResourceLocation.CODEC.fieldOf("type").codec(), Codec.PASSTHROUGH).xmap(
//                        pair -> CODECS.get(pair.getFirst()).parse(pair.getSecond()),
//                        (WeatherEffect effect) -> {
//                        new Dynamic<WeatherEffect>(JsonOps.INSTANCE, CODECS.get(effect.getType().getKey()).encodeStart(JsonOps.INSTANCE, effect))
//                            return new Pair<ResourceLocation, Dynamic<?>>(effect.getType().getKey(), CODECS.get(effect.getType().getKey()).encodeStart(JsonOps.INSTANCE, effect)))
//                        }
//                );
//                RecordCodecBuilder.create(instance -> instance.group(
//                Codec.STRING.fieldOf("field"),
//                Codec.PASSTHROUGH.fieldOf("args").xmap((a) -> {
//
//                }, (b) -> {
//
//                })
//        ).apply(instance, WeatherEffect::new));
//        public static final Codec<WeatherEffect> WEATHER_EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
//
//        ).apply(instance, WeatherEffect::new));

    }

    public static final class WeatherEffectType<T extends WeatherEffect> {
        //        public static final ResourceKey<Registry<WeatherEffectType<?>>> WEATHER_EFFECT_TYPE_REGISTRY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "weather_effect_type"));
        private final ResourceLocation key;
        private final Function<Dynamic<?>, T> create;

        public WeatherEffectType(ResourceLocation key, Function<Dynamic<?>, T> create) {
            this.key = key;
            this.create = create;
        }

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

        public ResourceLocation getKey() {
            return key;
        }
    }
}
