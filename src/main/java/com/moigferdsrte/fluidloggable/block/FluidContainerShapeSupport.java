package com.moigferdsrte.fluidloggable.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MangroveRootsBlock;
import net.minecraft.world.level.block.WaterloggedTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Geometry policy shared by flow, placement, buckets and configured containers. */
public final class FluidContainerShapeSupport {
    private FluidContainerShapeSupport() {
    }

    public static boolean canContainFluid(final BlockState state, final BlockGetter level, final BlockPos pos) {
        // Vanilla deliberately waterlogs these full-collision blocks (including copper grates).
        // Do not use SimpleWaterloggedBlock here: our mixins also add it to solid blocks.
        final var block = state.getBlock();
        return block instanceof LeavesBlock
                || block instanceof MangroveRootsBlock
                || block instanceof WaterloggedTransparentBlock
                || block instanceof BarrierBlock
                || !state.isCollisionShapeFullBlock(level, pos);
    }
}
