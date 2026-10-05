package com.mikarific.carpetthrundlestoneaddition.mixins.loggers;

import carpet.logging.LoggerRegistry;
import carpet.utils.Messenger;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MonsterRoomFeature.class)
public class MonsterRoomFeatureMixin_dungeon {
    @Unique private int roomWidth;
    @Unique private int roomHeight;

    @ModifyExpressionValue(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0))
    private int thrundlestone$captureWidth(int original) {
        this.roomWidth = original + 2;
        return original;
    }

    @ModifyExpressionValue(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 1))
    private int thrundlestone$captureHeight(int original) {
        this.roomHeight = original + 2;
        return original;
    }

    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 1, shift = At.Shift.AFTER))
    private void thrundlestone$spawnerAttempt(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext, CallbackInfoReturnable<Boolean> cir) {
        BlockPos spawnerPos = featurePlaceContext.origin();

        if (CarpetThrundlestoneSettings.dungeonLoggerExcludeNonViable) {
            int localX = Math.floorMod(spawnerPos.getX(), 16);
            int localZ = Math.floorMod(spawnerPos.getZ(), 16);
            boolean chestsEscapeChunk = localX <= roomWidth - 2 || localX >= 17 - roomWidth || localZ <= roomHeight - 2 || localZ >= 17 - roomHeight;
            if (!chestsEscapeChunk) return;
        }

        LoggerRegistry.getLogger("dungeons").log((option) -> {
            if (option.equals("all") || option.equals("spawner")) {
                return new Component[]{Messenger.c("g [", Messenger.s("Dungeon", "e"), "g ] Spawner attempted at ", Messenger.tp("l", spawnerPos))};
            }
            return null;
        });
    }

    @Inject(method = "place", at = @At(value = "INVOKE", target = "net/minecraft/world/level/WorldGenLevel.isEmptyBlock(Lnet/minecraft/core/BlockPos;)Z", ordinal = 2))
    private void thrundlestone$chestAttempt(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 0) BlockPos spawnerPos, @Local(ordinal = 1) BlockPos chestPos) {
        if (CarpetThrundlestoneSettings.dungeonLoggerExcludeNonViable) {
            boolean chestAndSpawnerInDifferentChunks = spawnerPos.getX() >> 4 != chestPos.getX() >> 4 || spawnerPos.getZ() >> 4 != chestPos.getZ() >> 4;
            if (!chestAndSpawnerInDifferentChunks) return;

            int chestLocalX = chestPos.getX() & 15;
            int chestLocalZ = chestPos.getZ() & 15;
            boolean chestNotOnChunkBorder = chestLocalX >= 1 && chestLocalX <= 14 && chestLocalZ >= 1 && chestLocalZ <= 14;
            if (!chestNotOnChunkBorder) return;
        }

        LoggerRegistry.getLogger("dungeons").log((option) -> {
            if (option.equals("all") || option.equals("chest")) {
                return new Component[]{Messenger.c("g [", Messenger.s("Dungeon", "e"), "g ] Chest attempted at ", Messenger.tp("l", chestPos))};
            }
            return null;
        });
    }
}
