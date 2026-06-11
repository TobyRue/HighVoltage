package io.github.tobyrue.high_voltage;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = HighVoltage.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;


    public static final ForgeConfigSpec.BooleanValue PREVENT_SLEEP_THUNDER;

    static {
        BUILDER.push("Biome Mechanics");
        PREVENT_SLEEP_THUNDER = BUILDER.define("preventSleepingDuringThunder", true);
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
