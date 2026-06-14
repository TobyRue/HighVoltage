package io.github.tobyrue.high_voltage;

import io.github.tobyrue.high_voltage.data.*;
import io.github.tobyrue.high_voltage.data.effects.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

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
        if (event.phase == TickEvent.Phase.START && event.level instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension().equals(Level.NETHER) || serverLevel.dimension().equals(Level.END)) {
                ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    serverLevel.setRainLevel(overworld.getRainLevel(1.0F));
                    serverLevel.setThunderLevel(overworld.getThunderLevel(1.0F));
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
                        int blockRadius = effect.radius() * 16;

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
        boolean isOutside = OutsideDetector.isOutside(world, player);

        if (world.isThundering()) {
            var biome = world.getBiome(playerPos);
            var profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, world);

            java.util.Set<UUID> currentProfileModifierIds = new java.util.HashSet<>();
            for (WeatherProfile.WeatherEffect action : profile.effects()) {
                if (action instanceof ModifyAttributeEffect effect) {
                    currentProfileModifierIds.add(effect.getModifierId());
                }
            }

            high_voltage$clearForeignStormAttributes(player, currentProfileModifierIds);

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


            if (targetPos.getY() <= world.getMinBuildHeight() || world.getBlockState(targetPos.below()).isAir()) {
                int randomYOffset = world.random.nextInt(14) - 4;
                targetPos = new BlockPos(targetPos.getX(), playerPos.getY() + randomYOffset, targetPos.getZ());
            }

            boolean isInCorrectBiome = world.getBiome(playerPos).equals(biome);
            for (WeatherProfile.WeatherEffect action : profile.effects()) {
                if (action instanceof ModifyAttributeEffect effect) {
                    Attribute attribute = effect.getAttribute();
                    if (attribute == null) continue;

                    AttributeInstance instance = player.getAttributes().getInstance(attribute);
                    if (instance == null) continue;

                    UUID modifierId = effect.getModifierId();
                    AttributeModifier modifier = effect.createModifier();

                    if (isInCorrectBiome) {
                        if (instance.getModifier(modifierId) == null) {
                            int roll = world.random.nextInt(effect.chance());

                            if (roll == 0) {
                                instance.addPermanentModifier(modifier);

                                if (effect.attribute().equals("minecraft:generic.max_health")) {
                                    player.setHealth(player.getHealth());
                                } else if (effect.attribute().equals("minecraft:generic.movement_speed")) {
                                    player.onUpdateAbilities();
                                }
                            }
                        }
                    }
                } else if (action instanceof IRandomTickWeatherEffect randomEffect && world.random.nextInt(randomEffect.getChance()) == 0) {
                    randomEffect.execute(world, player, targetPos, isOutside);
                }
            }
        } else {
            high_voltage$clearAllStormAttributes(player);
        }
    }

    private static void high_voltage$clearForeignStormAttributes(ServerPlayer player, java.util.Set<UUID> allowedIds) {
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES) {
            AttributeInstance attributeInstance = player.getAttributes().getInstance(attribute);
            if (attributeInstance == null) continue;

            java.util.Set<AttributeModifier> modifiers = new java.util.HashSet<>(attributeInstance.getModifiers());
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getName().startsWith("[High Voltage Weather] ")) {
                    if (!allowedIds.contains(modifier.getId())) {
                        attributeInstance.removeModifier(modifier);

                        if (attribute == net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) {
                            player.setHealth(player.getHealth());
                        } else if (attribute == net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) {
                            player.onUpdateAbilities();
                        }
                    }
                }
            }
        }
    }
    private static void high_voltage$clearAllStormAttributes(ServerPlayer player) {
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES) {
            AttributeInstance attributeInstance = player.getAttributes().getInstance(attribute);
            if (attributeInstance == null) continue;

            java.util.Set<AttributeModifier> modifiers = new java.util.HashSet<>(attributeInstance.getModifiers());
            for (AttributeModifier modifier : modifiers) {
                if (modifier.getName().startsWith("[High Voltage Weather] ")) {
                    attributeInstance.removeModifier(modifier);

                    if (attribute == net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) {
                        player.setHealth(player.getHealth());
                    } else if (attribute == net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED) {
                        player.onUpdateAbilities();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (Config.PREVENT_SLEEP_THUNDER.get() && event.getEntity().level.isThundering()) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            event.getEntity().displayClientMessage(Component.translatable("weather.high_voltage.to_loud_to_sleep"), true);
        }
    }

}
