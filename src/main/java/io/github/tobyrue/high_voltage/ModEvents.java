package io.github.tobyrue.high_voltage;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = HighVoltage.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel world) {
            if (world.isThundering()) {
                for (ServerPlayer player : world.players()) {
                    Holder<Biome> biome = world.getBiome(player.blockPosition());
                    var profile = WeatherManager.ServerWeatherManager.getCurrentServerProfile(biome);

                    if (world.random.nextInt(profile.playerChance()) == 0) {
                        int x = player.getBlockX() + (world.random.nextInt(80) - 40);
                        int z = player.getBlockZ() + (world.random.nextInt(80) - 40);
                        BlockPos strikePos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));

                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
                        bolt.moveTo(Vec3.atBottomCenterOf(strikePos));
                        world.addFreshEntity(bolt);
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
        if (world.isThundering() && WeatherSafetyHelper.isOutside(world, playerPos)) {
            var profileHolder = world.getBiome(playerPos);
            var profile = WeatherManager.ServerWeatherManager.getCurrentServerProfile(profileHolder);
            int r = 16;
            BlockPos targetPos = null;
            int attempts = 0;

            while (attempts < 10) {
                BlockPos potentialPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                        playerPos.offset(world.random.nextInt(r*2)-r, 0, world.random.nextInt(r*2)-r));

                if (world.getBiome(potentialPos).equals(profileHolder)) {
                    targetPos = potentialPos;
                    break;
                }
                attempts++;
            }
            if (targetPos == null) targetPos = playerPos;

            for (String action : profile.effects()) {
                System.out.println("profile: " + profile);
                runWeatherAction(world, player, playerPos, targetPos, action.trim());
            }
        }
    }

    private static void runWeatherAction(ServerLevel world, ServerPlayer player, BlockPos playerPos, BlockPos targetPos, String action) {
        if (action.isEmpty() || action.equalsIgnoreCase("none")) return;

        String name;
        String args = "";

        if (action.contains("[") && action.contains("]")) {
            name = action.substring(0, action.indexOf("["));
            args = action.substring(action.indexOf("[") + 1, action.lastIndexOf("]"));
        } else {
            name = action;
        }

        String[] argArray = args.isEmpty() ? new String[0] : args.split(",");


        switch (name) {
            case "hunger":
                player.getFoodData().addExhaustion(argArray.length > 0 ? Float.parseFloat(argArray[0]) : 0.05f);
                break;

            case "damage": // damage[whitelist,amount]
                if (checkWhitelist(player, argArray[0])) {
                    player.hurt(DamageSource.GENERIC, Float.parseFloat(argArray[1]));
                }
                break;

            case "ring_bell": // ring_bell[chance,volume,pitch]
                if (argArray.length > 0 && world.random.nextFloat() < Float.parseFloat(argArray[0])) {
                    float volume = argArray.length > 1 ? Float.parseFloat(argArray[1]) : 1.0f;
                    float pitch = argArray.length > 2 ? Float.parseFloat(argArray[2]) : 1.0f;

                    world.playSound(null, targetPos,
                            net.minecraft.sounds.SoundEvents.BELL_BLOCK,
                            net.minecraft.sounds.SoundSource.BLOCKS,
                            volume, pitch);
                }
                break;

            case "summon": // summon[entity_id,chance]
                if (world.random.nextFloat() < Float.parseFloat(argArray[1])) {
                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(argArray[0]));
                    if (type != null) {
                        net.minecraft.world.entity.Entity e = type.create(world);
                        if (e != null) {
                            e.moveTo(Vec3.atBottomCenterOf(targetPos));
                            world.addFreshEntity(e);
                        }
                    }
                }
                break;

            case "cauldron": // cauldron[block_id,chance] -> fill cauldron with lava/water
                if (world.random.nextFloat() < Float.parseFloat(argArray[1])) {
                    BlockState state = world.getBlockState(targetPos);
                    if (state.getBlock() instanceof CauldronBlock) {
                        Block fill = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(argArray[0]));
                        if (fill != null) world.setBlockAndUpdate(targetPos, fill.defaultBlockState());
                    }
                }
                break;

            case "place": // place[block_id,chance]
                if (world.random.nextFloat() < Float.parseFloat(argArray[1])) {
                    Block b = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(argArray[0]));
                    if (b != null && world.isEmptyBlock(targetPos) && b.defaultBlockState().canSurvive(world, targetPos)) {
                        world.setBlockAndUpdate(targetPos, b.defaultBlockState());
                    }
                }
                break;

            case "layer": // layer[block_id,property_name,max_value,chance]
                if (world.random.nextFloat() < Float.parseFloat(argArray[3])) {
                    ResourceLocation blockId = ResourceLocation.parse(argArray[0]);
                    Block block = ForgeRegistries.BLOCKS.getValue(blockId);
                    if (block == null) break;

                    BlockState currentState = world.getBlockState(targetPos);
                    String propertyName = argArray[1];
                    int maxValue = Integer.parseInt(argArray[2]);

                    if (currentState.is(block)) {
                        currentState.getProperties().stream()
                                .filter(p -> p.getName().equals(propertyName) && p instanceof net.minecraft.world.level.block.state.properties.IntegerProperty)
                                .map(p -> (net.minecraft.world.level.block.state.properties.IntegerProperty) p)
                                .findFirst()
                                .ifPresent(prop -> {
                                    int currentVal = currentState.getValue(prop);
                                    if (currentVal < maxValue) {
                                        world.setBlockAndUpdate(targetPos, currentState.setValue(prop, currentVal + 1));
                                    }
                                });
                    }
                    else if (world.isEmptyBlock(targetPos) && block.defaultBlockState().canSurvive(world, targetPos)) {
                        world.setBlockAndUpdate(targetPos, block.defaultBlockState());
                    }
                }
                break;

            case "freeze":
                if (checkWhitelist(player, "player") && player.getTicksFrozen() < Integer.parseInt(argArray[0])) {
                    player.setTicksFrozen(player.getTicksFrozen() + 5);
                }
                break;

            case "fire": // fire[chance]
                if (world.random.nextFloat() < Float.parseFloat(argArray[0])) {
                    if (world.isEmptyBlock(targetPos)) world.setBlockAndUpdate(targetPos, Blocks.FIRE.defaultBlockState());
                }
                break;

            case "velocity": // velocity[x,y,z]
                player.push(Double.parseDouble(argArray[0]), Double.parseDouble(argArray[1]), Double.parseDouble(argArray[2]));
                player.hurtMarked = true;
                break;

            case "extinguish":
                if (player.isOnFire()) player.clearFire();
                break;

            case "command": // command[chance(optional)]
                if (argArray.length > 0) {
                    if (world.random.nextFloat() < Float.parseFloat(argArray[0])) {
                        world.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), argArray[0]);
                    }
                } else {
                    world.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4).withSuppressedOutput(), argArray[0]);
                }
                break;

            case "hydrate": // hydrate[radius,chance]
                if (world.random.nextFloat() < Float.parseFloat(argArray[1])) {
                    int rad = Integer.parseInt(argArray[0]);
                    BlockPos.betweenClosed(targetPos.offset(-rad, -1, -rad), targetPos.offset(rad, 1, rad)).forEach(bp -> {
                        BlockState state = world.getBlockState(bp);
                        if (state.is(Blocks.FARMLAND)) {
                            world.setBlockAndUpdate(bp, state.setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
                        }
                    });
                }
                break;

            case "bee_hive": // bee_hive[radius] -> Forces bees into hives
                world.getEntitiesOfClass(net.minecraft.world.entity.animal.Bee.class, new net.minecraft.world.phys.AABB(targetPos).inflate(Double.parseDouble(argArray[0]))).forEach(bee -> {
                    if (bee.getHivePos() != null) {
                        bee.setStayOutOfHiveCountdown(0);
                    }
                });
                break;

            case "water_animal_buffer": // water_animal_buffer[radius] -> Prevents dolphins/axolotls from drying out
                world.getEntitiesOfClass(LivingEntity.class, new net.minecraft.world.phys.AABB(targetPos).inflate(Double.parseDouble(argArray[0]))).forEach(e -> {
                    if (e instanceof net.minecraft.world.entity.animal.axolotl.Axolotl || e instanceof net.minecraft.world.entity.animal.Dolphin) {
                        e.setAirSupply(e.getMaxAirSupply());
                    }
                });
                break;
            case "damage_armor": // damage_armor[slot,amount,chance] -> e.g., damage_armor[chest,1,0.05]
                if (world.random.nextFloat() < Float.parseFloat(argArray[2])) {
                    EquipmentSlot slot = null;
                    String inputSlot = argArray[0].toLowerCase();

                    slot = switch (inputSlot) {
                        case "head", "helmet" -> EquipmentSlot.HEAD;
                        case "chest", "chestplate" -> EquipmentSlot.CHEST;
                        case "legs", "leggings" -> EquipmentSlot.LEGS;
                        case "feet", "boots" -> EquipmentSlot.FEET;
                        default -> slot;
                    };

                    if (slot != null) {
                        var stack = player.getItemBySlot(slot);
                        if (!stack.isEmpty() && stack.isDamageableItem()) {
                            int damageAmount = Integer.parseInt(argArray[1]);
                            EquipmentSlot finalSlot = slot;
                            stack.hurtAndBreak(damageAmount, player, (p) -> p.broadcastBreakEvent(finalSlot));
                        }
                    }
                }
                break;
            case "none":
                break;
        }
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
