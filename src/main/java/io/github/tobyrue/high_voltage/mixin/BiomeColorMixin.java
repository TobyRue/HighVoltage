package io.github.tobyrue.high_voltage.mixin;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class BiomeColorMixin {
    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void makeItWhite(double x, double z, CallbackInfoReturnable<Integer> cir) {
        // if (snowing) cir.setReturnValue(0xFFFFFF);
    }
}