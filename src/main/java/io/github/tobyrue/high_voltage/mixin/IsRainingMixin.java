package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.ModWeatherEffects;
import io.github.tobyrue.high_voltage.data.effects.WetEntitiesEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class IsRainingMixin {

    @Inject(method = "isRainingAt", at = @At("HEAD"), cancellable = true)
    private void high_voltage$rainInCustomWeather(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;

        if (level instanceof ServerLevel world && world.isThundering()) {
            Holder<Biome> biome = world.getBiome(pos);
            WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome);
            boolean isWet = profile.effects().stream().anyMatch(e -> e instanceof WetEntitiesEffect)
                    || (profile.precipitation() != null && profile.precipitation().does_rain());

            if (isWet) {
                cir.setReturnValue(true);
            }
        }
    }
}
