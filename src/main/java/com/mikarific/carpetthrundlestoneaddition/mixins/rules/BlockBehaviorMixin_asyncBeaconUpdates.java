package com.mikarific.carpetthrundlestoneaddition.mixins.rules;

import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneAddition;
import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneSettings;
import com.mikarific.carpetthrundlestoneaddition.helpers.mixin.CollectingNeighborUpdaterExtension;
import com.mikarific.carpetthrundlestoneaddition.mixins.accessors.LevelAccessor;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(BlockBehaviour.class)
public class BlockBehaviorMixin_asyncBeaconUpdates {
    @Unique private static final Map<ResourceKey<Level>, Set<Long>> POWERED_BEACONS = new HashMap<>();

    @Inject(method = "neighborChanged", at = @At("HEAD"))
    private void thrundlestone$beaconPowered(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean movedByPiston, CallbackInfo ci) {
        Block beacon = state.getBlock();
        if (!(beacon instanceof BeaconBlock)) return;

        Set<Long> beacons = POWERED_BEACONS.computeIfAbsent(level.dimension(), ignored -> new HashSet<>());

        long posLong = pos.asLong();
        if (!level.hasNeighborSignal(pos)) {
            beacons.remove(posLong);
            return;
        }
        if (!CarpetThrundlestoneSettings.asyncBeaconUpdates) return;

        MinecraftServer server = level.getServer();
        if (server == null) return;

        if (!beacons.add(posLong)) return;

        EventLoopGroup eventLoopGroup;
        if (Epoll.isAvailable() && server.isEpollEnabled()) {
            eventLoopGroup = ServerConnectionListener.SERVER_EPOLL_EVENT_GROUP.get();
        } else {
            eventLoopGroup = ServerConnectionListener.SERVER_EVENT_GROUP.get();
        }

        CollectingNeighborUpdater neighborUpdater = (CollectingNeighborUpdater) ((LevelAccessor) level).thrundlestone$getNeighborUpdater();
        ((CollectingNeighborUpdaterExtension) neighborUpdater).thrundlestone$runAfter(() -> eventLoopGroup.next().execute(() -> {
            try {
                if (CarpetThrundlestoneSettings.asyncBeaconUpdatesUpdatesDirectly) {
                    level.updateNeighborsAt(pos, beacon);
                } else {
                    level.updateNeighbourForOutputSignal(pos, beacon);
                }
            } catch(Throwable e) {
                CarpetThrundlestoneAddition.LOGGER.error("Failed to update beacon updates", e);
            } finally {
                CarpetThrundlestoneAddition.LOGGER.info("Collecting beacon updates complete.");
            }
        }));
    }
}
