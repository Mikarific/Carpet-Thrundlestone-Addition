package com.mikarific.carpetthrundlestoneaddition.helpers.settings;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.*;
import static com.mikarific.carpetthrundlestoneaddition.CarpetThrundlestoneSettings.THRUNDLESTONE;

public class CarpetTISConflictSettings {
    @Rule(categories = {THRUNDLESTONE, COMMAND, EXPERIMENTAL})
    public static String commandManipulate = "false";
}