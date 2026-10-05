package com.mikarific.carpetthrundlestoneaddition.mixins.rules;

import com.mikarific.carpetthrundlestoneaddition.helpers.mixin.CollectingNeighborUpdaterExtension;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CollectingNeighborUpdater.class)
public abstract class CollectingNeighborUpdaterMixin_asyncBeaconUpdates implements CollectingNeighborUpdaterExtension {
    @Unique private final List<Runnable> thrundlestone$afterUpdates = new ArrayList<>();

    @Shadow private int count;

    @Override
    public void thrundlestone$runAfter(Runnable runnable) {
        if (count == 0) {
            runnable.run();
        } else {
            thrundlestone$afterUpdates.add(runnable);
        }
    }

    @Inject(method = "runUpdates", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/redstone/CollectingNeighborUpdater;count:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void thrundlestone$afterRunUpdates(CallbackInfo ci) {
        for (Runnable callback : thrundlestone$afterUpdates) {
            callback.run();
        }
        thrundlestone$afterUpdates.clear();
    }
}
