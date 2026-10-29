package com.mikarific.carpetthrundlestoneaddition.mixins.accessors;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(TranslatableContents.class)
public interface TranslatableContentsAccessor {
    @Invoker("decomposeTemplate")
    void thrundlestone$invokeDecomposeTemplate(String translation, Consumer<FormattedText> partsConsumer);
}
