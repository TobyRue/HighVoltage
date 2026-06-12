package io.github.tobyrue.high_voltage.mixin;

import com.google.common.collect.Multimap;
import io.github.tobyrue.high_voltage.data.WeatherProfile;
import io.github.tobyrue.high_voltage.data.WeatherProfileLoader;
import io.github.tobyrue.high_voltage.data.effects.DisableSprintingEffect;
import io.github.tobyrue.high_voltage.data.effects.ModifyAttributeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

//    @Final
//    @Shadow
//    private AttributeMap attributes;


    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void high_voltage$disableSprinting(boolean sprinting, CallbackInfo ci) {

        var me = (LivingEntity) (Object) this;

        if (sprinting && (me) instanceof Player) {
            if (me.level.isThundering()) {
                Holder<Biome> biome = me.level.getBiome(me.blockPosition());
                WeatherProfile profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, me.level);
                for (WeatherProfile.WeatherEffect action : profile.effects()) {
                    if (action instanceof DisableSprintingEffect effect) {
                        ci.cancel();
                    }
                }
            }
        }
    }

//
//    @Inject(method = "getAttributes", at = @At("HEAD"), cancellable = true)
//    private void high_voltage$addEffects(CallbackInfoReturnable<AttributeMap> cir) {
//        var me = (LivingEntity) (Object) this;
//        BlockPos pos = me.blockPosition();
//        var biome = me.getLevel().getBiome(pos);
//        var profile = WeatherProfileLoader.getProfileForBiomeWithFallback(biome, me.getLevel());
//
//        for (WeatherProfile.WeatherEffect action : profile.effects()) {
//            if (action instanceof ModifyAttributeEffect effect) {
//                attributes.addTransientAttributeModifiers();
//            }
//        }
//    }
}
