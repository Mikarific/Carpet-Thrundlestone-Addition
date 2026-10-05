package com.mikarific.carpetthrundlestoneaddition.mixins.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CollectingNeighborUpdater.class)
public class CollectingNeighborUpdaterMixin_asyncBlockUpdates {
    @Shadow @Final private Level level;

    @Inject(method = "addAndRun", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/redstone/CollectingNeighborUpdater;runUpdates()V"))
    private void thrundlestone$queueStarted(BlockPos blockPos, @Coerce Object neighborUpdates, CallbackInfo ci) {
        MinecraftServer server = level.getServer();
        if (server == null || server.isSameThread()) return;

        LoggerRegistry.getLogger("asyncBlockUpdates").log((option) -> {
            if (option.equals("all") || option.equals("thread")) {
                return new Component[]{Messenger.c("y Asynchronous block update queue started at ", Messenger.tp("l", blockPos), "y  on thread ", Messenger.s(Thread.currentThread().getName(), "d"))};
            }
            return null;
        });
    }

    @Inject(method = "runUpdates", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/redstone/CollectingNeighborUpdater;count:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void thrundlestone$queueFinished(CallbackInfo ci) {
        MinecraftServer server = level.getServer();
        if (server == null || server.isSameThread()) return;

        LoggerRegistry.getLogger("asyncBlockUpdates").log((option) -> {
            if (option.equals("all") || option.equals("thread")) {
                return new Component[]{Messenger.c("y Asynchronous block update queue finished on thread ", Messenger.s(Thread.currentThread().getName(), "d"))};
            }
            return null;
        });
    }

    @Inject(method = "addAndRun", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;)V"))
    private void thrundlestone$updatesSkipped(BlockPos blockPos, @Coerce Object neighborUpdates, CallbackInfo ci) {
        MinecraftServer server = level.getServer();
        if (server == null || server.isSameThread()) return;

        LoggerRegistry.getLogger("asyncBlockUpdates").log((option) -> {
            if (option.equals("all") || option.equals("skips")) {
                return new Component[]{Messenger.c("r Update skipping occured at ", Messenger.tp("l", blockPos), "r  on thread ", Messenger.s(Thread.currentThread().getName(), "d"))};
            }
            return null;
        });
    }
}
