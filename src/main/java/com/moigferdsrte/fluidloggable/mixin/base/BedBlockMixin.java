package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin extends AbstractBedBlock implements SimpleWaterloggedBlock {
	protected BedBlockMixin(final BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fluidloggable$defaultToDry(final DyeColor color, final BlockBehaviour.Properties properties, final CallbackInfo ci) {
		this.registerDefaultState(FluidloggedBlockStateSupport.defaultToDry(this.defaultBlockState()));
	}

	@Override
	public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
		return FluidloggedBlockStateSupport.withPlacementFluid(super.getStateForPlacement(context), context);
	}

	@Override
	public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @Nullable LivingEntity by, @NonNull ItemStack itemStack) {
		BlockPos headPos = pos.relative(state.getValue(BedBlock.FACING));
		((LevelExtension) level).fluidloggable$setBlockAndInsertFluidIfPossible(
				headPos,
				FluidloggedBlockStateSupport.withFluid(
						state.setValue(BedBlock.PART, BedPart.HEAD),
						level.getFluidState(headPos)
				),
				Block.UPDATE_ALL
		);
	}

	@Override
	protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
		return FluidloggedBlockStateSupport.getFluidState(state, super.getFluidState(state));
	}

	@Override
	protected @NonNull BlockState updateShape(
            @NonNull BlockState state,
            @NonNull LevelReader level,
            @NonNull ScheduledTickAccess ticks,
            @NonNull BlockPos pos,
            @NonNull Direction directionToNeighbour,
            @NonNull BlockPos neighbourPos,
            @NonNull BlockState neighbourState,
            @NonNull RandomSource random) {
		FluidloggedBlockStateSupport.scheduleFluidTick(level, ticks, pos, state);
		return FluidloggedBlockStateSupport.preserveFluidlogged(state, super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		if (fluidloggable$isComfortsBlock()) {
			return;
		}
		builder.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
	}

	@Unique
    private boolean fluidloggable$isComfortsBlock() {
		return ((Object) this).getClass().getName().startsWith("com.illusivesoulworks.comforts.common.block.");
	}
}
