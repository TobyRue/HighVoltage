package io.github.tobyrue.high_voltage;

import io.github.tobyrue.high_voltage.data.MyNetworkHandler;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.WeatherSyncPacket;
import io.github.tobyrue.high_voltage.data.effects.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = HighVoltage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {

            MinecraftServer server = event.getServer();
            if (server == null) return;

            if (server.getTickCount() % 20 == 0) {
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);

                if (overworld != null) {
                    float rain = overworld.getRainLevel(1.0F);
                    boolean thunder = overworld.isThundering();
                    WeatherSyncPacket packet = new WeatherSyncPacket(rain, thunder);
                    server.getPlayerList().getPlayers().forEach(player -> {
                        MyNetworkHandler.CHANNEL.sendTo(
                                packet,
                                player.connection.connection,
                                NetworkDirection.PLAY_TO_CLIENT
                        );
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.level instanceof ServerLevel nether) {
            if (nether.dimension().equals(Level.NETHER)) {
                ServerLevel overworld = nether.getServer().getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    nether.setRainLevel(overworld.getRainLevel(1.0F));
                    nether.setThunderLevel(overworld.getThunderLevel(1.0F));
                }
            }
        }
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel world) {
            if (world.isThundering()) {
                for (ServerPlayer player : world.players()) {
                    Holder<Biome> biome = world.getBiome(player.blockPosition());
                    WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);
                    BonusLightningEffect effect = WeatherProfileLoader.getEffect(profile, ModWeatherEffects.PLAYER_BONUS_LIGHTNING);
                    if (effect == null) continue;

                    if (world.random.nextInt(effect.chance()) == 0) {
                        int blockRadius = effect.chunk_radius() * 16;

                        int offsetX = world.random.nextInt(blockRadius * 2) - blockRadius;
                        int offsetZ = world.random.nextInt(blockRadius * 2) - blockRadius;

                        BlockPos strikePos = world.getHeightmapPos(
                                Heightmap.Types.MOTION_BLOCKING,
                                player.blockPosition().offset(offsetX, 0, offsetZ)
                        );

                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
                        if (bolt != null) {
                            bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
                            world.addFreshEntity(bolt);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level.isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        ServerLevel world = player.getLevel();
        BlockPos playerPos = player.blockPosition();
        // && WeatherSafetyHelper.isOutside(world, playerPos)
        if (world.isThundering()) {
            var biome = world.getBiome(playerPos);
            var profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);

            int r = 16;
            BlockPos targetPos = null;
            int attempts = 0;

            while (attempts < 10) {
                BlockPos potentialPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                        playerPos.offset(world.random.nextInt(r*2)-r, 0, world.random.nextInt(r*2)-r));

                if (world.getBiome(potentialPos).equals(biome)) {
                    targetPos = potentialPos;
                    break;
                }
                attempts++;
            }
            if (targetPos == null) targetPos = playerPos;

            for (WeatherProfile.WeatherEffect action : profile.effects()) {
                runWeatherEffect(world, player, targetPos, action);
            }
        }
    }

    private static void runWeatherEffect(ServerLevel world, ServerPlayer player, BlockPos targetPos, WeatherProfile.WeatherEffect effect) {

        if (effect instanceof HungerEffect hunger) {
            if (world.random.nextInt(hunger.chance()) == 0) {
                player.getFoodData().addExhaustion(hunger.exhaustion());
            }
        }

        else if (effect instanceof DamageEffect dmg) {
            if (world.random.nextInt(dmg.chance()) == 0) {
                player.hurt(DamageSource.GENERIC, dmg.damage());
            }
        }

        else if (effect instanceof RingBellEffect bell) {
            if (world.random.nextInt(bell.chance()) == 0) {
                world.playSound(null, targetPos,
                        net.minecraft.sounds.SoundEvents.BELL_BLOCK,
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        bell.volume(), bell.pitch());
            }
        }

        else if (effect instanceof SummonEntityEffect summon) {
            if (world.random.nextInt(summon.chance()) == 0) {
                var entity = summon.entity().create(world);
                if (entity != null) {
                    summon.data().ifPresent(entity::load);
                    entity.moveTo(Vec3.atBottomCenterOf(targetPos));
                    world.addFreshEntity(entity);
                }
            }
        }

        else if (effect instanceof DamageArmorEffect armor) {
            if (world.random.nextInt(armor.chance()) == 0) {
                for (EquipmentSlot slot : armor.slots()) {
                    var stack = player.getItemBySlot(slot);
                    if (!stack.isEmpty() && stack.isDamageableItem()) {
                        stack.hurtAndBreak(armor.damage(), player, (p) -> p.broadcastBreakEvent(slot));
                    }
                }
            }
        }

        else if (effect instanceof CommandEffect cmd) {
            if (world.random.nextInt(cmd.chance()) == 0) {
                world.getServer().getCommands().performPrefixedCommand(
                        player.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                        cmd.command()
                );
            }
        }

        else if (effect instanceof FreezeEffect freeze) {
            if (player.getTicksFrozen() < freeze.freeze_ticks()) {
                player.setTicksFrozen(player.getTicksFrozen() + 5);
            }
        }
//        else if (effect instanceof WetEntitiesEffect) {
//            if (player.isOnFire()) {
//                player.clearFire();
//            }
//
//            if (world.getGameTime() % 20 == 0) {
//                if (player.getType() == EntityType.ENDERMAN || player.getType() == EntityType.BLAZE) {
//                    player.hurt(DamageSource.DROWN, 1.0F);
//                }
//            }
//
//            int radius = 16;
//            world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius)).forEach(entity -> {
//                if (WeatherSafetyHelper.isOutside(world, entity.blockPosition())) {
//
//                    if (entity.isOnFire()) {
//                        entity.clearFire();
//                    }
//
//                    if (world.getGameTime() % 20 == 0) {
//                        if (entity.getType() == EntityType.ENDERMAN || entity.getType() == EntityType.BLAZE) {
//                            entity.hurt(DamageSource.DROWN, 1.0F);
//                        }
//                    }
//
//                    if (entity instanceof WaterAnimal || entity instanceof Axolotl) {
//                        entity.setAirSupply(entity.getMaxAirSupply());
//                    }
//                }
//            });
//        } else if (effect instanceof ExtinguishBlocksEffect extinguish) {
//            BlockState state = world.getBlockState(targetPos);
//
//            if (state.is(Blocks.FIRE)) {
//                BlockPos below = targetPos.below();
//                boolean isNetherrack = world.getBlockState(below).is(Blocks.NETHERRACK);
//
//                if (extinguish.netherrack() || !isNetherrack) {
//                    world.removeBlock(targetPos, false);
//                    world.levelEvent(null, 1009, targetPos, 0);
//                }
//            }
//        }
    }

    private static boolean checkWhitelist(LivingEntity entity, String key) {
        if (key.equals("player")) return entity instanceof Player;
        if (key.equals("boat")) return entity.getVehicle() instanceof Boat;
        if (key.equals("mob")) return !(entity instanceof Player);
        if (key.startsWith("#")) return entity.getType().is(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, ResourceLocation.parse(key.substring(1))));
        return false;
    }

    private static void applyEffect(ServerLevel world, ServerPlayer player, BlockPos pos, String effect) {
        if (effect.equals("freeze")) {
            if (player.getTicksFrozen() < 140) player.setTicksFrozen(player.getTicksFrozen() + 3);
        }

        if (effect.startsWith("layer_block[")) {
            String blockId = effect.substring(12, effect.length() - 1);
            if (world.random.nextDouble() < 0.1) {
                handleLayerLogic(world, pos, blockId);
            }
        }

        if (effect.startsWith("damage_armor[")) {
            String slotName = effect.substring(13, effect.length() - 1);
            EquipmentSlot slot = EquipmentSlot.byName(slotName);
            if (world.random.nextInt(100) == 0) {
                player.getItemBySlot(slot).hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(slot));
            }
        }

        if (effect.startsWith("exhaust[")) {
            float amount = Float.parseFloat(effect.substring(8, effect.length() - 1));
            player.getFoodData().addExhaustion(amount);
        }
    }

    private static void handleLayerLogic(ServerLevel world, BlockPos pos, String blockId) {
        BlockPos targetPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                pos.offset(world.random.nextInt(11) - 5, 0, world.random.nextInt(11) - 5));

        BlockState state = world.getBlockState(targetPos);
        if (state.getBlock() instanceof SnowLayerBlock) {
            int layers = state.getValue(SnowLayerBlock.LAYERS);
            if (layers < 8) world.setBlockAndUpdate(targetPos, state.setValue(SnowLayerBlock.LAYERS, layers + 1));
        } else if (world.isEmptyBlock(targetPos)) {
            world.setBlockAndUpdate(targetPos, Blocks.SNOW.defaultBlockState());
        }
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (ServerConfig.PREVENT_SLEEP_THUNDER.get() && event.getEntity().level.isThundering()) {
            System.out.println("STOP SLEEP");
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            event.getEntity().displayClientMessage(Component.literal("The storm is too loud to sleep "), true);
        }
    }

}
