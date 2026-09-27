package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluids;

public final class FlowingFluidTickGameTest {
	@GameTest(maxTicks = 1)
	public void unsupportedWaterFlowDrainsToAir(final GameTestHelper helper) {
		assertVanillaFlowTick(helper, Fluids.FLOWING_WATER, false);
	}

	@GameTest(maxTicks = 1)
	public void unsupportedLavaFlowDrainsToAir(final GameTestHelper helper) {
		assertVanillaFlowTick(helper, Fluids.FLOWING_LAVA, false);
	}

	@GameTest(maxTicks = 1)
	public void sourceNeighbourUpdatesVanillaWaterLevel(final GameTestHelper helper) {
		assertVanillaFlowTick(helper, Fluids.FLOWING_WATER, true);
	}

	@GameTest(maxTicks = 1)
	public void sourceNeighbourUpdatesVanillaLavaLevel(final GameTestHelper helper) {
		assertVanillaFlowTick(helper, Fluids.FLOWING_LAVA, true);
	}

	@GameTest(maxTicks = 1)
	public void drainingStoredWaterPreservesContainingBlock(final GameTestHelper helper) {
		assertStoredFlowDrains(helper, Fluids.FLOWING_WATER);
	}

	@GameTest(maxTicks = 1)
	public void drainingStoredLavaPreservesContainingBlock(final GameTestHelper helper) {
		assertStoredFlowDrains(helper, Fluids.FLOWING_LAVA);
	}

	private static BlockPos prepareCell(final GameTestHelper helper) {
		final var level = helper.getLevel();
		final BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
		level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (final Direction direction : Direction.Plane.HORIZONTAL) {
			level.setBlock(pos.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		}
		return pos;
	}

	private static void assertVanillaFlowTick(
			final GameTestHelper helper,
			final FlowingFluid fluid,
			final boolean hasSource
	) {
		final var level = helper.getLevel();
		final BlockPos pos = prepareCell(helper);
		final var initial = fluid.getFlowing(1, false);
		level.setBlock(pos, initial.createLegacyBlock(), Block.UPDATE_ALL);
		if (hasSource) {
			level.setBlock(pos.west(), fluid.getSource(false).createLegacyBlock(), Block.UPDATE_ALL);
		}

		// Exercise both setBlockAndUpdate calls, not the stored-fluid HEAD cancellation.
		initial.tick(level, pos, level.getBlockState(pos));

		if (hasSource) {
			final var result = level.getFluidState(pos);
			helper.assertTrue(result.getType().isSame(fluid), "Vanilla flow must retain its fluid type");
			helper.assertTrue(result.getAmount() > initial.getAmount(), "Source neighbour must replenish vanilla flow");
			helper.assertTrue(
					level.getBlockState(pos) == result.createLegacyBlock(),
					"Vanilla block state must match the updated fluid level"
			);
		} else {
			helper.assertTrue(level.getBlockState(pos).isAir(), "Unsupported vanilla flow must drain to air");
			helper.assertTrue(level.getFluidState(pos).isEmpty(), "Draining must not leave stored fluid behind");
		}
		helper.succeed();
	}

	private static void assertStoredFlowDrains(final GameTestHelper helper, final FlowingFluid fluid) {
		final var level = helper.getLevel();
		final BlockPos pos = prepareCell(helper);
		level.setBlock(pos, FluidloggedGameTestBootstrap.testBlock.defaultBlockState(), Block.UPDATE_ALL);
		final var initial = fluid.getFlowing(1, false);
		((LevelExtension) level).fluidloggable$setFluid(pos, initial, Block.UPDATE_ALL);

		initial.tick(level, pos, level.getBlockState(pos));

		helper.assertTrue(
				level.getBlockState(pos).is(FluidloggedGameTestBootstrap.testBlock),
				"Draining stored fluid must preserve the containing block"
		);
		helper.assertTrue(level.getFluidState(pos).isEmpty(), "Unsupported stored flow must drain");
		helper.succeed();
	}
}
