package io.github.tobyrue.high_voltage.mixin;

import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.DisableElytraEffect;
import io.github.tobyrue.high_voltage.data.effects.DisableSprintingEffect;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void high_voltage$denyElytraInStorm(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (player.level.isThundering()) {
            Holder<Biome> biome = player.level.getBiome(player.blockPosition());
            WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, player.level);

            if (profile != null) {
                for (WeatherProfile.WeatherEffect effect : profile.effects()) {
                    if (effect instanceof DisableElytraEffect && player.isFallFlying()) {
                        player.stopFallFlying();
                    } else if (effect instanceof DisableSprintingEffect && player.isSprinting()) {
                        player.setSprinting(false);

                        player.onUpdateAbilities();

                        if (player.connection != null) {
                            player.connection.send(new ClientboundUpdateAttributesPacket(
                                    player.getId(),
                                    player.getAttributes().getSyncableAttributes()
                            ));
                        }
                    }
                }
            }
        }
    }
}