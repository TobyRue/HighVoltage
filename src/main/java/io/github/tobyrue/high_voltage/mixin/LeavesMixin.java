package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.HighVoltage;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public class LeavesMixin {

    @Inject(
            at = {@At("TAIL")},
            method = "createBlockStateDefinition"
    )
    public void high_voltage$createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(HighVoltage.WEATHERED);
    }

    @Inject(
            at = {@At("TAIL")},
            method = "<init>"
    )
    private void high_voltage$init(BlockBehaviour.Properties properties, CallbackInfo ci) {
        BlockState defaultState = ((Block) (Object) this).defaultBlockState();
        if (defaultState.hasProperty(HighVoltage.WEATHERED)) {
            ((BlockStateInvoker) this).high_voltage$invokeSetDefaultState(defaultState.setValue(HighVoltage.WEATHERED, false));
        }
    }

    @Inject(
            at = {@At("RETURN")},
            method = "isRandomlyTicking",
            cancellable = true
    )
    public void high_voltage$isRandomlyTicking(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(
            at = {@At("HEAD")},
            method = "randomTick"
    )
    public void high_voltage$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        boolean targetWeatherState = level.isThundering();
        boolean currentState = state.getValue(HighVoltage.WEATHERED);

        if (currentState != targetWeatherState) {
            high_voltage$tryWeatherTransition(state, level, pos, random, targetWeatherState);
        }
    }

    @Inject(
            at = {@At("HEAD")},
            method = "tick"
    )
    public void high_voltage$scheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        boolean targetWeatherState = level.isThundering();
        boolean currentState = state.getValue(HighVoltage.WEATHERED);

        if (currentState != targetWeatherState) {
            high_voltage$tryWeatherTransition(state, level, pos, random, targetWeatherState);
        }
    }

    @Unique
    private void high_voltage$tryWeatherTransition(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, boolean targetState) {
        var biome = level.getBiome(pos);
        var profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, level);

        if (profile == null || profile.foliageColor() == 0x00FFFFFF) {
            return;
        }

        level.setBlock(pos, state.setValue(HighVoltage.WEATHERED, targetState), Block.UPDATE_ALL);

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.getBlock() instanceof LeavesBlock) {
                boolean neighborWeathered = neighborState.getValue(HighVoltage.WEATHERED);
                if (neighborWeathered != targetState) {
                    level.scheduleTick(neighborPos, neighborState.getBlock(), random.nextInt(10) + 10);
                }
            }
        }
    }
}