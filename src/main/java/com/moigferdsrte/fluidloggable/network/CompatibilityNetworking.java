package com.moigferdsrte.fluidloggable.network;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Consumer;

public final class CompatibilityNetworking {
    private static final ConfigurationTask.Type TASK = new ConfigurationTask.Type("fluidloggable:compatibility");
    private static boolean activeAtStartup;

    private CompatibilityNetworking() {}

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        activeAtStartup = FMLEnvironment.getDist() == Dist.DEDICATED_SERVER
                || !FluidloggableConfig.isClientOnlyCompatibilityModeEnabled();
        // Optional at the transport level so compatibility-mode clients can join unmodified servers.
        // The configuration task and client connection guard enforce the gameplay requirement.
        final var registrar = event.registrar("neoforge-core-1").optional();
        registrar.configurationBidirectional(CompatibilityPayload.TYPE, CompatibilityPayload.CODEC,
                CompatibilityNetworking::acknowledge);
        registrar.playToClient(ClientboundFluidUpdatePacket.TYPE, ClientboundFluidUpdatePacket.STREAM_CODEC);
    }

    private static void acknowledge(final CompatibilityPayload payload, final IPayloadContext context) {
        if (!activeAtStartup || payload.protocol() != ServerCompatibility.PROTOCOL) {
            context.disconnect(Component.literal("Fluidloggable versions or compatibility modes do not match."));
            return;
        }
        context.finishCurrentTask(TASK);
    }

    public static void registerTasks(final RegisterConfigurationTasksEvent event) {
        if (!activeAtStartup) return;
        if (!event.getListener().hasChannel(CompatibilityPayload.TYPE)) {
            event.getListener().disconnect(Component.literal(
                    "This server requires matching Fluidloggable on the client. Disable Client-only Compatibility Mode and restart Minecraft."));
            return;
        }
        event.register(new CompatibilityTask());
    }

    private record CompatibilityTask() implements ICustomConfigurationTask {
        @Override
        public void run(final Consumer<CustomPacketPayload> sender) {
            sender.accept(new CompatibilityPayload(ServerCompatibility.PROTOCOL));
        }
        @Override
        public Type type() { return TASK; }
    }
}
