package io.github.tobyrue.high_voltage;

import com.mojang.logging.LogUtils;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
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
import org.slf4j.Logger;

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
        DataPackRegistriesHooks.addRegistryCodec();
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
