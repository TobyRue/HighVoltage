package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.LayerEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class LayerTickMixin {

    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void high_voltage$globalLayerTick(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel world = (ServerLevel) (Object) this;

        if (!world.isThundering()) return;

        int x = chunk.getPos().getMinBlockX() + world.random.nextInt(16);
        int z = chunk.getPos().getMinBlockZ() + world.random.nextInt(16);

        BlockPos scanPos = new BlockPos(x, 0, z);
        BlockPos finalPos = null;

        if (world.dimensionType().hasCeiling()) {
            java.util.List<BlockPos> validFloors = new java.util.ArrayList<>();

            for (int y = world.getMinBuildHeight() + 1; y < 120; y++) {
                BlockPos checkPos = new BlockPos(x, y, z);
                BlockState current = world.getBlockState(checkPos);

                if ((current.isAir() || current.getMaterial().isReplaceable()) &&
                        world.getBlockState(checkPos.below()).isSolidRender(world, checkPos.below())) {
                    validFloors.add(checkPos);
                }
            }

            if (!validFloors.isEmpty()) {
                finalPos = validFloors.get(world.random.nextInt(validFloors.size()));
            }
        } else {
            finalPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, scanPos);
        }

        if (finalPos == null) return;

        Holder<Biome> biome = world.getBiome(finalPos);
        WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);
        if (profile == null) return;

        for (WeatherProfile.WeatherEffect effect : profile.effects()) {
            if (effect instanceof LayerEffect layerEffect) {
                if (world.random.nextInt(layerEffect.chance()) == 0) {
                    high_voltage$processLayering(world, finalPos, layerEffect);
                }
            }
        }
    }

    @Unique
    private void high_voltage$processLayering(ServerLevel world, BlockPos topPos, LayerEffect layerEffect) {
        Block block = ForgeRegistries.BLOCKS.getValue(layerEffect.blockId());
        if (block == null) return;

        BlockState state = world.getBlockState(topPos);

        int localMax = layerEffect.maxLevel();
        if (layerEffect.noisy()) {
            long seed = (long) topPos.getX() * 3121L ^ (long) topPos.getZ() * 45238971L;
            localMax = (int) (Math.abs(seed) % (layerEffect.maxLevel())) + 1;
        }

        if (state.is(block)) {
            IntegerProperty prop = (IntegerProperty) state.getBlock().getStateDefinition().getProperty(layerEffect.propertyName());
            if (prop != null) {
                int currentVal = state.getValue(prop);
                if (currentVal < localMax) {
                    world.setBlock(topPos, state.setValue(prop, currentVal + 1), 3);
                }
            }
        } else {
            if (state.isAir() && world.getBlockState(topPos.below()).isSolidRender(world, topPos.below())) {
                world.setBlock(topPos, block.defaultBlockState(), 3);
            }
        }
    }
}