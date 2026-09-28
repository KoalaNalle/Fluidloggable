package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.extension.LevelChunkSectionExtension;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.Fluids;

public final class StoredFluidRandomTickGameTest {
	@GameTest(maxTicks = 1)
	public void randomTickFlagTracksReplacementAndLastRemoval(final GameTestHelper helper) {
		var section = new LevelChunkSection(helper.getLevel().palettedContainerFactory());
		var storage = (LevelChunkSectionExtension) section;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				storage.fluidloggable$setFluidState(x, 0, z, Fluids.WATER.getSource(false));
			}
		}
		helper.assertFalse(section.isRandomlyTickingFluids(), "A water-filled section does not need random fluid ticks");
		storage.fluidloggable$setFluidState(0, 0, 0, Fluids.LAVA.getSource(false));
		storage.fluidloggable$setFluidState(0, 0, 0, Fluids.LAVA.getSource(false));
		storage.fluidloggable$setFluidState(1, 0, 0, Fluids.LAVA.getFlowing(4, false));
		helper.assertTrue(section.isRandomlyTickingFluids(), "Stored lava needs random fluid ticks");
		storage.fluidloggable$setFluidState(0, 0, 0, Fluids.WATER.getSource(false));
		helper.assertTrue(section.isRandomlyTickingFluids(), "Other stored lava must keep random ticks enabled");
		storage.fluidloggable$setFluidState(1, 0, 0, Fluids.EMPTY.defaultFluidState());
		storage.fluidloggable$setFluidState(1, 0, 0, Fluids.EMPTY.defaultFluidState());
		helper.assertFalse(section.isRandomlyTickingFluids(), "Last lava removal must disable random ticks");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void copyAndPacketReadRebuildRandomTickFlag(final GameTestHelper helper) {
		var section = new LevelChunkSection(helper.getLevel().palettedContainerFactory());
		var storage = (LevelChunkSectionExtension) section;
		storage.fluidloggable$setFluidState(0, 0, 0, Fluids.LAVA.getSource(false));
		var copy = section.copy();
		helper.assertTrue(copy.isRandomlyTickingFluids(), "Section copy must retain stored lava random ticks");
		storage.fluidloggable$setFluidState(0, 0, 0, Fluids.WATER.getSource(false));
		helper.assertTrue(copy.isRandomlyTickingFluids(), "Copies must not share mutable counters");
		var read = new LevelChunkSection(helper.getLevel().palettedContainerFactory());
		var buffer = new FriendlyByteBuf(Unpooled.buffer());
		try {
			copy.write(buffer);
			read.read(buffer);
			helper.assertTrue(read.isRandomlyTickingFluids(), "Packet read must restore stored lava random ticks");
			buffer.clear();
			section.write(buffer);
			read.read(buffer);
			helper.assertFalse(read.isRandomlyTickingFluids(), "Replacing packet contents must discard the old lava count");
		} finally {
			buffer.release();
		}
		((LevelChunkSectionExtension) copy).fluidloggable$copyFluidStatesFrom(storage);
		helper.assertFalse(copy.isRandomlyTickingFluids(), "Replacing a copy must discard the old lava count");
		helper.succeed();
	}
}
