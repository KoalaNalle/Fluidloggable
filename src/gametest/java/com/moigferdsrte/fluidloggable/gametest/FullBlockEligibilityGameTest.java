package com.moigferdsrte.fluidloggable.gametest;

import com.moigferdsrte.fluidloggable.block.*;

import com.moigferdsrte.fluidloggable.Fluidloggable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.Fluids;

public final class FullBlockEligibilityGameTest {
    @GameTest(maxTicks = 1)
    public void fullCubesCannotAcquireFluid(final GameTestHelper helper) {
        final var level = helper.getLevel();
        final var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        final var candidates = BuiltInRegistries.BLOCK.stream()
                .map(Block::defaultBlockState)
                .filter(state -> state.hasProperty(LavaloggableBlockSupport.LAVALOGGED))
                .filter(state -> state.isCollisionShapeFullBlock(level, pos))
                .map(state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()).sorted().toList();
        Fluidloggable.LOGGER.info("Full collision cubes with fluid properties: {}", candidates);
        for (Block block : new Block[]{Blocks.BEACON, Blocks.SPAWNER, Blocks.SHULKER_BOX, Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.VAULT, FluidloggedGameTestBootstrap.defaultConfiguredCube}) {
            final var state = block.defaultBlockState();
            helper.assertTrue(state.isCollisionShapeFullBlock(level, pos), "Fixture must be a full cube");
            helper.assertFalse(FluidloggedBlockStateSupport.canStoreFluid(level, pos, state, Fluids.WATER),
                    "Full cube must reject stored water: " + BuiltInRegistries.BLOCK.getKey(block));
            helper.assertFalse(FluidloggedBlockStateSupport.canStoreFluid(level, pos, state, Fluids.LAVA),
                    "Full cube must reject stored lava");
            if (block instanceof SimpleWaterloggedBlock container) {
                helper.assertFalse(container.canPlaceLiquid(null, level, pos, state, Fluids.WATER),
                        "Vanilla bucket route must also reject the full cube");
                helper.assertFalse(container.placeLiquid(level, pos, state, Fluids.WATER.getSource(false)),
                        "Dispenser/liquid placement must also reject the full cube");
            }
        }
        helper.succeed();
    }

    @GameTest(maxTicks = 1)
    public void partialBlocksAndVanillaPorousBlocksRemainEligible(final GameTestHelper helper) {
        final var level = helper.getLevel();
        final var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        for (Block block : new Block[]{Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.OAK_STAIRS,
                Blocks.OAK_SLAB, Blocks.END_PORTAL_FRAME, Blocks.MANGROVE_ROOTS, Blocks.OAK_LEAVES,
                BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("minecraft:copper_grate")),
                BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse("minecraft:waxed_copper_grate")), Blocks.BARRIER}) {
            helper.assertTrue(FluidloggedBlockStateSupport.canStoreFluid(level, pos, block.defaultBlockState(), Fluids.WATER),
                    "Partial blocks and vanilla porous blocks must retain waterlogging: " + BuiltInRegistries.BLOCK.getKey(block));
        }
        helper.succeed();
    }
}
