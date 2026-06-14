package io.github.tobyrue.high_voltage;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HighVoltage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    private static final float FOG_CHANGE_SPEED = 0.02f;

    private static float curR, curG, curB;
    private static float curStart = 8.0f;
    private static float curEnd = 32.0f;
    private static boolean initialized = false;

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Holder<Biome> biome = mc.level.getBiome(event.getCamera().getBlockPosition());
        WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, mc.level);

        float targetR = event.getRed();
        float targetG = event.getGreen();
        float targetB = event.getBlue();

        if (profile.fog() != null) {
            if (mc.level.isThundering() && OutsideDetector.isOutside(mc.level, mc.player)) {
                int color = profile.fog().color();
                targetR = (float) (color >> 16 & 255) / 255.0F;
                targetG = (float) (color >> 8 & 255) / 255.0F;
                targetB = (float) (color & 255) / 255.0F;
            }

            if (!initialized) {
                curR = targetR;
                curG = targetG;
                curB = targetB;
                initialized = true;
            }

            curR += (targetR - curR) * FOG_CHANGE_SPEED;
            curG += (targetG - curG) * FOG_CHANGE_SPEED;
            curB += (targetB - curB) * FOG_CHANGE_SPEED;

            event.setRed(curR);
            event.setGreen(curG);
            event.setBlue(curB);
        } else {
            if (mc.level.isThundering() && OutsideDetector.isOutside(mc.level, mc.player)) {
                targetR = (float) (biome.get().getFogColor() >> 16 & 255) / 255.0F;
                targetG = (float) (biome.get().getFogColor() >> 8 & 255) / 255.0F;
                targetB = (float) (biome.get().getFogColor() & 255) / 255.0F;
            }

            if (!initialized) {
                curR = targetR;
                curG = targetG;
                curB = targetB;
                initialized = true;
            }

            curR += (targetR - curR) * FOG_CHANGE_SPEED;
            curG += (targetG - curG) * FOG_CHANGE_SPEED;
            curB += (targetB - curB) * FOG_CHANGE_SPEED;

            event.setRed(curR);
            event.setGreen(curG);
            event.setBlue(curB);
        }
    }

    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Holder<Biome> biome = mc.level.getBiome(event.getCamera().getBlockPosition());
        WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, mc.level);

        float vanillaStart = event.getNearPlaneDistance();
        float vanillaEnd = event.getFarPlaneDistance();

        float targetStart = vanillaStart;
        float targetEnd = vanillaEnd;
        boolean applyingWeather = false;

        if (profile.fog() != null) {
            if (mc.level.isThundering() && OutsideDetector.isOutside(mc.level, mc.player)) {
                float profileEnd = profile.fog().end();

                if (profileEnd < vanillaEnd) {
                    targetStart = profile.fog().start();
                    targetEnd = profileEnd;
                    applyingWeather = true;
                }
            }
        } else {
            if (mc.level.isThundering() && OutsideDetector.isOutside(mc.level, mc.player)) {
                if (64.0f < vanillaEnd) {
                    targetStart = 8.0f;
                    targetEnd = 64.0f;
                    applyingWeather = true;
                }
            }
        }

        curStart += (targetStart - curStart) * FOG_CHANGE_SPEED;
        curEnd += (targetEnd - curEnd) * FOG_CHANGE_SPEED;

        event.setNearPlaneDistance(curStart);
        event.setFarPlaneDistance(curEnd);


        if (applyingWeather || Math.abs(curEnd - vanillaEnd) > 0.1f) {
            event.setCanceled(true);
        }
    }
}