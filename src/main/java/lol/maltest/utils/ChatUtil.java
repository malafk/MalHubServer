package lol.maltest.utils;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ChatUtil {
    public static @NotNull TextComponent color(String message) {
        return LegacyComponentSerializer.builder().character('&').hexColors().build().deserialize(message);
    }
}
