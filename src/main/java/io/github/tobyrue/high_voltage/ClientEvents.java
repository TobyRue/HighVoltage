package io.github.tobyrue.high_voltage;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
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
        if (mc.level == null) return;

        Holder<Biome> biome = mc.level.getBiome(event.getCamera().getBlockPosition());
        WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, mc.level);

        float targetR = event.getRed();
        float targetG = event.getGreen();
        float targetB = event.getBlue();

        if (profile.fog() != null) {
            if (mc.level.isThundering() && WeatherSafetyHelper.isOutside(mc.level, event.getCamera().getBlockPosition())) {
                targetR = (float) (profile.fog().color() >> 16 & 255) / 255.0F;
                targetG = (float) (profile.fog().color() >> 8 & 255) / 255.0F;
                targetB = (float) (profile.fog().color() & 255) / 255.0F;
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
            if (mc.level.isThundering() && WeatherSafetyHelper.isOutside(mc.level, event.getCamera().getBlockPosition())) {
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
        if (mc.level == null) return;

        Holder<Biome> biome = mc.level.getBiome(event.getCamera().getBlockPosition());
        WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome,mc.level);

        float targetStart = event.getNearPlaneDistance();
        float targetEnd = event.getFarPlaneDistance();

        if (profile.fog() != null) {

            if (mc.level.isThundering() && WeatherSafetyHelper.isOutside(mc.level, event.getCamera().getBlockPosition())) {
                targetStart = profile.fog().start();
                targetEnd = profile.fog().end();
            }

            curStart += (targetStart - curStart) * FOG_CHANGE_SPEED;
            curEnd += (targetEnd - curEnd) * FOG_CHANGE_SPEED;

            event.setNearPlaneDistance(curStart);
            event.setFarPlaneDistance(curEnd);

            if (mc.level.isThundering() || Math.abs(curEnd - targetEnd) > 0.1f) {
                event.setCanceled(true);
            }
        } else {
            if (mc.level.isThundering() && WeatherSafetyHelper.isOutside(mc.level, event.getCamera().getBlockPosition())) {
                targetStart = 8;
                targetEnd = 64;
            }

            curStart += (targetStart - curStart) * FOG_CHANGE_SPEED;
            curEnd += (targetEnd - curEnd) * FOG_CHANGE_SPEED;

            event.setNearPlaneDistance(curStart);
            event.setFarPlaneDistance(curEnd);

            if (mc.level.isThundering() || Math.abs(curEnd - targetEnd) > 0.1f) {
                event.setCanceled(true);
            }
        }
    }
}
