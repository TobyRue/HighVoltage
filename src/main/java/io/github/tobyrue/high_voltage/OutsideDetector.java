package io.github.tobyrue.high_voltage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

public class OutsideDetector {

    public static boolean isOutside(Level level, Player player) {
        return level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(player.getOnPos().above(1)) > 10;
    }

    public static boolean isInside(Level level, Player player) {
        return !isOutside(level, player);
    }
}