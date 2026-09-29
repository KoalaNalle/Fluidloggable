package com.moigferdsrte.fluidloggable.block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

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
    public void fullCubesWithoutAnExplicitExceptionCannotAcquireFluid(final GameTestHelper helper) {
        final var level = helper.getLevel();
        final var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        final var candidates = BuiltInRegistries.BLOCK.stream()
                .map(Block::defaultBlockState)
                .filter(state -> state.hasProperty(LavaloggableBlockSupport.LAVALOGGED))
                .filter(state -> state.isCollisionShapeFullBlock(level, pos))
                .map(state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()).sorted().toList();
        Fluidloggable.LOGGER.info("Full collision cubes with fluid properties: {}", candidates);
        for (Block block : new Block[]{Blocks.BEACON, Blocks.STONE, Blocks.BRICKS, Blocks.IRON_BLOCK, FluidloggedGameTestBootstrap.defaultConfiguredCube}) {
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
    public void intentionalFullContainersRetainFluidlogging(final GameTestHelper helper) {
        final var level = helper.getLevel();
        final var containers = BuiltInRegistries.BLOCK.stream()
                .filter(block -> block == Blocks.SPAWNER || block == Blocks.PISTON
                        || block == Blocks.STICKY_PISTON || block == Blocks.VAULT
                        || block instanceof net.minecraft.world.level.block.ShulkerBoxBlock)
                .toList();
        int index = 0;
        for (Block block : containers) {
            final var pos = helper.absolutePos(new BlockPos(1 + index % 6, 2, 1 + index / 6));
            index++;
            final var state = block.defaultBlockState();
            level.setBlock(pos, state, Block.UPDATE_ALL);
            final String name = BuiltInRegistries.BLOCK.getKey(block).toString();
            helper.assertTrue(state.isCollisionShapeFullBlock(level, pos), "Fixture must exercise full collision: " + name);
            for (var fluid : new net.minecraft.world.level.material.Fluid[]{Fluids.WATER, Fluids.LAVA}) {
                helper.assertTrue(FluidloggedBlockStateSupport.canStoreFluid(level, pos, state, fluid),
                        "Intentional full container must accept stored fluid: " + name);
                helper.assertTrue(FluidloggedBlockStateSupport.canPlaceFluid(level, pos, state, fluid),
                        "Intentional full container must accept bucket placement: " + name);
            }
            helper.assertTrue(block instanceof SimpleWaterloggedBlock, "Fixture must provide the vanilla water bucket route");
            final var container = (SimpleWaterloggedBlock) block;
            helper.assertTrue(container.canPlaceLiquid(null, level, pos, state, Fluids.WATER),
                    "Vanilla bucket route must remain available: " + name);
            helper.assertTrue(container.placeLiquid(level, pos, state, Fluids.WATER.getSource(false)),
                    "Water placement must succeed: " + name);
            helper.assertTrue(level.getBlockState(pos).is(block), "Filling must preserve the block: " + name);
            helper.assertTrue(level.getFluidState(pos).isSourceOfType(Fluids.WATER),
                    "Filled block must contain water: " + name);
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
