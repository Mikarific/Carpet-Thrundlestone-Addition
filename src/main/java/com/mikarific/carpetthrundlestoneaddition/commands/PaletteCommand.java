package com.mikarific.carpetthrundlestoneaddition.commands;

import carpet.utils.CommandHelper;
import carpet.utils.Messenger;
import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneAddition;
import com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneSettings;
import com.mikarific.carpetthrundlestoneaddition.mixins.accessors.PalettedContainerAccessor;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.util.ArrayList;
import java.util.List;

public class PaletteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("palette")
                .requires((player) -> CommandHelper.canUseCommand(player, CarpetThrundlestoneSettings.commandPalette))
                .then(Commands.literal("bits").executes((context) -> getBits(context.getSource(), context.getSource().getPlayerOrException().blockPosition()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> getBits(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"))))
                )
                .then(Commands.literal("size").executes((context) -> getSize(context.getSource(), context.getSource().getPlayerOrException().blockPosition()))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> getSize(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"))))
                )
                .then(Commands.literal("posInfo")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), false))
                                .then(Commands.literal("full").executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), true)))
                                .then(Commands.literal("normal").executes((context) -> posInfo(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos"), false)))
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
        PalettedContainer<BlockState> container = subchunk.getStates();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        container.write(buf);
        int bits = buf.readUnsignedByte();
        buf.release();

        Messenger.m(source, "g Palette bit size: ", Messenger.s(String.valueOf(bits), "wb"));
        return 1;
    }

    private static int getSize(CommandSourceStack source, BlockPos blockPos) {
        ServerLevel level = source.getLevel();
        LevelChunk chunk = level.getChunkAt(blockPos);
        int sectionIndex = chunk.getSectionIndex(blockPos.getY());
        if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) return 0;
        LevelChunkSection subchunk = chunk.getSection(sectionIndex);
        PalettedContainer<BlockState> container = subchunk.getStates();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        container.write(buf);
        int bits = buf.readUnsignedByte();
        if (bits == 0) {
            Messenger.m(source, "g Palette size: ", Messenger.s("1", "wb"));
        } else if (bits <= 8) {
            Messenger.m(source, "g Palette size: ", Messenger.s(String.valueOf(buf.readVarInt()), "wb"));
        } else {
            Messenger.m(source, "g Palette size ", "wb MAX", "g  aka ", Messenger.s(String.valueOf(Block.BLOCK_STATE_REGISTRY.size()), "wb"));
        }
        buf.release();
        return 1;
    }

    private static int posInfo(CommandSourceStack source, BlockPos blockPos, boolean isFull) {
        try {
            ServerLevel level = source.getLevel();
            LevelChunk chunk = level.getChunkAt(blockPos);

            int sectionIndex = chunk.getSectionIndex(blockPos.getY());
            if (sectionIndex < 0 || sectionIndex >= chunk.getSections().length) {
                Messenger.m(source, "r Position is outside the world height");
                return 0;
            }

            LevelChunkSection section = chunk.getSection(sectionIndex);
            PalettedContainer<BlockState> container = section.getStates();
            @SuppressWarnings("unchecked")
            PalettedContainer.Data<BlockState> data = ((PalettedContainerAccessor<BlockState>) container).thrundlestone$getData();

            BitStorage storage = data.storage();
            int bits = storage.getBits();
            if (bits == 0) {
                Messenger.m(source, "g Single value palette, no storage");
                return 1;
            }

            int perLong = 64 / bits;
            int index = getIndex(blockPos);
            int cell = index / perLong;
            int offset = (index % perLong) * bits;

            displayBits(source, storage.getRaw()[cell], offset, offset + bits - 1);

            if (isFull) {
                List<Object> fields = new ArrayList<>();

                for (int i = 0; i < 4096; i++) {
                    if (i / perLong == cell) {
                        fields.add(Messenger.tp("l", getBlockIndex(i, blockPos)));
                        fields.add("g  ");
                    }
                }

                Messenger.m(source, fields.toArray());
            }

            return 1;
        } catch (Throwable t) {
            Messenger.m(source, "r Failed to get posInfo");
            CarpetThrundlestoneAddition.LOGGER.error("Failed to get posInfo", t);
            return 0;
        }
    }

    private static void displayBits(CommandSourceStack source, long value, int from, int to) {
        Object[] fields = new String[65];

        fields[0] = "f L:";

        for (int i = 0; i < 64; i++) {
            fields[i + 1] = (i >= from && i <= to ? "r " : "g ") + ((value >>> i & 1) == 1 ? '1' : '0');
        }

        Messenger.m(source, fields);
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
