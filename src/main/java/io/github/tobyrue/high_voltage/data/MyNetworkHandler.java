package io.github.tobyrue.high_voltage.data;

import io.github.tobyrue.high_voltage.HighVoltage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class MyNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(HighVoltage.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.messageBuilder(WeatherSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(WeatherSyncPacket::decode)
                .encoder(WeatherSyncPacket::encode)
                .consumerMainThread(WeatherSyncPacket::handle)
                .add();
    }
}