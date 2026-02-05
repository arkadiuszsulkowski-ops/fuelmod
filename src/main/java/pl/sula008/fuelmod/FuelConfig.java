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
        BUILDER.push("Ustawienia Paliwa FuelMod");

        TOOL_DRAIN_PER_SECOND = BUILDER
                .comment("Ile mB paliwa pobiera JEDNA aktywna maszyna (wiertło/piła) na SEKUNDĘ pracy.",
                        "")
                .defineInRange("toolDrainPerSecond", 5, 0.0, 1000.0);

        FUEL_SETTINGS = BUILDER
                .comment("Format: 'modid:fluid,zuzycie_na_sekunde'",
                        "Przykład: 'minecraft:lava,2' oznacza, że pociąg bez maszyn spali 2mB na sekundę.",
                        "")
                .defineList("fuelSettings", Arrays.asList("minecraft:lava,2", "minecraft:magma,4"), entry -> true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}