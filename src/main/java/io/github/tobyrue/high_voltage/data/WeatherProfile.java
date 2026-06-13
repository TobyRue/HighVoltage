package io.github.tobyrue.high_voltage.data;

import com.google.gson.JsonElement;
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

import javax.json.JsonObject;
import java.lang.reflect.RecordComponent;
import java.util.*;

public record WeatherProfile(
        HolderSet<Biome> biomes,
        @Nullable Precipitation precipitation,
        @Nullable Fog fog,
        List<WeatherEffect> effects,
        int baseLightningChance,
        int foliageColor
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
            Codec.INT.optionalFieldOf("base_lightning_chance").forGetter(wp -> Optional.of(wp.baseLightningChance)),
            Codec.STRING.optionalFieldOf("foliage_color", "#00FFFFFF")
                    .xmap(WeatherProfile::hexStringToIntWithAlpha, WeatherProfile::intToHexStringWithAlpha)
                    .forGetter(WeatherProfile::foliageColor)
    ).apply(instance,
            (biomes, precip, fog, effects, chance, fc) ->
            new WeatherProfile(biomes, precip.orElse(null), fog.orElse(null), effects.orElse(List.of()), chance.orElse(10000), fc)));

    public static Integer hexStringToIntWithAlpha(final String hex) {
        try {
            long parsed = Long.parseLong(hex.substring(1), 16);

            if (hex.length() - 1 == 6) {
                parsed |= 0xFF000000L;
            }
            return (int) parsed;
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            return 0xFFFFFFFF;
        }
    }

    public static String intToHexStringWithAlpha(final Integer hex) {
        try {
            return "#" + String.format("%08X", hex);
        } catch (Exception ignored) {
            return "#FFFFFF";
        }
    }

    public static Integer hexStringToInt(final String hex) {

        try {
            return Integer.parseInt(hex.substring(1), 16);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            return 0xFFFFFF;
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
                    cached = WeatherProfile.WeatherEffectType.REGISTRY.get().getCodec().dispatch(
                            WeatherEffect::getType,
                            WeatherProfile.WeatherEffectType::codec
                    );
                }
                return cached;
            }

            @Override
            public <T> DataResult<Pair<WeatherEffect, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Pair<WeatherEffect, T>> result = getInternal().decode(ops, input);

                if (result.error().isPresent()) {
                    String rawError = result.error().get().message();

                    if (ops instanceof JsonOps || ops.getClass().getSimpleName().contains("RegistryOps")) {
                        try {
                            com.google.gson.JsonElement jsonElement = ops.convertTo(JsonOps.INSTANCE, input);
                            if (jsonElement instanceof com.google.gson.JsonObject obj) {
                                String fileNameContext = "Unknown Data File / Memory Stream";

                                try {
                                    java.lang.reflect.Field contextField = ops.getClass().getDeclaredField("registryLifecycle");
                                    contextField.setAccessible(true);
                                    Object lifecycle = contextField.get(ops);
                                    if (lifecycle != null) {
                                        fileNameContext = lifecycle.toString();
                                    }
                                } catch (Exception ignored) {
                                    fileNameContext = ops.toString();
                                }

                                high_voltage$diagnoseEffectError(obj, rawError, fileNameContext);
                            }
                        } catch (Exception e) {
                            System.err.println("[High Voltage Diagnostics Failed]: " + e.getMessage());
                        }
                    }
                }
                return result;
            }

            @Override
            public <T> DataResult<T> encode(WeatherEffect input, DynamicOps<T> ops, T prefix) {
                return getInternal().encode(input, ops, prefix);
            }

            private void high_voltage$diagnoseEffectError(com.google.gson.JsonObject json, String dfuMessage, String fileOrigin) {                System.err.println("\n=================================================================");
                System.err.println(" [High Voltage]: WEATHER EFFECT PARSING FAILURE");
                System.err.println(" SOURCE FILE: " + fileOrigin);
                System.err.println("=================================================================");
                System.err.println("DFU Core Error: " + dfuMessage);

                if (!json.has("type")) {
                    System.err.println("\n MISSING CRITICAL FIELD: \"type\"");
                    System.err.println("Fix: Every weather effect object inside your \"effects\" array must specify a \"type\".");
                    return;
                }

                String typeStr = json.get("type").getAsString();
                System.err.println("Target Effect Type: " + typeStr);

                IForgeRegistry<WeatherProfile.WeatherEffectType<?>> registry = WeatherProfile.WeatherEffectType.REGISTRY.get();
                ResourceLocation targetKey = ResourceLocation.parse(typeStr);
                WeatherProfile.WeatherEffectType<?> effectType = registry.getValue(targetKey);

                Set<ResourceLocation> registeredKeys = registry.getKeys();
                List<String> stringKeys = registeredKeys.stream().map(ResourceLocation::toString).toList();

                if (effectType == null) {
                    System.err.println("\n UNKNOWN EFFECT TYPE: \"" + typeStr + "\"");
                    String closestType = high_voltage$findClosestMatch(typeStr, stringKeys);
                    if (closestType != null) {
                        System.err.println("  Did you mean: \"" + closestType + "\"?");
                    }
                    System.err.println("=================================================================\n");
                    return;
                }

                Map<String, String> expectedFields = new LinkedHashMap<>();
                expectedFields.put("type", "String (Registry ID)");

                try {
                    WeatherProfile.WeatherEffectType<?> typeInstance = registry.getValue(targetKey);
                    Class<?> recordClass = null;

                    if (typeInstance != null) {
                        Optional<?> sampleObj = typeInstance.codec().parse(JsonOps.INSTANCE, json).result();
                        if (sampleObj.isPresent()) {
                            recordClass = sampleObj.get().getClass();
                        } else {
                            for (WeatherProfile.WeatherEffectType<?> registeredType : registry.getValues()) {
                                if (registry.getKey(registeredType).equals(targetKey)) {
                                    String codecClassName = registeredType.codec().getClass().getName();
                                    for (java.lang.reflect.Field field : io.github.tobyrue.high_voltage.data.effects.ModWeatherEffects.class.getDeclaredFields()) {
                                        if (field.getType().isAssignableFrom(RegistryObject.class)) {
                                            RegistryObject<?> ro = (RegistryObject<?>) field.get(null);
                                            if (ro.get() == registeredType) {
                                                java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) field.getGenericType();
                                                java.lang.reflect.ParameterizedType effectPt = (java.lang.reflect.ParameterizedType) pt.getActualTypeArguments()[0];
                                                Class<?> rawEffectClass = (Class<?>) effectPt.getActualTypeArguments()[0];
                                                if (rawEffectClass.isRecord()) {
                                                    recordClass = rawEffectClass;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (recordClass != null && recordClass.isRecord()) {
                        for (RecordComponent component : recordClass.getRecordComponents()) {
                            String jsonFieldName = component.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
                            expectedFields.put(jsonFieldName, component.getType().getSimpleName());
                        }
                    }
                } catch (Exception ignored) {
                }

                if (expectedFields.size() <= 1) {
                    expectedFields.put("chance", "Integer (Optional)");
                }

                System.err.println("\n AUTOMATED PARAMETER SIGNATURE FOR [" + typeStr + "]:");
                expectedFields.forEach((param, type) -> System.err.println("  • \"" + param + "\" -> [Type: " + type + "]"));

                System.err.println("\n MISMATCH ANALYSIS:");
                boolean foundTypos = false;

                for (String providedKey : json.keySet()) {
                    if (providedKey.equals("type")) continue;

                    if (!expectedFields.containsKey(providedKey)) {
                        foundTypos = true;
                        System.err.println(" Found unexpected parameter: \"" + providedKey + "\"");
                        String match = high_voltage$findClosestMatch(providedKey, expectedFields.keySet());
                        if (match != null) {
                            System.err.println("    Did you mean: \"" + match + "\"?");
                        }
                    }
                }

                if (!foundTypos) {
                    System.err.println(" All provided JSON parameter keys match the record variables structure.");
                    System.err.println("   Ensure your data formats match the parameter types listed above.");
                }

                System.err.println("\n RAW EFFECT SOURCE SNIPPET:");
                System.err.println(json.toString());
                System.err.println("=================================================================\n");
            }

            private String high_voltage$findClosestMatch(String input, Collection<String> options) {
                String closest = null;
                int shortestDistance = Integer.MAX_VALUE;
                for (String option : options) {
                    int distance = high_voltage$getLevenshteinDistance(input.toLowerCase(), option.toLowerCase());
                    if (distance < shortestDistance && distance <= 3) {
                        shortestDistance = distance;
                        closest = option;
                    }
                }
                return closest;
            }

            private int high_voltage$getLevenshteinDistance(String s, String t) {
                if (s.equals(t)) return 0;
                if (s.isEmpty()) return t.length();
                if (t.isEmpty()) return s.length();
                int[] v0 = new int[t.length() + 1];
                int[] v1 = new int[t.length() + 1];
                for (int i = 0; i < v0.length; i++) v0[i] = i;
                for (int i = 0; i < s.length(); i++) {
                    v1[0] = i + 1;
                    for (int j = 0; j < t.length(); j++) {
                        int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
                        v1[j + 1] = Math.min(Math.min(v1[j] + 1, v0[j + 1] + 1), v0[j] + cost);
                    }
                    System.arraycopy(v1, 0, v0, 0, v0.length);
                }
                return v0[t.length()];
            }
        };

        WeatherProfile.WeatherEffectType<?> getType();
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
