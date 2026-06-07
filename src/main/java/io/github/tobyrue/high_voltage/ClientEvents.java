package io.github.tobyrue.high_voltage;

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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.level.isThundering()) {
            Level world = player.level;

            if (world.dimension() == Level.NETHER) {
                for(int i = 0; i < 5; i++) {
                    world.addParticle(ParticleTypes.FLAME,
                            player.getX() + world.random.nextGaussian() * 10,
                            player.getY() + world.random.nextGaussian() * 10,
                            player.getZ() + world.random.nextGaussian() * 10, 0, 0.1, 0);
                }
            }

            if (world.dimension() == Level.END && player.getY() < 0) {
                world.addParticle(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY(), player.getZ(), 0, 0, 0);
            }
        }
    }
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Holder<Biome> biome = mc.level.getBiome(event.getCamera().getBlockPosition());
        WeatherManager.WeatherProfile profile = WeatherManager.getCurrentProfile(biome);

        float targetR = event.getRed();
        float targetG = event.getGreen();
        float targetB = event.getBlue();

        if (mc.level.isThundering() && WeatherSafetyHelper.isOutside(mc.level, event.getCamera().getBlockPosition()) && profile != null) {
            targetR = (float) (profile.fogColor() >> 16 & 255) / 255.0F;
            targetG = (float) (profile.fogColor() >> 8 & 255) / 255.0F;
            targetB = (float) (profile.fogColor() & 255) / 255.0F;
        }

        if (!initialized) {
            curR = targetR; curG = targetG; curB = targetB;
            initialized = true;
        }

        curR += (targetR - curR) * FOG_CHANGE_SPEED;
        curG += (targetG - curG) * FOG_CHANGE_SPEED;
        curB += (targetB - curB) * FOG_CHANGE_SPEED;

        event.setRed(curR);
        event.setGreen(curG);
        event.setBlue(curB);
    }

    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Holder<Biome> biome = mc.level.getBiome(event.getCamera().getBlockPosition());
        WeatherManager.WeatherProfile profile = WeatherManager.getCurrentProfile(biome);

        float targetStart = event.getNearPlaneDistance();
        float targetEnd = event.getFarPlaneDistance();

        if (mc.level.isThundering() && WeatherSafetyHelper.isOutside(mc.level, event.getCamera().getBlockPosition()) && profile != null) {
            targetStart = profile.fogStart();
            targetEnd = profile.fogEnd();
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
