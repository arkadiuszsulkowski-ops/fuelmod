package pl.sula008.fuelmod;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.Arrays;
import java.util.List;

public class FuelConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue TOOL_DRAIN_PER_SECOND;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FUEL_SETTINGS;

    static {
        BUILDER.push("FuelMod General Settings");

        TOOL_DRAIN_PER_SECOND = BUILDER
                .comment("How much fuel (mB) ONE active tool (drill/saw) consumes per SECOND of operation.")
                .translation("fuelmod.config.tool_drain")
                .defineInRange("toolDrainPerSecond", 5.0, 0.0, 1000.0);

        FUEL_SETTINGS = BUILDER
                .comment("Format: 'modid:fluid,drain_per_second'",
                        "Example: 'minecraft:lava,2' means the train consumes 2mB/s even without active tools.")
                .translation("fuelmod.config.fuel_settings")
                .defineList("fuelSettings", Arrays.asList("minecraft:lava,2", "minecraft:magma,4"), entry -> true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}