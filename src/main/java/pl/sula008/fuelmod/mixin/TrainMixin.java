package pl.sula008.fuelmod.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
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

    /* =========================
       STAN PALIWA (SERWER)
       ========================= */

    @Unique private long fuelmod$currentFuel = 0;
    @Unique private long fuelmod$maxFuel = 0;

    @Unique private int fuelmod$tickCounter = 0;

    /* =========================
       TICK
       ========================= */

    @Inject(method = "tick", at = @At("HEAD"))
    private void fuelmod$onTick(CallbackInfo ci) {
        Train train = (Train) (Object) this;

        fuelmod$tickCounter++;
        if (fuelmod$tickCounter >= 20) { // 1 sekunda
            fuelmod$runFuelLogic(train);
            fuelmod$tickCounter = 0;
        }

        if (train.carriages == null) return;

        if (fuelmod$currentFuel <= 0) {
            train.throttle = 0;
            train.speed = 0;
        }
    }

    /* =========================
       LOGIKA PALIWA
       ========================= */

    @Unique
    private void fuelmod$runFuelLogic(Train train) {
        if (train.carriages == null) return;

        long currentFuel = 0;
        long maxFuel = 0;
        boolean hasTank = false;
        int activeTools = 0;
        int drainRate = 0;

        for (Carriage carriage : train.carriages) {
            if (fuelmod$hasBlock(carriage, "railways:fuel_tank")) {
                hasTank = true;
            }

            activeTools += fuelmod$countTools(carriage);

            IFluidHandler handler = carriage.storage.getFluids();
            if (handler == null) continue;

            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack stack = handler.getFluidInTank(i);
                int rate = fuelmod$getRate(stack.getFluid());

                if (!stack.isEmpty() && rate > 0) {
                    drainRate = rate;
                    currentFuel += stack.getAmount();
                    maxFuel += handler.getTankCapacity(i);
                }
            }
        }

        if (hasTank && currentFuel > 0 && Math.abs(train.speed) > 0.01) {
            int toDrain = drainRate +
                    activeTools * FuelConfig.TOOL_DRAIN_PER_SECOND.get().intValue();

            if (toDrain > 0) {
                fuelmod$drainFromTanks(train, toDrain);
                currentFuel -= toDrain;
            }
        }

        // zapis stanu (BEZ UI)
        fuelmod$currentFuel = Math.max(currentFuel, 0);
        fuelmod$maxFuel = maxFuel;
    }

    /* =========================
       LICZENIE NARZĘDZI
       ========================= */

    @Unique
    private int fuelmod$countTools(Carriage carriage) {
        int count = 0;

        Entity entity = carriage.anyAvailableEntity();
        if (!(entity instanceof CarriageContraptionEntity cce)) return 0;
        if (!(cce.getContraption() instanceof CarriageContraption cc)) return 0;

        for (var pair : cc.getActors()) {
            MovementContext ctx = pair.right;
            if (ctx == null || ctx.disabled || ctx.state == null) continue;

            try {
                var blockField = ctx.state.getClass().getDeclaredField("f_60608_");
                blockField.setAccessible(true);

                var block =
                        (net.minecraft.world.level.block.Block) blockField.get(ctx.state);

                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
                if (id != null) {
                    String s = id.toString();
                    if (s.contains("drill") || s.contains("saw")) {
                        count++;
                    }
                }
            } catch (Exception ignored) {}
        }
        return count;
    }

    /* =========================
       DRENAŻ PALIWA
       ========================= */

    @Unique
    private long fuelmod$drainFromTanks(Train train, long amount) {
        long drained = 0;
        if (train.carriages == null || amount <= 0) return 0;

        for (Carriage carriage : train.carriages) {
            Entity entity = carriage.anyAvailableEntity();
            if (!(entity instanceof CarriageContraptionEntity cce)) continue;
            if (cce.getContraption() == null) continue;

            Object contraption = cce.getContraption();

            try {
                for (var field : contraption.getClass().getDeclaredFields()) {
                    if (!java.util.Map.class.isAssignableFrom(field.getType())) continue;

                    field.setAccessible(true);
                    Object mapObj = field.get(contraption);
                    if (!(mapObj instanceof java.util.Map<?, ?> map)) continue;

                    for (Object value : map.values()) {
                        if (drained >= amount) break;
                        if (!(value instanceof net.minecraft.world.level.block.entity.BlockEntity be)) continue;

                        LazyOptional<IFluidHandler> cap =
                                be.getCapability(ForgeCapabilities.FLUID_HANDLER);

                        IFluidHandler handler = cap.orElse(null);
                        if (handler == null) continue;

                        for (int i = 0; i < handler.getTanks(); i++) {
                            FluidStack stack = handler.getFluidInTank(i);
                            if (stack.isEmpty()) continue;
                            if (fuelmod$getRate(stack.getFluid()) <= 0) continue;

                            int toDrain = (int) Math.min(stack.getAmount(), amount - drained);
                            FluidStack out = handler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                            drained += out.getAmount();
                        }
                    }
                }
            } catch (Exception ignored) {}

            if (drained >= amount) break;
        }
        return drained;
    }

    /* =========================
       RATE PALIWA
       ========================= */

    @Unique
    private int fuelmod$getRate(net.minecraft.world.level.material.Fluid fluid) {
        if (fluid == null) return -1;

        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        if (id == null) return -1;

        for (String entry : FuelConfig.FUEL_SETTINGS.get()) {
            String[] p = entry.split(",");
            if (p.length == 2 && p[0].equalsIgnoreCase(id.toString())) {
                try {
                    return Integer.parseInt(p[1].trim());
                } catch (Exception ignored) {}
            }
        }
        return -1;
    }

    /* =========================
       SPRAWDZANIE BLOKU
       ========================= */

    @Unique
    private boolean fuelmod$hasBlock(Carriage carriage, String blockId) {
        Entity entity = carriage.anyAvailableEntity();
        if (!(entity instanceof CarriageContraptionEntity cce)) return false;
        if (cce.getContraption() == null) return false;

        for (StructureTemplate.StructureBlockInfo info :
                cce.getContraption().getBlocks().values()) {

            try {
                var stateField = StructureTemplate.StructureBlockInfo.class
                        .getDeclaredField("f_74596_");
                stateField.setAccessible(true);

                var state =
                        (net.minecraft.world.level.block.state.BlockState) stateField.get(info);

                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                if (id != null && id.toString().equals(blockId)) {
                    return true;
                }
            } catch (Exception e) {
                if (info.toString().contains(blockId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
