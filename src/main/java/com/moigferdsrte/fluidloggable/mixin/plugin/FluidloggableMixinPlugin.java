package com.moigferdsrte.fluidloggable.mixin.plugin;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import java.util.List;
import java.util.Set;

public final class FluidloggableMixinPlugin implements IMixinConfigPlugin {
    private static final String BASE = "com.moigferdsrte.fluidloggable.mixin.base.";

    @Override
    public void onLoad(final String mixinPackage) { FluidloggableConfig.load(); }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (mixinClassName.endsWith(".ClientConfigurationPacketListenerMixin")) return true;
        if (FMLEnvironment.getDist() == Dist.CLIENT && FluidloggableConfig.isClientOnlyCompatibilityModeEnabled()) return false;
        if (mixinClassName.startsWith("com.moigferdsrte.fluidloggable.compat.")) return false;
        return !mixinClassName.startsWith(BASE)
                || FluidloggableConfig.isBlockMixinEnabled(mixinClassName.substring(BASE.length()));
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(final String target, final ClassNode node, final String mixin, final IMixinInfo info) {}
    @Override public void postApply(final String target, final ClassNode node, final String mixin, final IMixinInfo info) {}
}
