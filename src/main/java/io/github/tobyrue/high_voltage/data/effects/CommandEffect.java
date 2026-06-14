package io.github.tobyrue.high_voltage.data.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tobyrue.high_voltage.data.IRandomTickWeatherEffect;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record CommandEffect(String run, int chance) implements WeatherProfile.WeatherEffect, IRandomTickWeatherEffect {
    public static final Codec<CommandEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("run").forGetter(CommandEffect::run),
            Codec.INT.optionalFieldOf("chance", 1).forGetter(CommandEffect::chance)
    ).apply(instance, CommandEffect::new));


    @Override
    public WeatherProfile.WeatherEffectType<?> getType() {
        return ModWeatherEffects.COMMAND.get();
    }

    @Override
    public int getChance() {
        return this.chance();
    }

    @Override
    public void execute(ServerLevel world, ServerPlayer player, BlockPos targetPos, boolean isOutside) {
        world.getServer().getCommands().performPrefixedCommand(
                player.createCommandSourceStack().withPermission(4).withSuppressedOutput().withPosition(Vec3.atBottomCenterOf(targetPos)),
                this.run()
        );
    }
}
