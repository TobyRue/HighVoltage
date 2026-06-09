package io.github.tobyrue.high_voltage.data;

public class WeatherSyncHandler {
    public static float globalRainLevel = 0.0f;
    public static boolean globalIsThundering = false;

    public static void setGlobalWeather(float rain, boolean thunder) {
        globalRainLevel = rain;
        globalIsThundering = thunder;
    }
}
