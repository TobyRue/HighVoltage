package io.github.tobyrue.high_voltage.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public class VisibilityMixin {
    @Inject(method = "test", at = @At("RETURN"), cancellable = true)
    private void reduceAggroInStorm(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (attacker.level.isThundering()) {
            double dist = attacker.distanceToSqr(target);
            if (dist > 256.0) {
                cir.setReturnValue(false);
            }
        }
    }
}