package com.mikarific.carpetthrundlestoneaddition.commands;

import carpet.utils.CommandHelper;
import carpet.utils.Messenger;
import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneSettings;
import com.mikarific.carpetthrundlestoneaddition.mixins.LevelChunkSectionAccessor;
import com.mikarific.carpetthrundlestoneaddition.mixins.PalettedContainerAccessor;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public class PaletteCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("palette")
                .requires((player) -> CommandHelper.canUseCommand(player, CarpetThrundlestoneSettings.commandPalette))
                .then(Commands.literal("bits").executes((context) -> getBits(context.getSource(), context.getSource().getPlayerOrException().blockPosition()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> getBits(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"))))
                )
                .then(Commands.literal("size").executes((context) -> getSize(context.getSource(), context.getSource().getPlayerOrException().blockPosition()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> getSize(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"))))
                )
                .then(Commands.literal("posInfo")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), false, null))
                                .then(Commands.literal("full").executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), true, null))
                                        .then(Commands.argument("block", BlockStateArgument.block(buildContext)).executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), true, BlockStateArgument.getBlock(context, "block").getState())))
                                )
                                .then(Commands.literal("normal").executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), false, null))
                                        .then(Commands.argument("block", BlockStateArgument.block(buildContext)).executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), false, BlockStateArgument.getBlock(context, "block").getState())))
                                )
                        )
                )
        );
    }

    private static int getBits(CommandSourceStack source, BlockPos blockPos) {
        ServerLevel level = source.getLevel();
        LevelChunk chunk = level.getChunkAt(blockPos);
        int sectionIndex = chunk.getSectionIndex(blockPos.getY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) return 0;
        LevelChunkSection subchunk = chunk.getSection(sectionIndex);
        PalettedContainer<BlockState> container = ((LevelChunkSectionAccessor) subchunk).getStates();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        container.write(buf);
        int bits = buf.readUnsignedByte();
        buf.release();

        Messenger.m(source, "w Palette bit size: ", Messenger.s(String.valueOf(bits), "t"));
        return 1;
    }

    private static int getSize(CommandSourceStack source, BlockPos blockPos) {
        ServerLevel level = source.getLevel();
        LevelChunk chunk = level.getChunkAt(blockPos);
        int sectionIndex = chunk.getSectionIndex(blockPos.getY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) return 0;
        LevelChunkSection subchunk = chunk.getSection(sectionIndex);
        PalettedContainer<BlockState> container = ((LevelChunkSectionAccessor) subchunk).getStates();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        container.write(buf);
        int bits = buf.readUnsignedByte();
        String result;
        if (bits == 0) {
            result = "1 (single value)";
        } else if (bits <= 8) {
            result = String.valueOf(buf.readVarInt());
        } else {
            result = Block.BLOCK_STATE_REGISTRY.size() + " (global/MAX)";
        }
        buf.release();

        Messenger.m(source, "w Palette size: ", Messenger.s(result, "t"));
        return 1;
    }
    private static int posInfo(CommandSourceStack source, BlockPos blockPos, boolean isFull, BlockState blockState) {
        try {
            return posInfoImpl(source, blockPos, isFull, blockState);
        } catch (Throwable t) {
            source.sendFailure(Component.literal(t.toString()));
            for (StackTraceElement e : t.getStackTrace()) {
                if (e.getClassName().startsWith("com.mikarific") || e.getClassName().contains("Accessor")) {
                    source.sendFailure(Component.literal("  at " + e));
                }
            }
            t.printStackTrace();
            return 0;
        }
    }
    private static int posInfoImpl(CommandSourceStack source, BlockPos blockPos, boolean isFull, BlockState blockState) {
        ServerLevel level = source.getLevel();
        LevelChunk chunk = level.getChunkAt(blockPos);

        int sectionIndex = chunk.getSectionIndex(blockPos.getY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
            source.sendFailure(Component.literal("Position is outside the world height"));
            return 0;
        }

        LevelChunkSection section = chunk.getSection(sectionIndex);
        PalettedContainer<BlockState> container = ((LevelChunkSectionAccessor) section).getStates();

        PalettedContainer.Data<BlockState> data = ((PalettedContainerAccessor) container).getData();
        BitStorage storage = data.storage();
        int bits = storage.getBits();
        if (bits == 0) {
            source.sendSuccess(() -> Component.literal("Single value palette, no storage"), false);
            return 1;
        }

        int perLong = 64 / bits;
        int index = getIndex(blockPos);
        int cell = index / perLong;
        int offset = (index % perLong) * bits;

        displayBits(source, storage.getRaw()[cell], offset, offset + bits - 1);

        if (isFull) {
            for (int i = 0; i < 4096; i++) {
                if (i / perLong == cell) {
                    final int idx = i;
                    source.sendSuccess(() -> Component.literal(getBlockIndex(idx, blockPos).toString()), false);
                }
            }
        }
        return 1;
    }
    private static void displayBits(CommandSourceStack source, long value, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            sb.append(i >= from && i <= to ? "\u00a7c" : "\u00a7f")
                    .append((value >>> i & 1) == 1 ? '1' : '0');
        }
        source.sendSuccess(() -> Component.literal("\u00a78L:" + sb), false);
    }
    private static int getIndex(BlockPos pos) {
        return (pos.getY() & 15) << 8 | (pos.getZ() & 15) << 4 | (pos.getX() & 15);
    }

    private static BlockPos getBlockIndex(int index, BlockPos pos) {
        int x = (pos.getX() & ~15) | (index & 15);
        int y = (pos.getY() & ~15) | ((index >>> 8) & 15);
        int z = (pos.getZ() & ~15) | ((index >>> 4) & 15);
        return new BlockPos(x, y, z);
    }
}
