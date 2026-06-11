package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ServerLevel.class)
public class ChunkTickMixin {

    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void high_voltage$globalLayerTick(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel world = (ServerLevel) (Object) this;


        if (!world.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD).isThundering()) return;

        int x = chunk.getPos().getMinBlockX() + world.random.nextInt(16);
        int z = chunk.getPos().getMinBlockZ() + world.random.nextInt(16);

        BlockPos biomeCheckPos = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
        Holder<Biome> biome = world.getBiome(biomeCheckPos);
        WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);

        if (profile == null) return;

        for (WeatherProfile.WeatherEffect effect : profile.effects()) {
            if (effect instanceof LayerEffect layerEffect) {
                if (world.random.nextInt(layerEffect.chance()) == 0) {
                    high_voltage$handleLayeringExecution(world, x, z, layerEffect);
                }
            } else if (effect instanceof RingBellEffect bellEffect) {
                List<BlockPos> validBells = high_voltage$scanChunkForBlocks(world, chunk, false, state ->
                        state.getBlock() instanceof net.minecraft.world.level.block.BellBlock
                );
                if (!validBells.isEmpty() && world.random.nextInt(bellEffect.chance()) == 0) {
                    BlockPos chosenBell = validBells.get(world.random.nextInt(validBells.size()));
                    high_voltage$processBellRing(world, chosenBell, bellEffect);
                }
            } else if (effect instanceof IgniteEffect igniteEffect) {
                if (world.random.nextInt(igniteEffect.chance()) == 0) {
                    high_voltage$processIgnite(world, chunk, igniteEffect);
                }
            } else if (effect instanceof DamageEffect damageEffect) {
                if (world.random.nextInt(damageEffect.chance()) == 0) {
                    high_voltage$processDamage(world, chunk, damageEffect);
                }
            } else if (effect instanceof VelocityEffect velocityEffect) {
                if (world.random.nextInt(velocityEffect.chance()) == 0) {
                    high_voltage$processVelocity(world, chunk, velocityEffect);
                }
            } else if (effect instanceof StatusEffectEffect statusEffect) {
                if (world.random.nextInt(statusEffect.chance()) == 0) {
                    high_voltage$processStatusEffect(world, chunk, statusEffect);
                }
            } else if (effect instanceof FillCauldronEffect fillEffect) {
                List<BlockPos> validCauldrons = high_voltage$scanChunkForBlocks(world, chunk, fillEffect.surface_only(), state ->
                        state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON) ||
                                state.is(Blocks.LAVA_CAULDRON) || state.is(Blocks.POWDER_SNOW_CAULDRON)
                );
                if (!validCauldrons.isEmpty() && world.random.nextInt(fillEffect.chance()) == 0) {
                    BlockPos chosenCauldron = validCauldrons.get(world.random.nextInt(validCauldrons.size()));
                    high_voltage$processCauldronFill(world, chosenCauldron, fillEffect.fluid());
                }
            }
        }
    }


