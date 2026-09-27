package com.moigferdsrte.fluidloggable.client;

import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public final class FluidloggableClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(final ClientGameTestContext context) {
		try (final var world = context.worldBuilder().create()) {
			final var connection = world.getConnection();
			connection.waitForChunksDownload();
			final var waterPos = world.getServer().computeOnServer(server -> {
				final var level = connection.getServerLevel();
				final var pos = connection.getServerPlayer().blockPosition().offset(0, 0, 3);
				final var lavaPos = pos.east(3);
				level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
				level.setBlock(lavaPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
				level.setBlock(pos, Blocks.TORCH.defaultBlockState(), Block.UPDATE_ALL);
				level.setBlock(lavaPos, Blocks.TORCH.defaultBlockState(), Block.UPDATE_ALL);
				final var fluids = (LevelExtension) level;
				fluids.fluidloggable$setFluid(pos, Fluids.WATER.getSource(false), Block.UPDATE_ALL);
				fluids.fluidloggable$setFluid(lavaPos, Fluids.LAVA.getSource(false), Block.UPDATE_ALL);
				return pos;
			});
			final var lavaPos = waterPos.east(3);
			context.runOnClient(client -> {
				final var player = connection.getClientPlayer();
				player.setYRot(0.0F);
				player.setYHeadRot(0.0F);
				player.setXRot(20.0F);
			});
			context.waitFor(client -> client.level != null
					&& client.level.getBlockState(waterPos).is(Blocks.TORCH)
					&& client.level.getFluidState(waterPos).isSourceOfType(Fluids.WATER)
					&& client.level.getBlockState(lavaPos).is(Blocks.TORCH)
					&& client.level.getFluidState(lavaPos).isSourceOfType(Fluids.LAVA));
			connection.waitForChunksRender();
			context.takeScreenshot("fluidloggable-world-startup");
		}
	}
}
