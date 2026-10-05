package com.mikarific.carpetthrundlestoneaddition.mixins.accessors;

import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CollectingNeighborUpdater.class)
public interface CollectingNeighborUpdaterAccessor {
    @Mutable
    @Accessor("maxChainedNeighborUpdates")
    void thrundlestone$setMaxChainedNeighborUpdates(int maxChainedNeighborUpdates);
}