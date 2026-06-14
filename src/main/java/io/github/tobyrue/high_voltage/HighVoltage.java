package io.github.tobyrue.high_voltage;

import com.mojang.logging.LogUtils;
import io.github.tobyrue.high_voltage.data.MyNetworkHandler;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.ModWeatherEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
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

    public static final BooleanProperty WEATHERED = BooleanProperty.create("weathered");


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
        public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
            event.register((state, level, pos, tintIndex) -> {
                        if (level == null || pos == null || tintIndex < 0) {
                            return -1;
                        }

                        int originalColor;
                        if (state.is(Blocks.SPRUCE_LEAVES)) {
                            originalColor = net.minecraft.world.level.FoliageColor.getEvergreenColor();
                        } else if (state.is(Blocks.BIRCH_LEAVES)) {
                            originalColor = net.minecraft.world.level.FoliageColor.getBirchColor();
                        } else if (state.is(Blocks.MANGROVE_LEAVES)) {
                            originalColor = net.minecraft.world.level.FoliageColor.getMangroveColor();
                        } else {
                            originalColor = net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(level, pos);
                        }

                        if (state.hasProperty(HighVoltage.WEATHERED) && state.getValue(HighVoltage.WEATHERED)) {
                            Minecraft mc = Minecraft.getInstance();
                            ClientLevel world = mc.level;

                            if (world != null) {
                                net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biome = world.getBiome(pos);
                                WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);

                                if (profile != null) {
                                    int profileColor = profile.foliageColor();
                                    int alpha = (profileColor >> 24) & 0xFF;

                                    if (alpha == 0) return originalColor;

                                    int origR = (originalColor >> 16) & 0xFF;
                                    int origG = (originalColor >> 8) & 0xFF;
                                    int origB = originalColor & 0xFF;

                                    int overR = (profileColor >> 16) & 0xFF;
                                    int overG = (profileColor >> 8) & 0xFF;
                                    int overB = profileColor & 0xFF;

                                    float alphaPct = alpha / 255.0f;
                                    float invAlphaPct = 1.0f - alphaPct;

                                    int finalR = Math.min(255, (int) ((origR * invAlphaPct) + (overR * alphaPct)));
                                    int finalG = Math.min(255, (int) ((origG * invAlphaPct) + (overG * alphaPct)));
                                    int finalB = Math.min(255, (int) ((origB * invAlphaPct) + (overB * alphaPct)));

                                    return (finalR << 16) | (finalG << 8) | finalB;
                                }
                            }
                        }

                        return originalColor;
                    },
                    Blocks.OAK_LEAVES,
                    Blocks.SPRUCE_LEAVES,
                    Blocks.BIRCH_LEAVES,
                    Blocks.JUNGLE_LEAVES,
                    Blocks.ACACIA_LEAVES,
                    Blocks.DARK_OAK_LEAVES,
                    Blocks.MANGROVE_LEAVES,
                    Blocks.AZALEA_LEAVES,
                    Blocks.FLOWERING_AZALEA_LEAVES
            );
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> {
                        if (tintIndex < 0) return -1;

                        net.minecraft.world.item.BlockItem blockItem = (net.minecraft.world.item.BlockItem) stack.getItem();
                        Block block = blockItem.getBlock();

                        if (block == Blocks.SPRUCE_LEAVES) {
                            return net.minecraft.world.level.FoliageColor.getEvergreenColor();
                        } else if (block == Blocks.BIRCH_LEAVES) {
                            return net.minecraft.world.level.FoliageColor.getBirchColor();
                        } else if (block == Blocks.MANGROVE_LEAVES) {
                            return net.minecraft.world.level.FoliageColor.getMangroveColor();
                        }

                        return net.minecraft.world.level.FoliageColor.getDefaultColor();
                    },
                    Blocks.OAK_LEAVES,
                    Blocks.SPRUCE_LEAVES,
                    Blocks.BIRCH_LEAVES,
                    Blocks.JUNGLE_LEAVES,
                    Blocks.ACACIA_LEAVES,
                    Blocks.DARK_OAK_LEAVES,
                    Blocks.MANGROVE_LEAVES,
                    Blocks.AZALEA_LEAVES,
                    Blocks.FLOWERING_AZALEA_LEAVES
            );
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
