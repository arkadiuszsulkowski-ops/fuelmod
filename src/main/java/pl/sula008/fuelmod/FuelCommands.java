package pl.sula008.fuelmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = "fuelmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FuelCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("fuelmod")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(context -> {
                            ConfigTracker.INSTANCE.configSets().get(ModConfig.Type.COMMON)
                                    .forEach(config -> config.getSpec().acceptConfig(config.getConfigData()));

                            // Zmienione na Component.translatable
                            context.getSource().sendSuccess(() ->
                                    Component.translatable("commands.fuelmod.reload.success"), true);

                            return 1;
                        })
                )
        );
    }
}