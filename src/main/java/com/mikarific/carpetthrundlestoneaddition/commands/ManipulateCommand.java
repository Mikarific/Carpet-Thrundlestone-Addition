package com.mikarific.carpetthrundlestoneaddition.commands;

import carpet.utils.CommandHelper;
import carpet.utils.Messenger;
import carpettisaddition.CarpetTISAdditionSettings;
import com.mikarific.carpetthrundlestoneaddition.helpers.settings.CarpetTISConflictSettings;
import com.mikarific.carpetthrundlestoneaddition.mixins.accessors.CollectingNeighborUpdaterAccessor;
import com.mikarific.carpetthrundlestoneaddition.mixins.accessors.LevelAccessor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.redstone.NeighborUpdater;

public class ManipulateCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("manipulate")
                .requires((player) -> CommandHelper.canUseCommand(player, FabricLoader.getInstance().isModLoaded("carpet-tis-addition") ? CarpetTISAdditionSettings.commandManipulate : CarpetTISConflictSettings.commandManipulate))
                .then(Commands.literal("server")
                        .then(Commands.literal("neighbor_updater_counter").executes((context) -> showNeighborUpdaterCounter(context.getSource()))
                                .then(Commands.literal("show").executes((context) -> showNeighborUpdaterCounter(context.getSource())))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", IntegerArgumentType.integer()).executes((context) -> setNeighborUpdaterCounter(context.getSource(), IntegerArgumentType.getInteger(context, "value"))))
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("delta", IntegerArgumentType.integer()).executes((context) -> addNeighborUpdaterCounter(context.getSource(), IntegerArgumentType.getInteger(context, "delta"))))
                                )
                        )
                )
        );
    }

    private static int addNeighborUpdaterCounter(CommandSourceStack source, int delta) {
        NeighborUpdater neighborUpdater = ((LevelAccessor) source.getLevel()).thrundlestone$getNeighborUpdater();
        int count = ((CollectingNeighborUpdaterAccessor) neighborUpdater).thrundlestone$getCount();
        int newValue = count + delta;
        ((CollectingNeighborUpdaterAccessor) neighborUpdater).thrundlestone$setCount(newValue);
        Messenger.m(source, "w Added " + delta + " to neighbor updater counter, current value: " + newValue);
        return 1;
    }

    private static int setNeighborUpdaterCounter(CommandSourceStack source, int value) {
        NeighborUpdater neighborUpdater = ((LevelAccessor) source.getLevel()).thrundlestone$getNeighborUpdater();
        ((CollectingNeighborUpdaterAccessor) neighborUpdater).thrundlestone$setCount(value);
        Messenger.m(source, "w Set neighbor updater counter to " + value);
        return 1;
    }

    private static int showNeighborUpdaterCounter(CommandSourceStack source) {
        NeighborUpdater neighborUpdater = ((LevelAccessor) source.getLevel()).thrundlestone$getNeighborUpdater();
        int count = ((CollectingNeighborUpdaterAccessor) neighborUpdater).thrundlestone$getCount();
        Messenger.m(source, "w Current neighbor updater counter value: " + count);
        return 1;
    }
}
