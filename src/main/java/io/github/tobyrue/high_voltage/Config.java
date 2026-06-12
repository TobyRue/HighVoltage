package io.github.tobyrue.high_voltage;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;


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

}
