package com.mikarific.carpetthrundlestoneaddition;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.mikarific.carpetthrundlestoneaddition.commands.PaletteCommand;
import com.mikarific.carpetthrundlestoneaddition.logging.ThrundlestoneAdditionLoggerRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class CarpetThrundlestoneAddition implements CarpetExtension, ModInitializer {
    public static final String MOD_ID = "carpet-thrundlestone-addition";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(new CarpetThrundlestoneAddition());
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(CarpetThrundlestoneSettings.class);
    }

    @Override
    public void registerLoggers() {
        ThrundlestoneAdditionLoggerRegistry.registerLoggers();
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, final CommandBuildContext buildContext) {
        PaletteCommand.register(dispatcher);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        InputStream langFile = CarpetThrundlestoneAddition.class.getClassLoader().getResourceAsStream("assets/carpet-thrundlestone-addition/lang/%s.json".formatted(lang));
        if (langFile == null) return Collections.emptyMap();
        String jsonData;
        try {
            jsonData = IOUtils.toString(langFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
        return new GsonBuilder().create().fromJson(jsonData, new TypeToken<Map<String, String>>(){}.getType());
    }

    @Override
    public String version() {
        return MOD_ID;
    }
}