package pl.sula008.fuelmod.mixin;

import com.simibubi.create.content.trains.entity.*;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public abstract class TrainMixin {

    @Unique
    private int fuelmod$tickCounter = 0;

    @Unique
    private final ServerBossEvent fuelmod$bossBar = new ServerBossEvent(
            Component.literal("Paliwo pociągu"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.PROGRESS
    );

    @Unique
    private final Set<UUID> fuelmod$trackedPlayers = new HashSet<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void fuelmod$onTick(CallbackInfo ci) {
        Train train = (Train) (Object) this;


        fuelmod$manageBossBarPlayers(train);


        fuelmod$tickCounter++;
        if (fuelmod$tickCounter >= 20) {
            fuelmod$tickCounter = 0;
            fuelmod$runFuelLogic(train);
        }
    }

    @Unique
    private void fuelmod$runFuelLogic(Train train) {
        long currentFuel = 0;
        long maxFuel = 0;
        boolean hasTank = false;
        int activeTools = 0;
        int drainRate = 0;

        if (train.carriages == null) return;

        for (Carriage carriage : train.carriages) {
            if (fuelmod$hasBlock(carriage, "railways:fuel_tank")) hasTank = true;
            activeTools += fuelmod$countTools(carriage);

            IFluidHandler fluids = carriage.storage.getFluids();
            if (fluids != null) {
                for (int i = 0; i < fluids.getTanks(); i++) {
                    FluidStack stack = fluids.getFluidInTank(i);
                    int rate = fuelmod$getRate(stack);
                    if (rate > 0) {
                        drainRate = rate;
                        currentFuel += stack.getAmount();
                        maxFuel += fluids.getTankCapacity(i);
                    }
                }
            }
        }

        if (hasTank && currentFuel > 0 && Math.abs(train.speed) > 0.01) {
            int toDrain = drainRate + (activeTools * FuelConfig.TOOL_DRAIN_PER_SECOND.get().intValue());
            if (toDrain > 0) {
                fuelmod$drainFromTanks(train, toDrain);
                currentFuel -= toDrain;
            }
        }

        fuelmod$updateBossBar(currentFuel, maxFuel, hasTank, activeTools);

        if (!hasTank || (currentFuel <= 0)) {
            train.throttle = 0;
        }
    }

    @Unique
    private void fuelmod$updateBossBar(long current, long max, boolean hasTank, int tools) {
        if (!hasTank || max <= 0) {
            fuelmod$bossBar.setVisible(false);
            return;
        }

        float progress = Math.min(1.0f, Math.max(0.0f, (float) current / (float) max));
        fuelmod$bossBar.setProgress(progress);
        fuelmod$bossBar.setVisible(true);

        if (progress > 0.6f) fuelmod$bossBar.setColor(BossEvent.BossBarColor.GREEN);
        else if (progress > 0.2f) fuelmod$bossBar.setColor(BossEvent.BossBarColor.YELLOW);
        else fuelmod$bossBar.setColor(BossEvent.BossBarColor.RED);

        String toolInfo = tools > 0 ? " | Maszyny: " + tools : "";
        fuelmod$bossBar.setName(Component.literal("Paliwo: " + current + " / " + max + " mB" + toolInfo));
    }

    @Unique
    private void fuelmod$manageBossBarPlayers(Train train) {
        if (train.carriages == null) return;

        Set<UUID> currentPassengers = new HashSet<>();
        Set<ServerPlayer> playersToAdd = new HashSet<>();


        for (Carriage carriage : train.carriages) {
            Entity entity = carriage.anyAvailableEntity();
            if (entity != null) {
                for (Entity passenger : entity.getPassengers()) {
                    if (passenger instanceof ServerPlayer player) {
                        currentPassengers.add(player.getUUID());
                        if (!fuelmod$trackedPlayers.contains(player.getUUID())) {
                            playersToAdd.add(player);
                        }
                    }
                }
            }
        }


        for (ServerPlayer p : playersToAdd) {
            fuelmod$bossBar.addPlayer(p);
            fuelmod$trackedPlayers.add(p.getUUID());
        }


        if (fuelmod$trackedPlayers.size() != currentPassengers.size()) {
            // Musimy pobrać listę aktualnych graczy z BossBara i ich przefiltrować
            fuelmod$bossBar.getPlayers().forEach(player -> {
                if (!currentPassengers.contains(player.getUUID())) {
                    fuelmod$bossBar.removePlayer(player);
                    fuelmod$trackedPlayers.remove(player.getUUID());
                }
            });
        }
    }

    @Unique
    private void fuelmod$drainFromTanks(Train train, int amount) {
        int remaining = amount;
        for (Carriage carriage : train.carriages) {
            IFluidHandler fluids = carriage.storage.getFluids();
            if (fluids != null) {
                FluidStack drained = fluids.drain(remaining, IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty()) {
                    remaining -= drained.getAmount();
                    Entity entity = carriage.anyAvailableEntity();
                    if (entity instanceof CarriageContraptionEntity cce && cce.level() != null) {
                        cce.level().getChunkAt(cce.blockPosition()).setUnsaved(true);
                    }
                }
            }
            if (remaining <= 0) break;
        }
    }

    @Unique
    private int fuelmod$getRate(FluidStack stack) {
        if (stack.isEmpty()) return -1;
        ResourceLocation rl = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (rl == null) return -1;
        String id = rl.toString();
        for (String entry : FuelConfig.FUEL_SETTINGS.get()) {
            String[] parts = entry.split(",");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(id)) {
                try { return Integer.parseInt(parts[1]); } catch (Exception e) { return 1; }
            }
        }
        return -1;
    }

    @Unique
    private int fuelmod$countTools(Carriage carriage) {
        final int[] count = {0};
        Entity entity = carriage.anyAvailableEntity();
        if (entity instanceof CarriageContraptionEntity cce && cce.getContraption() instanceof CarriageContraption cc) {
            cc.getActors().forEach(pair -> {
                MovementContext ctx = pair.right;
                if (ctx != null && ctx.state != null && !ctx.disabled) {
                    ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(ctx.state.getBlock());
                    if (rl != null && (rl.toString().contains("drill") || rl.toString().contains("saw"))) {
                        count[0]++;
                    }
                }
            });
        }
        return count[0];
    }

    @Unique
    private boolean fuelmod$hasBlock(Carriage carriage, String blockId) {
        Entity entity = carriage.anyAvailableEntity();
        if (entity instanceof CarriageContraptionEntity cce && cce.getContraption() != null) {
            for (StructureTemplate.StructureBlockInfo info : cce.getContraption().getBlocks().values()) {
                ResourceLocation id = ForgeRegistries.BLOCKS.getKey(info.state().getBlock());
                if (id != null && id.toString().equals(blockId)) return true;
            }
        }
        return false;
    }
}