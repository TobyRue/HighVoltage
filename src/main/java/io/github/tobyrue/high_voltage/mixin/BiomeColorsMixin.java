package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {

//    @Inject(method = "getAverageColor", at = @At("RETURN"), cancellable = true)
//    private static void high_voltage$overrideHardcodedColors(BlockAndTintGetter level, BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir) {
//        high_voltage$applyOverlay(pos, cir);
//    }
//
//    @Unique
//    private static void high_voltage$applyOverlay(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
//        Minecraft mc = Minecraft.getInstance();
//        ClientLevel world = mc.level;
//
//        if (world != null) {
//            Holder<Biome> biome = world.getBiome(pos);
//            WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);
//
//            if (profile != null && world.isThundering()) {
//                int profileColor = profile.foliageColor();
//                int alpha = (profileColor >> 24) & 0xFF;
//
//                if (alpha == 0) return;
//
//                int originalColor = cir.getReturnValue();
//
//                int origR = (originalColor >> 16) & 0xFF;
//                int origG = (originalColor >> 8) & 0xFF;
//                int origB = originalColor & 0xFF;
//
//                int overR = (profileColor >> 16) & 0xFF;
//                int overG = (profileColor >> 8) & 0xFF;
//                int overB = profileColor & 0xFF;
//
//                float alphaPct = alpha / 255.0f;
//                float invAlphaPct = 1.0f - alphaPct;
//
//                int finalR = Math.min(255, (int) ((origR * invAlphaPct) + (overR * alphaPct)));
//                int finalG = Math.min(255, (int) ((origG * invAlphaPct) + (overG * alphaPct)));
//                int finalB = Math.min(255, (int) ((origB * invAlphaPct) + (overB * alphaPct)));
//
//                int finalColor = (finalR << 16) | (finalG << 8) | finalB;
//                cir.setReturnValue(finalColor);
//            }
//        }
//    }
}