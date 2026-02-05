package pl.sula008.fuelmod.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockBreakingMovementBehaviour.class, remap = false)
public abstract class ToolDisablerMixin {

    // Wstrzykujemy się w metodę, która pyta: "Czy mogę zniszczyć ten blok?"
    @Inject(method = "canBreak", at = @At("HEAD"), cancellable = true)
    private void fuelmod$onCanBreak(Level world, BlockPos pos, MovementContext context, CallbackInfoReturnable<Boolean> cir) {
        // Jeśli contraption NIE JEST pociągiem, zwracamy false (nie niszcz)
        if (!(context.contraption instanceof CarriageContraption)) {
            cir.setReturnValue(false);
        }
    }
}