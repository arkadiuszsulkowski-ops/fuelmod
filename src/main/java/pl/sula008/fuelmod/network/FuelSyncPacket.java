package pl.sula008.fuelmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import pl.sula008.fuelmod.client.FuelClientData;

import java.util.function.Supplier;

public class FuelSyncPacket {

    private final long fuel;
    private final long maxFuel;

    public FuelSyncPacket(long fuel, long maxFuel) {
        this.fuel = fuel;
        this.maxFuel = maxFuel;
    }

    public FuelSyncPacket(FriendlyByteBuf buf) {
        this.fuel = buf.readLong();
        this.maxFuel = buf.readLong();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(fuel);
        buf.writeLong(maxFuel);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            FuelClientData.set(fuel, maxFuel);
        });
        ctx.get().setPacketHandled(true);
    }
}
