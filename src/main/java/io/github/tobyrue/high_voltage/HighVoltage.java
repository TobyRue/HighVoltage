package io.github.tobyrue.high_voltage;

import com.mojang.logging.LogUtils;
import io.github.tobyrue.high_voltage.data.MyNetworkHandler;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.ModWeatherEffects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

@Mod(HighVoltage.MODID)
public class HighVoltage {
    public static final String MODID = "high_voltage";

    public HighVoltage(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);



        WeatherProfile.WEATHER_PROFILE_REGISTRY.register(modEventBus);

        ModWeatherEffects.EFFECTS.register(modEventBus);

        modEventBus.addListener(WeatherProfile::createRegistry);

        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListener);

        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MyNetworkHandler::register);
    }

    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new WeatherProfileLoader());
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        WeatherProfile.WeatherEffectType.createRegistry(event);
        WeatherProfile.createRegistry(event);
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
