package com.moigferdsrte.fluidloggable.flowing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidFlowUpdateSupport {
	private FluidFlowUpdateSupport() {
	}

	/** An unchanged flow must not wake its neighbour again: adjacent containers can form a tick loop. */
	public static boolean shouldReplaceStoredFluid(final FluidState current, final FluidState target) {
		return current != target && (current.isEmpty() || current.getType().isSame(target.getType()));
	}

	public static void scheduleAdjacentFluidTicks(
			final ServerLevel level,
			final BlockPos changedPos
	) {
		scheduleFluidTick(level, changedPos);
		for (Direction direction : Direction.values()) {
			scheduleFluidTick(level, changedPos.relative(direction));
		}
	}

	private static void scheduleFluidTick(final ServerLevel level, final BlockPos pos) {
		final FluidState fluidState = level.getFluidState(pos);
		if (!fluidState.isEmpty() && isSupportedFluid(fluidState)) {
			level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
		}
	}

	private static boolean isSupportedFluid(final FluidState state) {
		return state.getType().isSame(Fluids.WATER)
				|| state.getType().isSame(Fluids.LAVA);
	}
}
