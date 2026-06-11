package io.github.tobyrue.high_voltage.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.effects.FreezeEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.nbt.TagTypes;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class WeatherProfileLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static Map<ResourceLocation, WeatherProfile> PROFILES = Collections.emptyMap();

    public static final WeatherProfile DEFAULT_RAIN = new WeatherProfile(
            HolderSet.direct(),

            new WeatherProfile.Precipitation(
                    ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/heavy_rain.png"),
                    0xFFFFFF, 1.0f, 4.0f,
                    true,
                    ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse("minecraft:rain")),
                    true
            ),

            new WeatherProfile.Fog(0x8E8945, 5, 30),
            List.of(),

            10000
    );
    public static final WeatherProfile DEFAULT_SNOW = new WeatherProfile(
            HolderSet.direct(),

            new WeatherProfile.Precipitation(
                    ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/heavy_snow.png"),
                    0xFFFFFF, 1.5f, 4.5f,
                    true,
                    ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse("minecraft:item_snowball")),
                    false
            ),
            new WeatherProfile.Fog(0xFFFFFF, 5, 40),
            List.of(new FreezeEffect(100)),
            10000
    );
    public static final WeatherProfile DEFAULT_NONE = new WeatherProfile(
            HolderSet.direct(),
            new WeatherProfile.Precipitation(
                    ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/none.png"),
                    0xFFFFFF, 0f, 0f,
                    false,
                    null,
                    false
            ),
            null,
            List.of(),
            10000
    );

    public WeatherProfileLoader() {
        super(GSON, "weather_profiles");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        Map<ResourceLocation, WeatherProfile> newMap = new HashMap<>();


        RegistryAccess access = RegistryAccess.builtinCopy();

        if (ServerLifecycleHooks.getCurrentServer() != null) {
            access = ServerLifecycleHooks.getCurrentServer().registryAccess();
        }

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, access);

        pObject.forEach((location, json) -> {
            WeatherProfile.CODEC.parse(ops, json)
                    .resultOrPartial(err -> System.err.println("Error loading weather profile " + location + ": " + err))
                    .ifPresent(profile -> newMap.put(location, profile));
        });

        PROFILES = newMap;
    }
    /**
     * Finds the first profile that matches the given biome.
     * Checks both specific Biome IDs and Biome Tags (HolderSet).
     */
    public static Optional<WeatherProfile> getProfileForBiome(Holder<Biome> biome) {
        ResourceLocation targetId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        for (WeatherProfile profile : PROFILES.values()) {
            boolean matches = profile.biomes().stream().anyMatch(holder ->
                    holder.unwrapKey().map(key -> key.location().equals(targetId)).orElse(false)
            );

            if (matches) return Optional.of(profile);

            var bound = profile.biomes().unwrap();
            if (bound.left().isPresent()) {
                if (biome.is(bound.left().get())) {
                    return Optional.of(profile);
                }
            }
        }
        return Optional.empty();
    }
    public static WeatherProfile getProfileForBiomeWithFallback(Holder<Biome> biome) {
        ResourceLocation targetId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        for (WeatherProfile profile : PROFILES.values()) {
            boolean matches = profile.biomes().stream().anyMatch(holder ->
                    holder.unwrapKey().map(key -> key.location().equals(targetId)).orElse(false)
            );

            if (matches) return Optional.of(profile).orElse(biome.get().getPrecipitation().equals(Biome.Precipitation.NONE) ? DEFAULT_NONE : (biome.get().getBaseTemperature() < 0.15 ? DEFAULT_SNOW : DEFAULT_RAIN));

            var bound = profile.biomes().unwrap();
            if (bound.left().isPresent()) {
                if (biome.is(bound.left().get())) {
                    return Optional.of(profile).orElse(biome.get().getPrecipitation().equals(Biome.Precipitation.NONE) ? DEFAULT_NONE : (biome.get().getBaseTemperature() < 0.15 ? DEFAULT_SNOW : DEFAULT_RAIN));
                }
            }
        }
        return biome.get().getPrecipitation().equals(Biome.Precipitation.NONE) ? DEFAULT_NONE : (biome.get().getBaseTemperature() < 0.15 ? DEFAULT_SNOW : DEFAULT_RAIN);
    }

    public static WeatherProfile getProfileForBiomeWithFallback(Holder<Biome> biome, Level level) {
        ResourceLocation targetId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        for (WeatherProfile profile : PROFILES.values()) {
            boolean matches = profile.biomes().stream().anyMatch(holder ->
                    holder.unwrapKey().map(key -> key.location().equals(targetId)).orElse(false)
            );

            if (matches) return (Optional.of(profile).orElse(level.dimension() != Level.OVERWORLD ? DEFAULT_NONE : biome.get().getPrecipitation().equals(Biome.Precipitation.NONE) ? DEFAULT_NONE : (biome.get().getBaseTemperature() < 0.15 ? DEFAULT_SNOW : DEFAULT_RAIN)));
            var bound = profile.biomes().unwrap();
            if (bound.left().isPresent()) {
                if (biome.is(bound.left().get())) {
                    return (Optional.of(profile).orElse(level.dimension() != Level.OVERWORLD ? DEFAULT_NONE : biome.get().getPrecipitation().equals(Biome.Precipitation.NONE) ? DEFAULT_NONE : (biome.get().getBaseTemperature() < 0.15 ? DEFAULT_SNOW : DEFAULT_RAIN)));
                }
            }
        }
        return level.dimension() != Level.OVERWORLD ? DEFAULT_NONE : (biome.get().getPrecipitation().equals(Biome.Precipitation.NONE) ? DEFAULT_NONE : (biome.get().getBaseTemperature() < 0.15 ? DEFAULT_SNOW : DEFAULT_RAIN));
    }

    public static Optional<WeatherProfile.WeatherEffectType<?>> getEffectTypeById(ResourceLocation id) {
        return Optional.ofNullable(WeatherProfile.WeatherEffectType.REGISTRY.get().getValue(id));
    }
    /**
     * Searches a profile for an effect matching a specific RegistryObject type.
     * Usage: getEffect(profile, ModWeatherEffects.COMMAND)
     */
    public static <T extends WeatherProfile.WeatherEffect> T getEffect(WeatherProfile profile, RegistryObject<WeatherProfile.WeatherEffectType<T>> type) {
        for (WeatherProfile.WeatherEffect effect : profile.effects()) {
            if (effect.getType() == type.get()) {
                return (T) effect;
            }
        }
        return null;
    }

    public static Map<ResourceLocation, WeatherProfile> getAllProfiles() {
        return PROFILES;
    }
}