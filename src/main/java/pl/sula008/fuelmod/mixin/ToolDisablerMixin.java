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

    /**
     * Wstrzykujemy się w tick() klasy bazowej dla wierteł i pił.
     * Jeśli to nie jest pociąg, po prostu nie pozwalamy narzędziu "myśleć" o niszczeniu bloków.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void fuelmod$stopBreakingTick(MovementContext context, CallbackInfo ci) {

        // Jeśli to nie jest pociąg, przerywamy cały proces "pracy" narzędzia
        if (!(context.contraption instanceof CarriageContraption)) {

            // Zerujemy postęp niszczenia bloku (żeby nie pękł po czasie)
            context.data.putInt("BreakingPos", -1);

            // Anulujemy dalsze wykonywanie logiki niszczenia
            ci.cancel();
        }
    }
}