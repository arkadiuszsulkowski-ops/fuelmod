package pl.sula008.fuelmod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(Fuelmod.MOD_ID)
public class Fuelmod {
    public static final String MOD_ID = "fuelmod";

    public Fuelmod() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FuelConfig.SPEC);

    }
}
