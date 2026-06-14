package io.github.tobyrue.high_voltage.data;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface IRandomTickWeatherEffect {
    default int getChance() {
        return 1;
    }
    void execute(ServerLevel world, ServerPlayer player, BlockPos targetPos, boolean isOutside);
}
