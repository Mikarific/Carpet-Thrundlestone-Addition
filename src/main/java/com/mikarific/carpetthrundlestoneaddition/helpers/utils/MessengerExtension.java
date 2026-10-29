package com.mikarific.carpetthrundlestoneaddition.helpers.utils;

import carpet.utils.Messenger;
import carpet.utils.Translations;
import com.mikarific.carpetthrundlestoneaddition.mixins.accessors.TranslatableContentsAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableFormatException;

import java.util.ArrayList;
import java.util.List;

public class MessengerExtension extends Messenger {
    public static Component format(String key, Object... args) {
       return format(key, "", args);
    }

    public static Component format(String key, String style, Object... args) {
        TranslatableContentsAccessor contents = (TranslatableContentsAccessor) Component.translatable(key, args).getContents();
        String pattern = Translations.tr(key);

        try {
            List<FormattedText> segments = new ArrayList<>();
            contents.thrundlestone$invokeDecomposeTemplate(pattern, segments::add);

            MutableComponent result = Component.empty();
            for (FormattedText segment : segments) {
                if (segment instanceof Component component) {
                    result.append(component);
                } else {
                    result.append(Component.literal(segment.getString()).withStyle(parseStyle(style)));
                }
            }
            return result;
        } catch (TranslatableFormatException var4) {
            return Component.literal(pattern);
        }
    }
}
