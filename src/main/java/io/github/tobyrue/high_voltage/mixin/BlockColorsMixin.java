package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

//    @Inject(
//            method = "getColor(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;I)I",
//            at = @At("RETURN"),
//            cancellable = true
//    )
//    private void high_voltage$overrideFoliageColorsEveryFrame(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> cir) {
//        if (pos == null || tintIndex < 0) return;
//
//        Minecraft mc = Minecraft.getInstance();
//        ClientLevel world = mc.level;
//
//        if (world != null && world.isThundering()) {
//            Holder<Biome> biome = world.getBiome(pos);
//            WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);
//
//            if (profile != null) {
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