package io.github.tobyrue.high_voltage.mixin;

import com.google.common.collect.Lists;
import io.github.tobyrue.high_voltage.ServerConfig;
import io.github.tobyrue.high_voltage.WeatherManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.List;


@Mixin(ServerLevel.class)
public abstract class LightningSpawnMixin {
    @Shadow @Final
    List<ServerPlayer> players = Lists.newArrayList();

    @Shadow
    public abstract ServerLevel getLevel();

    @ModifyConstant(method = "tickChunk", constant = @Constant(intValue = 100000))
    private int high_voltage$increaseLightningProbability(int constant) {
        for (ServerPlayer player : this.players) {
            BlockPos pos = player.blockPosition();
            var biome = getLevel().getBiome(pos);
            return WeatherManager.ServerWeatherManager.getCurrentServerProfile(biome).lightningChance();
        }
        return constant;
    }
}
