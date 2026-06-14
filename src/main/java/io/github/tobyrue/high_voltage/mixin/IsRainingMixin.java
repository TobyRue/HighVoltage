package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class IsRainingMixin {

    @Inject(method = "isThundering", at = @At("HEAD"), cancellable = true)
    private void high_voltage$allowDimensionWeather(CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;

        if (level.dimension().equals(Level.NETHER) || level.dimension().equals(Level.END)) {
            cir.setReturnValue(level.getThunderLevel(1.0F) > 0.9D);
        }
    }

    @Inject(method = "isRainingAt", at = @At("RETURN"), cancellable = true)
    private void high_voltage$overrideRainLogic(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;

        if (!cir.getReturnValueZ() && level.isThundering()) {

            Holder<Biome> biomeHolder = level.getBiome(pos);
            WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biomeHolder, level);

            if (profile != null && profile.precipitation() != null && profile.precipitation().acts_like_rain()) {
                cir.setReturnValue(true);
            }
        }
    }
}
