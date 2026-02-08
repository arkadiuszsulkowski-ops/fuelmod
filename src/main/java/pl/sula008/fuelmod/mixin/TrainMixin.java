package pl.sula008.fuelmod.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.sula008.fuelmod.FuelConfig;

@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin {

    @Unique
    private long fuelmod$lastFuelTime = System.nanoTime();

    @Unique
    private boolean fuelmod$noFuel = false;


    /* =========================
       SPALANIE – REALNA SEKUNDA
       ========================= */
    @Inject(method = "tick", at = @At("HEAD"))
    private void fuelmod$fuelTick(CallbackInfo ci) {
        Train train = (Train) (Object) this;



        if (Math.abs(train.speed) < 0.01) return;

        long now = System.nanoTime();
        long elapsed = now - fuelmod$lastFuelTime;

        if (elapsed < 1_000_000_000L) return;

        long seconds = elapsed / 1_000_000_000L;
        fuelmod$lastFuelTime += seconds * 1_000_000_000L;

        fuelmod$runFuelLogic(train, seconds);
    }

    /* =========================
       BLOKADA RUCHU – BETON
       ========================= */

    @Inject(method = "tick", at = @At("HEAD"))
    private void fuelmod$hardStop(CallbackInfo ci) {
        if (!fuelmod$noFuel) return;

        Train train = (Train) (Object) this;

        // 🚫 CAŁKOWITA BLOKADA RUCHU (SCROLL NIE DZIAŁA)
        train.throttle = 0;
        train.targetSpeed = 0;
        train.speed = 0;

        // 🚫 BLOKADA NARZĘDZI
        if (train.carriages == null) return;

        for (Carriage carriage : train.carriages) {
            Entity e = carriage.anyAvailableEntity();
            if (!(e instanceof CarriageContraptionEntity cce)) continue;
            if (!(cce.getContraption() instanceof CarriageContraption cc)) continue;

            for (var pair : cc.getActors()) {
                MovementContext ctx = pair.right;
                if (ctx != null) {
                    ctx.disabled = true; // 🔥 DRILLE / SAWY OFF
                }
            }
        }
    }

    /* =========================
       LOGIKA PALIWA
       ========================= */
    @Unique
    private void fuelmod$runFuelLogic(Train train, long seconds) {
        if (train.carriages == null || train.carriages.isEmpty()) {
            fuelmod$noFuel = true;
            return;
        }

        boolean hasTank = false;
        long totalFuel = 0;
        int baseDrain = 0;
        int tools = 0;

        for (Carriage c : train.carriages) {
            if (fuelmod$hasFuelTank(c)) hasTank = true;
            tools += fuelmod$countTools(c);

            IFluidHandler fluids = c.storage.getFluids();
            if (fluids == null) continue;

            for (int i = 0; i < fluids.getTanks(); i++) {
                FluidStack fs = fluids.getFluidInTank(i);
                int rate = fuelmod$getRate(fs);
                if (!fs.isEmpty() && rate > 0) {
                    totalFuel += fs.getAmount();
                    baseDrain = rate;
                }
            }
        }

        if (!hasTank || totalFuel <= 0) {
            fuelmod$noFuel = true;
            return;
        }

        fuelmod$noFuel = false;

        int perSecond = baseDrain + (int)(tools * FuelConfig.TOOL_DRAIN_PER_SECOND.get());
        int toDrain = (int)(perSecond * seconds);

        fuelmod$drainFromTanks(train, toDrain);
    }

    /* =========================
       DRAIN
       ========================= */
    @Unique
    private void fuelmod$drainFromTanks(Train train, int amount) {
        int remaining = amount;
        for (Carriage c : train.carriages) {
            if (remaining <= 0) break;
            IFluidHandler f = c.storage.getFluids();
            if (f == null) continue;
            FluidStack d = f.drain(remaining, IFluidHandler.FluidAction.EXECUTE);
            remaining -= d.getAmount();
        }
    }

    /* =========================
       HELPERY
       ========================= */
    @Unique
    private int fuelmod$getRate(FluidStack stack) {
        if (stack.isEmpty()) return -1;
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (id == null) return -1;

        for (String e : FuelConfig.FUEL_SETTINGS.get()) {
            String[] p = e.split(",");
            if (p.length == 2 && p[0].equalsIgnoreCase(id.toString())) {
                try { return Integer.parseInt(p[1].trim()); }
                catch (Exception ignored) {}
            }
        }
        return -1;
    }

    @Unique
    private boolean fuelmod$hasFuelTank(Carriage c) {
        Entity e = c.anyAvailableEntity();
        if (!(e instanceof CarriageContraptionEntity ce)) return false;
        if (ce.getContraption() == null) return false;

        for (var info : ce.getContraption().getBlocks().values()) {
            if (info != null && info.toString().contains("railways:fuel_tank"))
                return true;
        }
        return false;
    }

    @Unique
    private int fuelmod$countTools(Carriage c) {
        Entity e = c.anyAvailableEntity();
        if (!(e instanceof CarriageContraptionEntity ce)) return 0;
        if (!(ce.getContraption() instanceof CarriageContraption cc)) return 0;

        int count = 0;
        for (var p : cc.getActors()) {
            MovementContext ctx = p.right;
            if (ctx == null || ctx.disabled || ctx.state == null) continue;
            String s = ctx.state.toString().toLowerCase();
            if (s.contains("drill") || s.contains("saw")) count++;
        }
        return count;
    }
}
