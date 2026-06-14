package io.github.tobyrue.high_voltage.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockStateInvoker {
    @Invoker("registerDefaultState")
    void high_voltage$invokeSetDefaultState(BlockState state);
}
