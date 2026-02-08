package pl.sula008.fuelmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fuelmod", value = Dist.CLIENT)
public class FuelHudOverlay {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        event.getGuiGraphics().drawString(
                Minecraft.getInstance().font,
                "HUD DZIAŁA",
                10, 10,
                0xFFFFFF,
                false
        );
    }
}
