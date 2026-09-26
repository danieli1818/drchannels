package me.danieli1818.drchannels.util;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configurable messages keyed by their path under {@code messages} in config.yml. Templates are
 * colourised once on load; placeholders ({@code {name}}) are substituted per send.
 */
public final class Messages {

    private record State(String prefix, Map<String, List<String>> templates) {
    }

    private volatile State state = new State("", Map.of());

    public void load(@Nullable ConfigurationSection section) {
        final Map<String, List<String>> loaded = new HashMap<>();
        if (section != null) {
            for (final String key : section.getKeys(true)) {
                if (section.isList(key)) {
                    loaded.put(key, section.getStringList(key).stream().map(Text::colorize).toList());
                } else if (section.isString(key)) {
                    loaded.put(key, List.of(Text.colorize(section.getString(key, ""))));
                }
            }
        }
        final List<String> prefixLines = loaded.remove("prefix");
        state = new State(prefixLines == null ? "" : prefixLines.get(0), Map.copyOf(loaded));
    }

    /**
     * Sends a message, prefixing its first line. Missing keys are sent as the raw key so they are easy to spot.
     *
     * @param placeholders alternating placeholder names and values
     */
    public void send(CommandSender recipient, String key, String... placeholders) {
        final State current = state;
        final List<String> lines = current.templates().getOrDefault(key, List.of(key));
        for (int i = 0; i < lines.size(); i++) {
            final String line = apply(lines.get(i), placeholders);
            recipient.sendMessage(i == 0 ? current.prefix() + line : line);
        }
    }

    /**
     * @return the first line of a message with placeholders applied, without prefix
     */
    public String format(String key, String... placeholders) {
        return apply(state.templates().getOrDefault(key, List.of(key)).get(0), placeholders);
    }

    private static String apply(String template, String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be name/value pairs");
        }
        String result = template;
        for (int i = 0; i < placeholders.length; i += 2) {
            result = result.replace('{' + placeholders[i] + '}', placeholders[i + 1]);
        }
        return result;
    }
}
