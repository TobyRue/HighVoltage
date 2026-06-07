package io.github.tobyrue.high_voltage;

import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = HighVoltage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;


    public static final ForgeConfigSpec.DoubleValue SNOW_ACCUMULATION_RATE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HEAT_SOURCES;
    public static final ForgeConfigSpec.BooleanValue PREVENT_SLEEP_THUNDER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SERVER_WEATHER_PROFILES;

    static {

        BUILDER.push("Biome Mechanics");
        SNOW_ACCUMULATION_RATE = BUILDER.comment("Chance per tick for snow to grow in blizzard").defineInRange("snowGrowthRate", 0.05, 0.0, 1.0);
        HEAT_SOURCES = BUILDER.comment("Format: 'modid:block[property=value]:radius'")
                .defineList("heatSources", List.of(
                        "minecraft:lava[level=0]:8",
                        "minecraft:lit_campfire:6",
                        "minecraft:torch:3",
                        "minecraft:lit_furnace:4"
                ), o -> o instanceof String);

        PREVENT_SLEEP_THUNDER = BUILDER.define("preventSleepingDuringThunder", true);

        SERVER_WEATHER_PROFILES = BUILDER.comment(
                "Format: 'biome_id_or_tag|texture_path|fog_color|texture_tint|fog_start|fog_end|vertical_speed|horizontal_wind|particle_id|has_rain_sound'",
                "",
                "FIELDS:",
                "1. LightningChance: Vanilla is 100000, BEWARE THE LOWER THE VALUE THE MORE LIGHTNING",
                "2. PlayerRiskChance: 1 in X chance to strike near player every tick",
                "3. Effects: "
        ).defineList("weatherProfiles", List.of(
                "#forge:is_desert|10000|1000|damage_armor[chest,1,0.01]",
                "#minecraft:is_badlands|10000|1000|damage_armor[chest,1,0.01]",
                "#forge:is_snowy|10000|1000|layer[minecraft:snow,layers,8,0.25]",
                "minecraft:jungle|10000|1000|ring_bell[0.8]"
        ), o -> o instanceof String);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
//    @SubscribeEvent
//    static void onLoad(final ModConfigEvent event) {
//        lightningChance = LIGHTNING_CHANCE.get();
////        logDirtBlock = LOG_DIRT_BLOCK.get();
////        magicNumber = MAGIC_NUMBER.get();
////        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();
////
////        // convert the list of strings into a set of items
////        items = ITEM_STRINGS.get().stream()
////                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
////                .collect(Collectors.toSet());
//    }
}
