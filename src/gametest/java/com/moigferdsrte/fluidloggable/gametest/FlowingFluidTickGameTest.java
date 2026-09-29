package com.moigferdsrte.fluidloggable.gametest;

import com.moigferdsrte.fluidloggable.block.*;

import com.moigferdsrte.fluidloggable.extension.LevelExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FlowingFluidTickGameTest {
    @GameTest(maxTicks = 220)
    public void denseSwampPlantsSettleAfterNeighbourUpdates(final GameTestHelper helper) {
        final var level = helper.getLevel();
        for (int x = 1; x <= 6; x++) {
            for (int z = 1; z <= 6; z++) {
                final var pos = helper.absolutePos(new BlockPos(x, 2, z));
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                final boolean edge = x == 1 || x == 6 || z == 1 || z == 6;
                final Block block = edge ? Blocks.STONE : ((x + z) % 2 == 0 ? Blocks.SHORT_GRASS : Blocks.MANGROVE_ROOTS);
                level.setBlock(pos, edge ? block.defaultBlockState() : block.defaultBlockState()
                        .setValue(WaterloggableBlockSupport.WATERLOGGED, true), Block.UPDATE_ALL);
            }
        }
        final Runnable assertSettled = () -> {
            for (int x = 2; x <= 5; x++) {
                for (int z = 2; z <= 5; z++) {
                    final var pos = helper.absolutePos(new BlockPos(x, 2, z));
                    helper.assertTrue(level.getBlockState(pos).is((x + z) % 2 == 0 ? Blocks.SHORT_GRASS : Blocks.MANGROVE_ROOTS),
                            "Swamp plants must survive in the pond");
                    helper.assertTrue(level.getFluidState(pos).isSourceOfType(Fluids.WATER), "Pond must stay filled");
                    for (var fluid : new Fluid[]{Fluids.WATER, Fluids.FLOWING_WATER}) {
                        helper.assertFalse(level.getFluidTicks().hasScheduledTick(pos, fluid)
                                        || level.getFluidTicks().willTickThisTick(pos, fluid),
                                "Settled swamp plants must not continually schedule fluid ticks");
                    }
                }
            }
        };
        helper.runAtTickTime(100, () -> {
            assertSettled.run();
            level.setBlock(helper.absolutePos(new BlockPos(1, 2, 3)), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        });
        helper.runAtTickTime(200, () -> {
            assertSettled.run();
            helper.succeed();
        });
    }

	@GameTest(maxTicks = 220)
	public void settledWaterloggedChannelStopsSchedulingTicks(final GameTestHelper helper) {
		assertChannelSettlesAndDrains(helper, Fluids.WATER, Blocks.SHORT_GRASS, 120, 200);
	}

	@GameTest(maxTicks = 620)
	public void settledLavaloggedChannelStopsSchedulingTicks(final GameTestHelper helper) {
		assertChannelSettlesAndDrains(helper, Fluids.LAVA, FluidloggedGameTestBootstrap.testBlock, 300, 600);
	}

	private static void assertChannelSettlesAndDrains(
			final GameTestHelper helper, final FlowingFluid fluid, final Block container,
			final int settleTick, final int drainTick
	) {
		final var level = helper.getLevel();
		for (int x = 1; x <= 6; x++) {
			for (int z = 2; z <= 4; z++) {
				for (int y = 1; y <= 2; y++) {
					level.setBlock(helper.absolutePos(new BlockPos(x, y, z)),
							Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
		}
		final BlockPos first = helper.absolutePos(new BlockPos(3, 2, 3));
		final BlockPos second = helper.absolutePos(new BlockPos(4, 2, 3));
		level.setBlock(first.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(second.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(first, container.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(second, container.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(first.west(), fluid.getSource(false).createLegacyBlock(), Block.UPDATE_ALL);
		helper.runAtTickTime(settleTick, () -> {
			for (BlockPos pos : new BlockPos[] {first, second}) {
				helper.assertTrue(level.getBlockState(pos).is(container), "Flow must preserve the container");
				helper.assertTrue(level.getFluidState(pos).getType().isSame(fluid), "Channel must remain filled");
				helper.assertFalse(level.getFluidState(pos).isSource(), "Channel must contain flowing fluid");
				for (var type : new Fluid[] {fluid.getSource(), fluid.getFlowing()}) {
					helper.assertFalse(level.getFluidTicks().hasScheduledTick(pos, type)
							|| level.getFluidTicks().willTickThisTick(pos, type),
							"Settled fluidlogged channel must not continually reschedule fluid ticks");
				}
			}
			level.setBlock(first.west(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		});
		helper.runAtTickTime(drainTick, () -> {
			for (BlockPos pos : new BlockPos[] {first, second}) {
				helper.assertTrue(level.getFluidState(pos).isEmpty(), "Removing the source must wake and drain the channel");
				helper.assertTrue(level.getBlockState(pos).is(container), "Draining must preserve the container");
			}
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 1)
	public void waterAboveWaterloggedSourceDoesNotReplaceItWithFallingFlow(final GameTestHelper helper) {
		final var level = helper.getLevel();
		final BlockPos pos = prepareCell(helper);
		level.setBlock(pos, Blocks.MANGROVE_ROOTS.defaultBlockState()
				.setValue(WaterloggableBlockSupport.WATERLOGGED, true), Block.UPDATE_ALL);
		level.setBlock(pos.above(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);

		level.getFluidState(pos.above()).tick(level, pos.above(), level.getBlockState(pos.above()));

		helper.assertTrue(level.getFluidState(pos).isSourceOfType(Fluids.WATER),
				"Water above a waterlogged source must not replace it with falling flow");
		helper.succeed();
	}

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
