package pl.sula008.fuelmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import java.util.Optional;

public class FuelCommands {

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Rejestrujemy komendę bez żadnych hasPermission i bez skomplikowanych builderów
        dispatcher.register(Commands.literal("fuelmod")
                .then(Commands.literal("reload")
                        .executes(context -> {
                            Optional<? extends ModConfig> config = ConfigTracker.INSTANCE.configSets()
                                    .get(ModConfig.Type.COMMON)
                                    .stream()
                                    .filter(c -> c.getSpec() == FuelConfig.SPEC)
                                    .findFirst();

                            if (config.isPresent()) {
                                FuelConfig.SPEC.acceptConfig(config.get().getConfigData());
                                context.getSource().sendSuccess(() -> Component.literal("§aFuelMod reloaded!"), true);
                                return 1;
                            }
                            return 0;
                        })
                )
        );
    }
}