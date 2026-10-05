package com.mikarific.carpetthrundlestoneaddition;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Rule;
import carpet.api.settings.Validator;
import com.mikarific.carpetthrundlestoneaddition.mixins.accessors.CollectingNeighborUpdaterAccessor;
import com.mikarific.carpetthrundlestoneaddition.mixins.accessors.LevelAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.redstone.NeighborUpdater;

import static carpet.api.settings.RuleCategory.*;

public class CarpetThrundlestoneSettings {
    public static final String THRUNDLESTONE = "thrundlestone";

    @Rule(categories = {THRUNDLESTONE, CREATIVE})
    public static boolean asyncBeaconUpdates = false;

    @Rule(categories = {THRUNDLESTONE, CREATIVE})
    public static boolean asyncBeaconUpdatesUpdatesDirectly = false;

    @Rule(categories = {THRUNDLESTONE, COMMAND})
    public static String commandPalette = "ops";

    @Rule(categories = {THRUNDLESTONE})
    public static boolean dungeonLoggerExcludeNonViable = true;

    private static class MaxChainedNeighborUpdatesValidator extends Validator<Integer> {
        @Override
        public Integer validate(CommandSourceStack source, CarpetRule<Integer> currentRule, Integer newValue, String string) {
            if (currentRule.value().equals(newValue) || source == null) return newValue;

            source.getServer().getAllLevels().forEach(level -> {
                NeighborUpdater neighborUpdater = ((LevelAccessor) level).thrundlestone$getNeighborUpdater();

                if (newValue < 0) {
                    ((CollectingNeighborUpdaterAccessor) neighborUpdater).thrundlestone$setMaxChainedNeighborUpdates(source.getServer().getMaxChainedNeighborUpdates());
                } else {
                    ((CollectingNeighborUpdaterAccessor) neighborUpdater).thrundlestone$setMaxChainedNeighborUpdates(newValue);
                }
            });

            return newValue < 0 ? -1 : newValue;
        }
    }
    @Rule(categories = {THRUNDLESTONE, CREATIVE}, options = {"-1", "0", "65535", "1000000", "2147483647"}, strict = false, validators = MaxChainedNeighborUpdatesValidator.class)
    public static int maxChainedNeighborUpdates = -1;
}