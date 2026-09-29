package com.moigferdsrte.fluidloggable.network;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientCompatibilityNetworking {
    private static boolean compatibilityModeAtStartup;
    private ClientCompatibilityNetworking() {}

    public static void initialize() {
        compatibilityModeAtStartup = FluidloggableConfig.isClientOnlyCompatibilityModeEnabled();
    }

    public static void handle(final CompatibilityPayload payload, final IPayloadContext context) {
        final var connection = (ClientCompatibilityConnection) context.listener();
        connection.fluidloggable$compatibility().advertise(payload.protocol());
        if (validate(connection)) {
            context.reply(new CompatibilityPayload(ServerCompatibility.PROTOCOL));
        }
    }

    public static boolean validate(final ClientCompatibilityConnection connection) {
        final var problem = connection.fluidloggable$compatibility().problem(compatibilityModeAtStartup);
        if (problem == null) return true;
        connection.fluidloggable$disconnect(Component.translatable(problem.translationKey));
        return false;
    }
}
