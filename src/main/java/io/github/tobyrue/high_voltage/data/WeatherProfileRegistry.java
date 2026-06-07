package io.github.tobyrue.high_voltage.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class WeatherProfileRegistry extends SimpleJsonResourceReloadListener {

    public WeatherProfileRegistry(Gson gson) {
        super(gson, "weather_profiles");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> p_10793_, ResourceManager p_10794_, ProfilerFiller p_10795_) {

    }
}
