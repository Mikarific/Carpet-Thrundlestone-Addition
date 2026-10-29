package com.mikarific.carpetthrundlestoneaddition.mixins.rules;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneSettings;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(CollectingNeighborUpdater.class)
public class CollectingNeighborUpdaterMixin_asyncFeederSimulator {
    @Shadow @Final private Level level;

    @WrapMethod(method = "addAndRun")
    private void thrundlestone$asyncFeederSimulator(BlockPos blockPos, @Coerce Object neighborUpdates, Operation<Void> original) {
        if (CarpetThrundlestoneSettings.asyncUpdates) {
            MinecraftServer server = level.getServer();
            if (server == null) {
                original.call(blockPos, neighborUpdates);
                return;
            }

            EventLoopGroup eventLoopGroup;
            if (Epoll.isAvailable() && server.isEpollEnabled()) {
                eventLoopGroup = ServerConnectionListener.SERVER_EPOLL_EVENT_GROUP.get();
            } else {
                eventLoopGroup = ServerConnectionListener.SERVER_EVENT_GROUP.get();
            }

            eventLoopGroup.next().execute(() -> original.call(blockPos, neighborUpdates));
        } else {
            original.call(blockPos, neighborUpdates);
        }
    }
}
