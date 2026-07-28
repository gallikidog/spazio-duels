package network.minespazio.spazioduels.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colorize(List<String> messages) {
        if (messages == null) return new ArrayList<>();
        List<String> colorized = new ArrayList<>();
        for (String msg : messages) {
            colorized.add(colorize(msg));
        }
        return colorized;
    }

    public static Component toComponent(String legacyText) {
        return LEGACY_SERIALIZER.deserialize(colorize(legacyText));
    }

    public static Component createClickableComponent(String text, String command, String hoverText) {
        Component comp = LEGACY_SERIALIZER.deserialize(colorize(text));
        if (command != null && !command.isEmpty()) {
            comp = comp.clickEvent(ClickEvent.runCommand(command));
        }
        if (hoverText != null && !hoverText.isEmpty()) {
            comp = comp.hoverEvent(HoverEvent.showText(LEGACY_SERIALIZER.deserialize(colorize(hoverText))));
        }
        return comp;
    }
}
