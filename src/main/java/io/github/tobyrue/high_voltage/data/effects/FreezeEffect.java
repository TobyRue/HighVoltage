package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.IRandomTickWeatherEffect;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LightLayer;

public record FreezeEffect(int freeze_ticks) implements WeatherProfile.WeatherEffect, IRandomTickWeatherEffect {
    public static final Codec<FreezeEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("freeze_ticks").forGetter(FreezeEffect::freeze_ticks)
    ).apply(instance, FreezeEffect::new));

    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.FREEZE.get();
    }

    @Override
    public void execute(ServerLevel world, ServerPlayer player, BlockPos targetPos, boolean isOutside) {
        if (isOutside) {
            if (player.getTicksFrozen() < this.freeze_ticks()) {
                if (!(player.getLevel().getBrightness(LightLayer.BLOCK, player.getOnPos().above()) > 11)) {
                    player.setTicksFrozen(player.getTicksFrozen() + 5);
                }
            } else if (this.freeze_ticks() == player.getTicksFrozen()) {
                if (!(player.getLevel().getBrightness(LightLayer.BLOCK, player.getOnPos().above()) > 11)) {
                    player.setTicksFrozen(this.freeze_ticks());
                }
            }
        }
    }
}
