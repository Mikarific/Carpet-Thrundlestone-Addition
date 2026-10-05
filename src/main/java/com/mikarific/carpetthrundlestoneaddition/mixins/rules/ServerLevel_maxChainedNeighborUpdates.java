package com.mikarific.carpetthrundlestoneaddition.mixins.rules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public class ServerLevel_maxChainedNeighborUpdates {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getMaxChainedNeighborUpdates()I"))
    private static int thrundlestone$getMaxChainedNeighborUpdates(MinecraftServer instance, Operation<Integer> original) {
        if (CarpetThrundlestoneSettings.maxChainedNeighborUpdates < 0) return original.call(instance);
        return CarpetThrundlestoneSettings.maxChainedNeighborUpdates;
    }
}
