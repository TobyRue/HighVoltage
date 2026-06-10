package io.github.tobyrue.high_voltage.mixin;

import com.google.common.collect.Lists;
import io.github.tobyrue.high_voltage.ServerConfig;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;


@Mixin(ServerLevel.class)
public abstract class LightningSpawnMixin {
    @Shadow @Final
    List<ServerPlayer> players = Lists.newArrayList();

    @Shadow
    public abstract ServerLevel getLevel();

    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextInt(I)I",
                    ordinal = 0
            )
    )
    private int high_voltage$redirectLightningChance(RandomSource randomSource, int originalBound) {
        var me = (ServerLevel) (Object) this;

        for (ServerPlayer player : this.players) {
            BlockPos pos = player.blockPosition();
            var biome = getLevel().getBiome(pos);
            var profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, getLevel()).baseLightningChance();

            return profile <= 0 ? profile : me.random.nextInt(profile);
        }

        return randomSource.nextInt(originalBound);
    }
}
