package io.github.tobyrue.high_voltage.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.WeatherManager;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelRenderer.class)
public class WeatherRendererMixin {
//    @Unique
//    private static final ResourceLocation SAND_STORM = ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/sand_storm.png");
//    @Unique
//    private static final ResourceLocation HEAVY_SNOW = ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/heavy_snow.png");
//    @Unique
//    private static final ResourceLocation HEAVY_RAIN = ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/heavy_rain.png");
//    @Unique
//    private static final ResourceLocation TROPICAL_RAIN = ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "textures/environment/tropical_rain.png");
//
//
//    @Shadow
//    @Final
//    private static ResourceLocation RAIN_LOCATION;
//    @Shadow
//    @Final private static ResourceLocation SNOW_LOCATION;


    @Shadow private int ticks;



    @Unique
    private WeatherProfile high_voltage$getEffectiveProfile() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.isThundering()) return null;

        BlockPos pos = mc.gameRenderer.getMainCamera().getBlockPosition();
        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        return WeatherProfileLoader.getProfileForBiomeWithFallback(biomeHolder);
    }

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;warmEnoughToRain(Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean high_voltage$textureAndColorSwap(Biome instance, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biomeHolder);

        if ((profile.precipitation() != null)) {
            ResourceLocation neededTexture = profile.precipitation().texture();

            if (high_voltage$currentBatchTexture != null && !neededTexture.equals(high_voltage$currentBatchTexture)) {
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();

                if (bufferbuilder.building()) {
                    tesselator.end();
                    RenderSystem.setShaderTexture(0, neededTexture);

                    int tint = (profile != null) ? profile.precipitation().tint() : 0xFFFFFF;
                    float r = ((tint >> 16) & 0xFF) / 255f;
                    float g = ((tint >> 8) & 0xFF) / 255f;
                    float b = (tint & 0xFF) / 255f;
                    RenderSystem.setShaderColor(r, g, b, 1.0F);

                    bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
                }
                high_voltage$currentBatchTexture = neededTexture;
            }
        }
        return instance.warmEnoughToRain(pos);
    }

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void high_voltage$resetBatch(LightTexture p_109704_, float p_109705_, double p_109706_, double p_109707_, double p_109708_, CallbackInfo ci) {
        this.high_voltage$currentBatchTexture = null;
    }

    @Unique
    private ResourceLocation high_voltage$currentBatchTexture = null;

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


//    @Redirect(
//            method = "renderSnowAndRain",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;warmEnoughToRain(Lnet/minecraft/core/BlockPos;)Z")
//    )


//    @Redirect(
//            method = "renderSnowAndRain",
//            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V")
//    )
//    private void high_voltage$changeWeatherTexture(int unit, ResourceLocation original) {
//        Minecraft mc = Minecraft.getInstance();
//        BlockPos pos = mc.gameRenderer.getMainCamera().getBlockPosition();
//
//        WeatherManager.WeatherProfile profile = high_voltage$getProfileAtPos(pos);
//
//        ResourceLocation textureToUse = original;
//        if (profile != null) {
//            textureToUse = profile.texture();
//        }
//
//        RenderSystem.setShaderTexture(unit, textureToUse);
//    }


    @Unique
    private WeatherProfile high_voltage$getProfileAtPos(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.isThundering()) return null;

        Holder<Biome> biomeHolder = mc.level.getBiome(pos);
        return WeatherProfileLoader.getProfileForBiomeWithFallback(biomeHolder);
    }


//    @Unique
//    private WeatherManager.WeatherProfile high_voltage$getProfile() {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.level != null) {
//            BlockPos pos = mc.gameRenderer.getMainCamera().getBlockPosition();
//            return WeatherManager.getCurrentProfile(mc.level.getBiome(pos));
//        }
//        return null;
//    }

    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;ticks:I")
    )

    private int high_voltage$speed(LevelRenderer instance) {
        WeatherProfile profile = high_voltage$getEffectiveProfile();
        return (profile != null) ? (int)(this.ticks * profile.precipitation().vy()) : this.ticks;
    }

    @ModifyVariable(
            method = "renderSnowAndRain",
            at = @At(value = "STORE"),
            ordinal = 2
    )

    private float high_voltage$vSpeed(float val) {
        WeatherProfile profile = high_voltage$getEffectiveProfile();
        return (profile != null) ? val * profile.precipitation().vy() : val;
    }

    @ModifyVariable(
            method = "renderSnowAndRain",
            at = @At(value = "STORE"),
            ordinal = 3
    )
    private float high_voltage$hWind(float val) {
        WeatherProfile profile = high_voltage$getEffectiveProfile();
        return (profile != null) ? val + profile.precipitation().vx() : val;
    }


    @Redirect(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitation()Lnet/minecraft/world/level/biome/Biome$Precipitation;")
    )
    private Biome.Precipitation high_voltage$forceDesertWeather(Biome instance) {
        Minecraft mc = Minecraft.getInstance();
        BlockPos pos = mc.gameRenderer.getMainCamera().getBlockPosition();

        if (high_voltage$getEffectiveProfile() != null && high_voltage$getEffectiveProfile().precipitation() != null) {
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