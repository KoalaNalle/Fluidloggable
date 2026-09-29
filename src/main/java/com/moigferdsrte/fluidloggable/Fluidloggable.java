package com.moigferdsrte.fluidloggable;

import com.moigferdsrte.fluidloggable.network.CompatibilityNetworking;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Fluidloggable.MOD_ID)
public final class Fluidloggable {
    public static final String MOD_ID = "fluidloggable";
    public static final int UPDATE_SCHEDULE_FLUID_TICK = 0x100000;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Fluidloggable(final IEventBus modBus) {
        LOGGER.info("Initializing Fluidloggable for NeoForge");
        modBus.addListener(CompatibilityNetworking::registerPayloads);
        modBus.addListener(CompatibilityNetworking::registerTasks);
    }

    public static Identifier id(final String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
