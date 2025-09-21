package me.thedivazo.zombiecouriers.mixin;

import net.minecraft.entity.monster.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public abstract class ZombieNoSunBurnMixin {

    @Inject(method = "isSunSensitive()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void zombiecouriers$noSunBurnFromSun(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
