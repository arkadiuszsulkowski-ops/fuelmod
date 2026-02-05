package pl.sula008.fuelmod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Fuelmod.MOD_ID)
public class Fuelmod {
    public static final String MOD_ID = "fuelmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Fuelmod() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FuelConfig.SPEC);

        LOGGER.info("FuelMod has been initialized successfully!");
    }
}