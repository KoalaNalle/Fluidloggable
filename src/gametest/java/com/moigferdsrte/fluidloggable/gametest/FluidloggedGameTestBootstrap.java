package com.moigferdsrte.fluidloggable.gametest;

import com.moigferdsrte.fluidloggable.block.*;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

@Mod("fluidloggable_gametest")
public final class FluidloggedGameTestBootstrap {
    public FluidloggedGameTestBootstrap(final IEventBus bus) {
        bus.addListener(this::registerBlocks);
        NeoForgeGameTests.register(bus);
    }
	private static final ResourceKey<Block> TEST_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "fluid_channel")
	);
	private static final ResourceKey<Block> TAGGED_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "tagged_channel")
	);
	private static final ResourceKey<Block> LATE_CONFIGURED_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "late_configured_channel")
	);

	public static Block defaultConfiguredCube;
	public static Block testBlock;
	public static Block taggedBlock;
	public static Block lateConfiguredBlock;

    private void registerBlocks(final RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.BLOCK)) return;
        final var cubeKey = ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath("biomesoplenty", "fluidloggable_test_cube"));
        defaultConfiguredCube = Registry.register(BuiltInRegistries.BLOCK, cubeKey,
                new Block(BlockBehaviour.Properties.of().setId(cubeKey)));
		testBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				TEST_BLOCK_KEY,
				new FluidChannelBlock()
		);
		lateConfiguredBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				LATE_CONFIGURED_BLOCK_KEY,
				new Block(BlockBehaviour.Properties.of().noCollision().setId(LATE_CONFIGURED_BLOCK_KEY))
		);
		FluidloggableConfig.setFluidloggableBlockIds(List.of(
				TAGGED_BLOCK_KEY.identifier().toString(),
				LATE_CONFIGURED_BLOCK_KEY.identifier().toString()
		));
		taggedBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				TAGGED_BLOCK_KEY,
				new Block(BlockBehaviour.Properties.of().noCollision().setId(TAGGED_BLOCK_KEY))
		);
	}

	private static final class FluidChannelBlock extends Block {
		private FluidChannelBlock() {
			super(BlockBehaviour.Properties.of().noCollision().setId(TEST_BLOCK_KEY));
			this.registerDefaultState(this.stateDefinition.any()
					.setValue(BlockStateProperties.WATERLOGGED, false)
					.setValue(BlockStateProperties.POWERED, false));
		}

		@Override
		protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
			builder.add(BlockStateProperties.WATERLOGGED, BlockStateProperties.POWERED);
		}
	}
}
