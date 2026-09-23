/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.event.ClickEvent
 *  net.kyori.adventure.text.event.HoverEvent
 *  net.kyori.adventure.text.event.HoverEventSource
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  org.bukkit.ChatColor
 */
package network.minespazio.spazioduels.util;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class TextUtil {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public static String colorize(String message) {
        if (message == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)message);
    }

    public static List<String> colorize(List<String> messages) {
        if (messages == null) {
            return new ArrayList<String>();
        }
        ArrayList<String> colorized = new ArrayList<String>();
        for (String msg : messages) {
            colorized.add(TextUtil.colorize(msg));
        }
        return colorized;
    }

    public static Component toComponent(String legacyText) {
        return LEGACY_SERIALIZER.deserialize(TextUtil.colorize(legacyText));
    }

    public static Component createClickableComponent(String text, String command, String hoverText) {
        TextComponent comp = LEGACY_SERIALIZER.deserialize(TextUtil.colorize(text));
        if (command != null && !command.isEmpty()) {
            comp = (TextComponent)comp.clickEvent(ClickEvent.runCommand((String)command));
        }
        if (hoverText != null && !hoverText.isEmpty()) {
            comp = (TextComponent)comp.hoverEvent((HoverEventSource)HoverEvent.showText((Component)LEGACY_SERIALIZER.deserialize(TextUtil.colorize(hoverText))));
        }
        return comp;
    }
}

