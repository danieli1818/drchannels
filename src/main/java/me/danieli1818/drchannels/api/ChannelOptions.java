package me.danieli1818.drchannels.api;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Helpers for reading channel options, tolerant of the shapes produced by both YAML files and
 * {@code /channel create key=value} arguments.
 */
public final class ChannelOptions {

    private ChannelOptions() {
    }

    /**
     * @return the value as a list, accepting either a YAML list or a single scalar value
     */
    public static List<String> stringList(ConfigurationSection options, String key) {
        if (options.isList(key)) {
            return options.getStringList(key);
        }
        final String value = options.getString(key);
        return value == null || value.isBlank() ? List.of() : List.of(value);
    }

    public static double positiveDouble(ConfigurationSection options, String key) throws InvalidChannelException {
        final double value = options.getDouble(key, Double.NaN);
        if (!(value > 0)) {
            throw new InvalidChannelException("'" + key + "' must be a positive number");
        }
        return value;
    }

    public static List<String> nonEmptyList(ConfigurationSection options, String key) throws InvalidChannelException {
        final List<String> values = stringList(options, key);
        if (values.isEmpty()) {
            throw new InvalidChannelException("'" + key + "' must contain at least one value");
        }
        return values;
    }
}
