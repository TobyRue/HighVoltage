package io.github.tobyrue.high_voltage;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.ModWeatherEffects;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import net.minecraftforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HighVoltage.MODID)
public class HighVoltage {
    public static final String MODID = "high_voltage";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<Registry<WeatherProfile>> WEATHER_PROFILE_REGISTRY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "weather_profiles"));

    public HighVoltage(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
//
//
        MinecraftForge.EVENT_BUS.register(this);
//
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);


        modEventBus.addListener(this::onConfigLoad);
        modEventBus.addListener(this::onConfigReload);

        WeatherProfile.WEATHER_PROFILE_REGISTRY.register(modEventBus);

        ModWeatherEffects.EFFECTS.register(modEventBus);

        modEventBus.addListener(WeatherProfile::createRegistry);

        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);
    }

    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new WeatherProfileLoader());
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        WeatherProfile.WeatherEffectType.createRegistry(event);
        WeatherProfile.createRegistry(event);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            WeatherManager.parseConfig();
            WeatherManager.ServerWeatherManager.parseConfig();
            LOGGER.info("High Voltage: Weather profiles loaded.");
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            WeatherManager.parseConfig();
            WeatherManager.ServerWeatherManager.parseConfig();
            LOGGER.info("High Voltage: Weather profiles reloaded.");
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }


    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
