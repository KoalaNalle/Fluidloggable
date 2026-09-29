package com.moigferdsrte.fluidloggable.gametest;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Boots the real client, waits for resource loading, and exits without opening a user world. */
@Mod(value = "fluidloggable_gametest", dist = Dist.CLIENT)
public final class NeoForgeClientSmokeTest {
    private int ticks;

    public NeoForgeClientSmokeTest() {
        if (Boolean.getBoolean("fluidloggable.clientSmokeTest")) {
            NeoForge.EVENT_BUS.addListener(this::tick);
        }
    }

    private void tick(final ClientTickEvent.Post event) {
        final var client = Minecraft.getInstance();
        // NeoForge emits client ticks only after the first resource reload completes.
        if (++ticks == 20) {
            // Some targets are otherwise first loaded only when joining a world.
            try {
                for (String target : new String[]{
                        "net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl",
                        "net.minecraft.client.renderer.block.FluidRenderer",
                        "net.minecraft.client.renderer.chunk.RenderSectionRegion",
                        "net.minecraft.client.renderer.chunk.SectionCompiler",
                        "net.minecraft.client.renderer.chunk.SectionCopy"}) {
                    Class.forName(target);
                }
            } catch (ClassNotFoundException exception) {
                throw new AssertionError("Missing client mixin target", exception);
            }
            Fluidloggable.LOGGER.info("Fluidloggable NeoForge client startup smoke test passed");
            client.stop();
        }
    }
}
