package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.MyNetworkHandler;
import io.github.tobyrue.high_voltage.data.WeatherSyncPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WeatherCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherCommand.class)
public class WeatherCommandMixin {
    @Inject(method = "setClear", at = @At("HEAD"))
    private static void high_voltage$syncClear(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            overworld.setWeatherParameters(time, 0, false, false);
            float rain = overworld.getRainLevel(1.0F);
            boolean thunder = overworld.isThundering();
            WeatherSyncPacket packet = new WeatherSyncPacket(rain, thunder);
            source.getServer().getPlayerList().getPlayers().forEach(player -> {
                MyNetworkHandler.CHANNEL.sendTo(
                        packet,
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }
    }

    @Inject(method = "setRain", at = @At("HEAD"))
    private static void high_voltage$syncRain(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            overworld.setWeatherParameters(0, time, true, false);
            float rain = overworld.getRainLevel(1.0F);
            boolean thunder = overworld.isThundering();
            WeatherSyncPacket packet = new WeatherSyncPacket(rain, thunder);
            source.getServer().getPlayerList().getPlayers().forEach(player -> {
                MyNetworkHandler.CHANNEL.sendTo(
                        packet,
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }
    }

    @Inject(method = "setThunder", at = @At("HEAD"))
    private static void high_voltage$syncThunder(CommandSourceStack source, int time, CallbackInfoReturnable<Integer> cir) {
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            overworld.setWeatherParameters(0, time, true, true);
            float rain = overworld.getRainLevel(1.0F);
            boolean thunder = overworld.isThundering();
            WeatherSyncPacket packet = new WeatherSyncPacket(rain, thunder);
            source.getServer().getPlayerList().getPlayers().forEach(player -> {
                MyNetworkHandler.CHANNEL.sendTo(
                        packet,
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }
    }
}