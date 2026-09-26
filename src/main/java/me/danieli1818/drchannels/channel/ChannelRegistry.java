package me.danieli1818.drchannels.channel;

import me.danieli1818.drchannels.api.InvalidChannelException;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Holds the loaded channels as an immutable snapshot that is swapped atomically, so the async chat
 * thread never sees a partially updated state. All mutations happen on the main thread.
 */
public final class ChannelRegistry {

    private record Snapshot(Map<String, Channel> byId, Map<String, Channel> byName, @Nullable Channel defaultChannel) {
        static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), null);
    }

    private final ChannelConfig config;
    private final Logger logger;
    private volatile Snapshot snapshot = Snapshot.EMPTY;
    private @Nullable String defaultId;
    private Runnable changeListener = () -> {
    };

    public ChannelRegistry(ChannelConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    /**
     * @param listener invoked on the main thread after every reload, create or delete
     */
    public void onChange(Runnable listener) {
        this.changeListener = listener;
    }

    public void reload(@Nullable String defaultId) {
        this.defaultId = defaultId == null ? null : defaultId.toLowerCase(Locale.ROOT);
        publish(config.load(this.defaultId));
        if (this.defaultId == null || snapshot.defaultChannel() == null) {
            logger.severe("Default channel '" + this.defaultId + "' is not defined in channels.yml; players will have no fallback channel.");
        }
    }

    /**
     * Validates, persists and registers a new channel.
     *
     * @throws IllegalArgumentException if a channel with this id already exists
     */
    public Channel create(String id, ConfigurationSection definition) throws InvalidChannelException, IOException {
        if (snapshot.byId().containsKey(id) || config.contains(id)) {
            throw new IllegalArgumentException("Channel '" + id + "' already exists");
        }
        final Channel channel = config.parse(id, definition, id.equals(defaultId));
        config.save(id, definition);
        final Map<String, Channel> channels = new LinkedHashMap<>(snapshot.byId());
        channels.put(id, channel);
        publish(channels);
        return channel;
    }

    public void delete(Channel channel) throws IOException {
        config.remove(channel.id());
        final Map<String, Channel> channels = new LinkedHashMap<>(snapshot.byId());
        channels.remove(channel.id());
        publish(channels);
    }

    /**
     * @param name a channel id or alias, case-insensitive
     */
    public Optional<Channel> find(String name) {
        return Optional.ofNullable(snapshot.byName().get(name.toLowerCase(Locale.ROOT)));
    }

    public Optional<Channel> byId(String id) {
        return Optional.ofNullable(snapshot.byId().get(id));
    }

    public Collection<Channel> all() {
        return snapshot.byId().values();
    }

    public Optional<Channel> defaultChannel() {
        return Optional.ofNullable(snapshot.defaultChannel());
    }

    public boolean isDefault(Channel channel) {
        return channel.id().equals(defaultId);
    }

    private void publish(Map<String, Channel> channels) {
        final Map<String, Channel> byName = new HashMap<>(channels);
        for (final Channel channel : channels.values()) {
            for (final String alias : channel.aliases()) {
                final Channel existing = byName.putIfAbsent(alias, channel);
                if (existing != null && existing != channel) {
                    logger.warning("Alias '" + alias + "' of channel '" + channel.id() + "' is already used by '" + existing.id() + "'; ignoring it.");
                }
            }
        }
        snapshot = new Snapshot(
                Collections.unmodifiableMap(new LinkedHashMap<>(channels)),
                Map.copyOf(byName),
                defaultId == null ? null : channels.get(defaultId));
        changeListener.run();
    }
}
