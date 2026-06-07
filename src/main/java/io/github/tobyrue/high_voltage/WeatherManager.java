package io.github.tobyrue.high_voltage;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherManager {
    public record WeatherProfile(ResourceLocation texture, int fogColor, int textureTint, float fogStart, float fogEnd, float vSpeed, float hWind, String particleType, boolean hasRainSound) {}

    private static final WeatherProfile DEFAULT_RAIN = new WeatherProfile(
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/rain.png"),
            0x8E8945, 0xFFFFFF, 20.0f, 60.0f, 1.0f, 0.0f, "minecraft:rain", true
    );

    private static final WeatherProfile DEFAULT_SNOW = new WeatherProfile(
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/environment/snow.png"),
            0xFFFFFF, 0xFFFFFF, 8.0f, 32.0f, 1.0f, 0.0f, "none", false
    );
    private static final Map<String, WeatherProfile> PROFILE_MAP = new HashMap<>();

    public static void parseConfig() {
        PROFILE_MAP.clear();
        for (String entry : CommonConfig.WEATHER_PROFILES.get()) {
            try {
                String[] p = entry.split("\\|");
                if (p.length >= 10) {
                    PROFILE_MAP.put(p[0].trim(), new WeatherProfile(
                            ResourceLocation.parse(p[1].trim()),
                            Integer.decode(p[2].trim()),
                            Integer.decode(p[3].trim()),
                            Float.parseFloat(p[4].trim()),
                            Float.parseFloat(p[5].trim()),
                            Float.parseFloat(p[6].trim()),
                            Float.parseFloat(p[7].trim()),
                            p[8].trim(),
                            Boolean.parseBoolean(p[9].trim())
                    ));
                }
            } catch (Exception e) {
                System.err.println("High Voltage: Error parsing profile: " + entry);
            }
        }
    }


    public static Map<String, WeatherProfile> getProfileMap() {
        return PROFILE_MAP;
    }

    public static WeatherProfile getCurrentProfile(Holder<Biome> biome) {
        for (Map.Entry<String, WeatherProfile> entry : PROFILE_MAP.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("#")) {
                TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY, ResourceLocation.parse(key.substring(1)));
                if (biome.is(tag)) return entry.getValue();
            } else {
                if (biome.unwrapKey().get().location().toString().equals(key)) return entry.getValue();
            }
        }
        if (biome.value().getBaseTemperature() > 0.15) {
            return DEFAULT_RAIN;
        } else {
            return DEFAULT_SNOW;
        }
    }

    public class ServerWeatherManager {
        public record ServerWeatherProfile(int lightningChance, int playerChance, List<String> effects) {}

        private static final ServerWeatherProfile SERVER_DEFAULT_RAIN = new ServerWeatherProfile(
                10000, 500, List.of()
        );

        private static final ServerWeatherProfile SERVER_DEFAULT_SNOW = new ServerWeatherProfile(
                10000, 500, List.of()
        );
        private static final Map<String, ServerWeatherProfile> SERVER_PROFILE_MAP = new HashMap<>();

        public static void parseConfig() {
            SERVER_PROFILE_MAP.clear();
            for (String entry : ServerConfig.SERVER_WEATHER_PROFILES.get()) {
                try {
                    String[] p = entry.split("\\|");
                    if (p.length >= 4) {
                        java.util.List<String> effectList = java.util.Arrays.asList(p[3].split(";"));
                        SERVER_PROFILE_MAP.put(p[0].trim(), new ServerWeatherProfile(
                                Integer.decode(p[1].trim()),
                                Integer.decode(p[2].trim()),
                                effectList
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("High Voltage: Error parsing server profile: " + entry);
                }
            }
        }


        public static Map<String, ServerWeatherProfile> getServerProfileMap() {
            return SERVER_PROFILE_MAP;
        }

        public static ServerWeatherProfile getCurrentServerProfile(Holder<Biome> biome) {
            for (Map.Entry<String, ServerWeatherProfile> entry : SERVER_PROFILE_MAP.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("#")) {
                    TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY, ResourceLocation.parse(key.substring(1)));
                    if (biome.is(tag)) return entry.getValue();
                } else {
                    if (biome.unwrapKey().get().location().toString().equals(key)) return entry.getValue();
                }
            }
            if (biome.value().getBaseTemperature() > 0.15) {
                return SERVER_DEFAULT_RAIN;
            } else {
                return SERVER_DEFAULT_SNOW;
            }
        }
    }
}