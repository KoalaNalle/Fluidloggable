package com.moigferdsrte.fluidloggable.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
	// NeoForge delegates the vanilla overload to this overload with additional geometry renderers.
	@Redirect(
		method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;"
                + "Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;"
                + "Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$useRegionFluidState(
		final BlockState blockState,
		@Local(argsOnly = true, name = "region") final RenderSectionRegion region,
		@Local(name = "pos") final BlockPos pos
	) {
		return region.getFluidState(pos);
	}
}