//        @Unique
//        private Optional<BlockPos> high_voltage$(ServerLevel world, LevelChunk chunk, boolean surfaceOnly, java.util.function.Predicate<BlockState> stateFilter) {
//
//        }
    




    /**
     * Scans a chunk's columns to locate matching block states based on a dynamic filter predicate.
     */
    @Unique
    private List<BlockPos> high_voltage$scanChunkForBlocks(ServerLevel world, LevelChunk chunk, boolean surfaceOnly, java.util.function.Predicate<BlockState> stateFilter) {
        List<BlockPos> list = new ArrayList<>();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();

        if (surfaceOnly) {
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(minX + dx, 0, minZ + dz));
                    BlockPos targetPos = topPos.below();
                    if (stateFilter.test(world.getBlockState(targetPos))) {
                        list.add(targetPos);
                    }
                }
            }
            return list;
        }

        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];

            if (section == null || section.hasOnlyAir()) continue;

            int bottomY = chunk.getMinBuildHeight() + (sectionIndex * 16);

            if (world.dimensionType().hasCeiling() && bottomY > 120) break;

            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    for (int dy = 0; dy < 16; dy++) {
                        BlockState state = section.getBlockState(dx, dy, dz);
                        if (stateFilter.test(state)) {
                            list.add(new BlockPos(minX + dx, bottomY + dy, minZ + dz));
                        }
                    }
                }
            }
        }
        return list;
    }

    @Unique
    private void high_voltage$processCauldronFill(ServerLevel world, BlockPos pos, String fluid) {
        BlockState state = world.getBlockState(pos);
        String fluidType = fluid.toLowerCase();

        switch (fluidType) {
            case "water" -> high_voltage$fillLayeredCauldron(world, pos, state, Blocks.WATER_CAULDRON.defaultBlockState());
            case "snow" -> high_voltage$fillLayeredCauldron(world, pos, state, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
            case "lava" -> {
                if (state.is(Blocks.CAULDRON)) {
                    world.setBlockAndUpdate(pos, Blocks.LAVA_CAULDRON.defaultBlockState());
                }
            }
        }
    }

    @Unique
    private void high_voltage$fillLayeredCauldron(ServerLevel world, BlockPos pos, BlockState currentState, BlockState filledTargetState) {
        if (currentState.is(Blocks.CAULDRON)) {
            world.setBlockAndUpdate(pos, filledTargetState.setValue(LayeredCauldronBlock.LEVEL, 1));
        } else if (currentState.is(filledTargetState.getBlock())) {
            int currentLevel = currentState.getValue(LayeredCauldronBlock.LEVEL);
            if (currentLevel < 3) {
                world.setBlockAndUpdate(pos, currentState.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1));
            }
        }
    }


    @Unique
    private void high_voltage$processStatusEffect(ServerLevel world, LevelChunk chunk, StatusEffectEffect effect) {
        MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(effect.effect());
        if (mobEffect == null) return;

        double minX = chunk.getPos().getMinBlockX();
        double minZ = chunk.getPos().getMinBlockZ();
        net.minecraft.world.phys.AABB chunkBounds = new net.minecraft.world.phys.AABB(
                minX, world.getMinBuildHeight(), minZ,
                minX + 16, world.getMaxBuildHeight(), minZ + 16
        );

        world.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, chunkBounds).forEach(entity -> {
            boolean matchesEntity = effect.entity_predicate().isEmpty() ||
                    effect.entity_predicate().get().contains(entity.getType().builtInRegistryHolder());

            if (matchesEntity) {
                MobEffectInstance instance = new MobEffectInstance(
                        mobEffect,
                        effect.duration(),
                        effect.amplifier(),
                        effect.ambient(),
                        effect.visible()
                );

                entity.addEffect(instance);
            }
        });
    }
    @Unique
    private void high_voltage$processVelocity(ServerLevel world, LevelChunk chunk, VelocityEffect effect) {
        double minX = chunk.getPos().getMinBlockX();
        double minZ = chunk.getPos().getMinBlockZ();
        net.minecraft.world.phys.AABB chunkBounds = new net.minecraft.world.phys.AABB(
                minX, world.getMinBuildHeight(), minZ,
                minX + 16, world.getMaxBuildHeight(), minZ + 16
        );

        world.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, chunkBounds).forEach(entity -> {
            boolean matchesEntity = effect.entity_predicate().isEmpty() ||
                    effect.entity_predicate().get().contains(entity.getType().builtInRegistryHolder());

            if (matchesEntity) {
                Vec3 chosenVelocity = effect.getRandomVelocity(world.random);

                entity.setDeltaMovement(entity.getDeltaMovement().add(chosenVelocity));

                entity.hurtMarked = true;
            }
        });
    }
    @Unique
    private void high_voltage$processIgnite(ServerLevel world, LevelChunk chunk, IgniteEffect effect) {
        double minX = chunk.getPos().getMinBlockX();
        double minZ = chunk.getPos().getMinBlockZ();
        net.minecraft.world.phys.AABB chunkBounds = new net.minecraft.world.phys.AABB(
                minX, world.getMinBuildHeight(), minZ,
                minX + 16, world.getMaxBuildHeight(), minZ + 16
        );

        world.getEntitiesOfClass(LivingEntity.class, chunkBounds).forEach(entity -> {
            if (effect.entity_predicate().contains(entity.getType().builtInRegistryHolder())) {
                if (!entity.fireImmune()) {
                    entity.setSecondsOnFire(effect.duration() / 20);
                }
            }
        });
    }

    @Unique
    private void high_voltage$processDamage(ServerLevel world, LevelChunk chunk, DamageEffect effect) {
        double minX = chunk.getPos().getMinBlockX();
        double minZ = chunk.getPos().getMinBlockZ();
        net.minecraft.world.phys.AABB chunkBounds = new net.minecraft.world.phys.AABB(
                minX, world.getMinBuildHeight(), minZ,
                minX + 16, world.getMaxBuildHeight(), minZ + 16
        );

        world.getEntitiesOfClass(Entity.class, chunkBounds).forEach(entity -> {
            if (effect.entity_predicate().contains(entity.getType().builtInRegistryHolder())) {
                entity.hurt(DamageSource.GENERIC, effect.damage());
            }
        });
    }

    @Unique
    private void high_voltage$processBellRing(ServerLevel world, BlockPos centerPos, RingBellEffect effect) {
        int r = (int) effect.radius();

        for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-r, -r, -r), centerPos.offset(r, r, r))) {
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof net.minecraft.world.level.block.BellBlock bell) {
                bell.attemptToRing(world, pos, net.minecraft.core.Direction.DOWN);
            }
        }
    }

    @Unique
    private void high_voltage$handleLayeringExecution(ServerLevel world, int x, int z, LayerEffect layerEffect) {
        if (layerEffect.surface_only()) {
            BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
            high_voltage$processLayering(world, topPos, layerEffect);
        } else {
            int maxHeight = world.dimensionType().hasCeiling() ? 120 : world.getMaxBuildHeight();

            for (int y = world.getMinBuildHeight() + 1; y < maxHeight; y++) {
                BlockPos checkPos = new BlockPos(x, y, z);
                BlockState current = world.getBlockState(checkPos);

                if ((current.isAir() || current.getMaterial().isReplaceable()) &&
                        world.getBlockState(checkPos.below()).isSolidRender(world, checkPos.below())) {

                    high_voltage$processLayering(world, checkPos, layerEffect);
                }
            }
        }
    }

    @Unique
    private void high_voltage$processLayering(ServerLevel world, BlockPos pos, LayerEffect layerEffect) {
        Block block = ForgeRegistries.BLOCKS.getValue(layerEffect.block());
        if (block == null) return;

        BlockState state = world.getBlockState(pos);

        int localMax = layerEffect.max_level();
        if (layerEffect.noisy()) {
            long seed = (long) pos.getX() * 3121L ^ (long) pos.getZ() * 45238971L;
            localMax = (int) (Math.abs(seed) % (layerEffect.max_level())) + 1;
        }

        if (state.is(block)) {
            IntegerProperty prop = (IntegerProperty) state.getBlock().getStateDefinition().getProperty(layerEffect.property());
            if (prop != null) {
                int currentVal = state.getValue(prop);
                if (currentVal < localMax) {
                    world.setBlock(pos, state.setValue(prop, currentVal + 1), 3);
                }
            }
        }
        else if (state.isAir() && world.getBlockState(pos.below()).isSolidRender(world, pos.below())) {
            world.setBlock(pos, block.defaultBlockState(), 3);
        }
    }
}