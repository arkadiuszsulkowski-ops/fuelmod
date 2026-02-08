package pl.sula008.fuelmod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import pl.sula008.fuelmod.client.FuelHudOverlay;
import pl.sula008.fuelmod.network.FuelNetwork;

@Mod(Fuelmod.MOD_ID)
public class Fuelmod {
    public static final String MOD_ID = "fuelmod";
    public static final Logger LOGGER = LogUtils.getLogger();


    public void FuelMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(RegisterGuiOverlaysEvent event) {

    }

    public Fuelmod() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FuelConfig.SPEC);
        FuelNetwork.init();
        LOGGER.info("FuelMod has been initialized successfully!");
    }



}