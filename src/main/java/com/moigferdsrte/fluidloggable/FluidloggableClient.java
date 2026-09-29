package com.moigferdsrte.fluidloggable;

import com.moigferdsrte.fluidloggable.network.ClientCompatibilityNetworking;
import com.moigferdsrte.fluidloggable.network.CompatibilityPayload;
import com.moigferdsrte.fluidloggable.network.ClientboundFluidUpdatePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = Fluidloggable.MOD_ID, dist = Dist.CLIENT)
public final class FluidloggableClient {
    public FluidloggableClient(final IEventBus modBus) {
        ClientCompatibilityNetworking.initialize();
        modBus.addListener(FluidloggableClient::registerPayloadHandlers);
    }

    private static void registerPayloadHandlers(final RegisterClientPayloadHandlersEvent event) {
        event.register(CompatibilityPayload.TYPE, ClientCompatibilityNetworking::handle);
        event.register(ClientboundFluidUpdatePacket.TYPE,
                (payload, context) -> ClientboundFluidUpdatePacket.apply(payload));
    }

}
