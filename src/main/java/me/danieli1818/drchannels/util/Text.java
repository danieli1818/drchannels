package me.danieli1818.drchannels.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Text {

    private static final Pattern HEX_COLOR = Pattern.compile("&#([0-9a-fA-F]{6})");

    private Text() {
    }

    /**
     * Translates {@code &} colour codes and {@code &#rrggbb} hex colours to legacy section-sign codes.
     */
    public static String colorize(String text) {
        final String hexed = HEX_COLOR.matcher(text)
                .replaceAll(match -> Matcher.quoteReplacement(ChatColor.of("#" + match.group(1)).toString()));
        return ChatColor.translateAlternateColorCodes('&', hexed);
    }

    /**
     * Escapes text so it is shown literally inside a {@link String#format} chat format.
     */
    public static String escapeFormat(String text) {
        return text.replace("%", "%%");
    }

    /**
     * Compiles a user-facing chat format ({@code {DISPLAYNAME}}, {@code {MESSAGE}}, colours) into a
     * Bukkit chat format. {@code {CHANNEL}} is left in place for the channel tag.
     */
    public static String compileChatFormat(String template) {
        return colorize(escapeFormat(template))
                .replace("{DISPLAYNAME}", "%1$s")
                .replace("{MESSAGE}", "%2$s");
    }
}
