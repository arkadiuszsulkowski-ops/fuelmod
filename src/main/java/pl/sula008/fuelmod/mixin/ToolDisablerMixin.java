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
        // Zawsze sprawdzaj null-safety, aby uniknąć "Ticking entity" crashu
        if (context == null || context.contraption == null) {
            return;
        }

        // Jeśli to NIE jest pociąg, po prostu wyjdź z metody (anuluj tick)
        if (!(context.contraption instanceof CarriageContraption)) {
            // Rezygnujemy z context.data.putInt, żeby nie wywalało błędu NoSuchMethodError
            // Anulowanie ci wystarczy, by wiertło/piła nic nie zrobiły w tej klatce
            ci.cancel();
        }
    }
}