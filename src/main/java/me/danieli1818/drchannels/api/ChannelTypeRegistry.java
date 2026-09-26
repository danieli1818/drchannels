package me.danieli1818.drchannels.api;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of channel types. Other plugins may register their own types from their {@code onEnable};
 * channels are loaded on the first server tick, after every plugin has been enabled.
 */
public final class ChannelTypeRegistry {

    private final Map<String, ChannelType> types = new ConcurrentHashMap<>();

    /**
     * @throws IllegalStateException if a type with this id is already registered
     */
    public void register(String id, ChannelType type) {
        final String key = normalize(id);
        if (types.putIfAbsent(key, type) != null) {
            throw new IllegalStateException("Channel type '" + key + "' is already registered");
        }
    }

    public Optional<ChannelType> find(String id) {
        return Optional.ofNullable(types.get(normalize(id)));
    }

    public Set<String> ids() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(types.keySet()));
    }

    private static String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
