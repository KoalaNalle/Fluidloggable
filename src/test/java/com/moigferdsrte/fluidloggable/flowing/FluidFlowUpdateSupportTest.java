package com.moigferdsrte.fluidloggable.flowing;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidFlowUpdateSupportTest {
	static {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void unchangedWaterAndLavaDoNotWakeSettledNeighbours() {
		for (var fluid : new net.minecraft.world.level.material.FlowingFluid[] {Fluids.WATER, Fluids.LAVA}) {
			var source = fluid.getSource(false);
			assertFalse(FluidFlowUpdateSupport.shouldReplaceStoredFluid(source, source));
			for (int amount = 1; amount <= 8; amount++) {
				var flow = fluid.getFlowing(amount, false);
				assertFalse(FluidFlowUpdateSupport.shouldReplaceStoredFluid(flow, flow));
				var falling = fluid.getFlowing(amount, true);
				assertFalse(FluidFlowUpdateSupport.shouldReplaceStoredFluid(falling, falling));
			}
		}
	}

	@Test
	void changedLevelAndFallingStateStillPropagate() {
		assertTrue(FluidFlowUpdateSupport.shouldReplaceStoredFluid(
				Fluids.WATER.getFlowing(4, false), Fluids.WATER.getFlowing(5, false)));
		assertTrue(FluidFlowUpdateSupport.shouldReplaceStoredFluid(
				Fluids.WATER.getFlowing(4, false), Fluids.WATER.getFlowing(4, true)));
		assertTrue(FluidFlowUpdateSupport.shouldReplaceStoredFluid(
				Fluids.LAVA.getFlowing(4, false), Fluids.LAVA.getSource(false)));
	}

	@Test
	void emptyContainersFillButOtherFluidsAreNotOverwritten() {
		assertTrue(FluidFlowUpdateSupport.shouldReplaceStoredFluid(
				Fluids.EMPTY.defaultFluidState(), Fluids.WATER.getFlowing(7, false)));
		assertFalse(FluidFlowUpdateSupport.shouldReplaceStoredFluid(
				Fluids.LAVA.getSource(false), Fluids.WATER.getFlowing(7, false)));
		assertFalse(FluidFlowUpdateSupport.shouldReplaceStoredFluid(
				Fluids.WATER.getSource(false), Fluids.LAVA.getFlowing(7, false)));
	}
}
