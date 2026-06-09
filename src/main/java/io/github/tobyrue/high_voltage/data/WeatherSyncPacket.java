package io.github.tobyrue.high_voltage.data;

import io.github.tobyrue.high_voltage.mixin.WeatherRendererMixin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WeatherSyncPacket {
    private final float rainLevel;
    private final boolean isThundering;

    public WeatherSyncPacket(float rainLevel, boolean isThundering) {
        this.rainLevel = rainLevel;
        this.isThundering = isThundering;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(this.rainLevel);
        buf.writeBoolean(this.isThundering);
    }

    public static WeatherSyncPacket decode(FriendlyByteBuf buf) {
        return new WeatherSyncPacket(buf.readFloat(), buf.readBoolean());
    }

    public static void handle(WeatherSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            WeatherRendererMixin.setGlobalWeather(msg.rainLevel, msg.isThundering);
        });
        ctx.get().setPacketHandled(true);
    }
}
