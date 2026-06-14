package io.github.tobyrue.high_voltage.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.WeatherSyncHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WeatherRendererMixin {
    @Shadow
    private ClientLevel level;

    @Shadow private int ticks;

    @Unique
    private WeatherProfile high_voltage$getEffectiveProfile() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !WeatherSyncHandler.globalIsThundering) return null;

        BlockPos pos = mc.gameRenderer.getMainCamera().getBlockPosition();
        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        return WeatherProfileLoader.getProfileForBiomeWithFallback(biomeHolder, mc.level);
    }

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;warmEnoughToRain(Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean high_voltage$textureColorAndMotionSwap(Biome instance, BlockPos pos) {
        this.high_voltage$currentProfile = high_voltage$getProfileAtPos(pos);

        if (this.high_voltage$currentProfile != null && this.high_voltage$currentProfile.precipitation() != null) {
            ResourceLocation neededTexture = this.high_voltage$currentProfile.precipitation().texture();

            if (high_voltage$currentBatchTexture != null && !neededTexture.equals(high_voltage$currentBatchTexture)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                if (bufferbuilder.building()) {
                    tesselator.end();
                    RenderSystem.setShaderTexture(0, neededTexture);

                    int tint = this.high_voltage$currentProfile.precipitation().tint();
                    float r = ((tint >> 16) & 0xFF) / 255f;
                    float g = ((tint >> 8) & 0xFF) / 255f;
                    float b = (tint & 0xFF) / 255f;
                    RenderSystem.setShaderColor(r, g, b, 1.0F);

                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                }
                high_voltage$currentBatchTexture = neededTexture;
            }
        }
        if (level != null && level.isThundering()) {
            return true;
        }
        return instance.warmEnoughToRain(pos);
    }

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void high_voltage$resetBatch(LightTexture p_109704_, float p_109705_, double p_109706_, double p_109707_, double p_109708_, CallbackInfo ci) {
        this.high_voltage$currentBatchTexture = null;
        this.high_voltage$currentProfile = null;
    }

    @Unique
    private ResourceLocation high_voltage$currentBatchTexture = null;
    @Unique
    private WeatherProfile high_voltage$currentProfile = null;

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V")
    )
    private void high_voltage$handleTextureSwap(int unit, ResourceLocation texture) {
        high_voltage$currentBatchTexture = texture;
        RenderSystem.setShaderTexture(unit, texture);
    }

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;create(J)Lnet/minecraft/util/RandomSource;")
    )
    private RandomSource high_voltage$switchBatchOnTheFly(long seed) {
        Minecraft mc = Minecraft.getInstance();

        return RandomSource.create(seed);
    }


    @Unique
    private WeatherProfile high_voltage$getProfileAtPos(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.isThundering()) return null;
        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        return WeatherProfileLoader.getProfileForBiomeWithFallback(biomeHolder, mc.level);
    }


    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I")
    )
    private int high_voltage$ignoreCeilingInDimensions(Level level, Heightmap.Types type, int x, int z) {
        if (level.dimension().equals(Level.NETHER) || level.dimension().equals(Level.END)) {
            return level.getMinBuildHeight();
        }

        return level.getHeight(type, x, z);
    }



    // 1. RAIN VERTICAL SPEED (f2)
    @ModifyVariable(
            method = "renderSnowAndRain",
            at = @At(value = "STORE"),
            ordinal = 2
    )

    private float high_voltage$rainVerticalSpeed(float val) {
        if (this.high_voltage$currentProfile != null && this.high_voltage$currentProfile.precipitation() != null) {
            return val * this.high_voltage$currentProfile.precipitation().vy();
        }
        return val;
    }

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;uv(FF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
            slice = @Slice(
                from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;warmEnoughToRain(Lnet/minecraft/core/BlockPos;)Z"),
                to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V", ordinal = 1)
            )
    )
    private VertexConsumer high_voltage$applyRainWind(VertexConsumer instance, float u, float v) {
        if (this.high_voltage$currentProfile != null && this.high_voltage$currentProfile.precipitation() != null) {
            float windX = (this.ticks * 0.02f) * this.high_voltage$currentProfile.precipitation().vx();
            return instance.uv(u + windX, v);
        }
        return instance.uv(u, v);
    }

    // 2. SNOW VERTICAL SPEED (f5)
    @ModifyVariable(
            method = "renderSnowAndRain",
            at = @At(value = "STORE"),
            ordinal = 3
    )
    private float high_voltage$snowVerticalSpeed(float val) {
        if (this.high_voltage$currentProfile != null && this.high_voltage$currentProfile.precipitation() != null) {
            return val * this.high_voltage$currentProfile.precipitation().vy();
        }
        return val;
    }

    // 3. SNOW HORIZONTAL DRIFT (f6)
    @ModifyVariable(
            method = "renderSnowAndRain",
            at = @At(value = "STORE"),
            ordinal = 4
    )
    private float high_voltage$snowHorizontalWind(float val) {
        if (this.high_voltage$currentProfile != null && this.high_voltage$currentProfile.precipitation() != null) {
            return val * this.high_voltage$currentProfile.precipitation().vx();
        }
        return val;
    }

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitation()Lnet/minecraft/world/level/biome/Biome$Precipitation;")
    )
    private Biome.Precipitation high_voltage$forceWeatherWithAlphaCheck(Biome instance) {
        Minecraft mc = Minecraft.getInstance();
        float rainLevel = WeatherSyncHandler.globalRainLevel;
        if (rainLevel > 0.0F && high_voltage$getEffectiveProfile() != null && WeatherSyncHandler.globalIsThundering) {
            return Biome.Precipitation.RAIN;
        }
        return instance.getPrecipitation();
    }


    @Redirect(
            method = "tickRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitation()Lnet/minecraft/world/level/biome/Biome$Precipitation;")
    )
    private Biome.Precipitation high_voltage$tickWeatherInDesert(Biome instance) {
        if (high_voltage$getEffectiveProfile() != null && high_voltage$getEffectiveProfile().precipitation() != null) {
            return Biome.Precipitation.RAIN;
        }
        return instance.getPrecipitation();
    }

    @Redirect(
            method = "tickRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V")
    )
    private void high_voltage$conditionalPlaySound(ClientLevel level, BlockPos pos, SoundEvent sound, SoundSource source, float vol, float pitch, boolean distanceDelay) {
        WeatherProfile profile = high_voltage$getEffectiveProfile();

        if (profile != null && !profile.precipitation().sound()) {
            return;
        }

        level.playLocalSound(pos, sound, source, vol, pitch, distanceDelay);
    }


    @Redirect(
            method = "tickRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    )
    private void high_voltage$localizedParticles(ClientLevel level, ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {
        BlockPos pos = new BlockPos(x, y, z);
        WeatherProfile profile = high_voltage$getProfileAtPos(pos);

        if (profile != null && profile.precipitation() != null && profile.precipitation().particle() != null) {
            try {
                ParticleType<?> type = profile.precipitation().particle();
                if (type instanceof ParticleOptions customOptions) {
                    level.addParticle(customOptions, x, y, z, vx, vy, vz);
                    return;
                }
            } catch (Exception ignored) {}
            level.addParticle(options, x, y, z, vx, vy, vz);
        }
    }
}