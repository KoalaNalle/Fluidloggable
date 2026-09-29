package com.moigferdsrte.fluidloggable.mixin.state;

import com.moigferdsrte.fluidloggable.block.FluidContainerShapeSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleWaterloggedBlock.class)
public interface SimpleWaterloggedBlockMixin {
    @Inject(method = "canPlaceLiquid", at = @At("HEAD"), cancellable = true)
    private void fluidloggable$checkBucketShape(final LivingEntity entity, final BlockGetter level,
            final BlockPos pos, final BlockState state, final Fluid fluid,
            final CallbackInfoReturnable<Boolean> cir) {
        if (!FluidContainerShapeSupport.canContainFluid(state, level, pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "placeLiquid", at = @At("HEAD"), cancellable = true)
    private void fluidloggable$checkPlacementShape(final LevelAccessor level, final BlockPos pos,
            final BlockState state, final FluidState fluid, final CallbackInfoReturnable<Boolean> cir) {
        if (!FluidContainerShapeSupport.canContainFluid(state, level, pos)) {
            cir.setReturnValue(false);
        }
    }
}
