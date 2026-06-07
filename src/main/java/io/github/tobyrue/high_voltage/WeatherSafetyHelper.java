package io.github.tobyrue.high_voltage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class WeatherSafetyHelper {
    public static boolean isOutside(Level level, BlockPos pos) {
        if (level.canSeeSky(pos)) return true;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(pos);

        int depth = 0;
        while (!queue.isEmpty() && depth < 64) {
            BlockPos current = queue.poll();
            visited.add(current);
            depth++;

            if (level.canSeeSky(current)) return true;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor) && !level.getBlockState(neighbor).isCollisionShapeFullBlock(level, neighbor)) {
                    BlockState state = level.getBlockState(neighbor);
                    if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN)) {
                        queue.add(neighbor);
                    } else if (!(state.getBlock() instanceof DoorBlock)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
        return false;
    }
}