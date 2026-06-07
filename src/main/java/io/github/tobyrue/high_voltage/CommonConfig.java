package io.github.tobyrue.high_voltage;

import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = HighVoltage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WEATHER_PROFILES;

    static {

        BUILDER.push("Biome Mechanics");
        WEATHER_PROFILES = BUILDER.comment(
                "Format: 'biome_id_or_tag|texture_path|fog_color|texture_tint|fog_start|fog_end|vertical_speed|horizontal_wind|particle_id|has_rain_sound'",
                "",
                "FIELDS:",
                "1. Biome: Registry name (minecraft:desert) or tag (#minecraft:is_desert)",
                "2. Texture: Path to the environment texture (minecraft:textures/environment/rain.png)",
                "3. Fog Color: Hex code for the weather fog (e.g., 0xFFFFFF)",
                "4. Texture Tint: Hex code to color the precipitation texture itself (e.g., 0xFFFFFF for no tint)",
                "5. Fog Start: Distance where fog begins (Vanilla is ~2.0 to 8.0)",
                "6. Fog End: Distance where fog is maximum (Vanilla is ~25.0 to 60.0)",
                "7. Vertical Speed: How fast the texture falls (Vanilla is 1.0)",
                "8. Horizontal Wind: How much the texture slants/drifts (Vanilla is 0.0)",
                "9. Particle ID: The particle spawned on the ground (e.g., minecraft:rain or high_voltage:sand_bead)",
                "10. Rain Sound: Boolean (true/false) to play the vanilla rain loop",
                "",
                "EXAMPLE (Sandstorm):",
                "'#minecraft:is_desert|high_voltage:textures/environment/sand_storm.png|0xD2B48C|0xFFCC00|2.0|15.0|2.0|1.5|minecraft:sand|false'"
        ).defineList("weatherProfiles", List.of(
                "#forge:is_desert|high_voltage:textures/environment/dust.png|#E3BC82|#E3BC82|2|25|0.2|8.5|null|false",
                "#minecraft:is_badlands|high_voltage:textures/environment/dust.png|#A85B0A|#A85B0A|8|32|0.2|8.5|null|false",
                "#forge:is_snowy|high_voltage:textures/environment/heavy_snow.png|#FFFFFF|#FFFFFF|5|40|5.0|2.5|null|false",
                "minecraft:jungle|high_voltage:textures/environment/tropical_rain.png|#224422|#FFFFFF|10|30|4.0|1.0|minecraft:rain|true"
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
