package pl.sula008.fuelmod.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockBreakingMovementBehaviour.class, remap = false)
public abstract class ToolDisablerMixin {


    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void fuelmod$stopBreakingTick(MovementContext context, CallbackInfo ci) {

        if (!(context.contraption instanceof CarriageContraption)) {
            context.data.putInt("BreakingPos", -1);
            ci.cancel();
        }
    }
}