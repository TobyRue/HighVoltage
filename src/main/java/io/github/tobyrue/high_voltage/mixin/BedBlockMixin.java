package io.github.tobyrue.high_voltage.mixin;

import com.mojang.datafixers.util.Either;
import io.github.tobyrue.high_voltage.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(BedBlock.class)
public class BedBlockMixin {
    @Redirect(
            method = "lambda$use$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"
            ),
            remap = false
    )
    private static void high_voltage$redirectLambdaSleepMessage(Player player, Component message, boolean isActionBar) {
        if (Config.PREVENT_SLEEP_THUNDER.get() && !player.level.isNight()) {
            if (message.getContents() instanceof TranslatableContents translatable) {
                if (translatable.getKey().equals("block.minecraft.bed.no_sleep")) {
                    player.displayClientMessage(Component.translatable("block.high_voltage.bed.not_possible_now"), true);
                    return;
                }
            }
        }

        player.displayClientMessage(message, isActionBar);
    }
}
