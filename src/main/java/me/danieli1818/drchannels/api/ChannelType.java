package me.danieli1818.drchannels.api;

import org.bukkit.configuration.ConfigurationSection;

/**
 * A kind of channel (global, local, world, ...). Creates the {@link ChannelScope} for a channel from
 * that channel's configuration section.
 */
@FunctionalInterface
public interface ChannelType {

    /**
     * @param options the channel's configuration section, including common keys such as {@code tag}
     * @throws InvalidChannelException if the type-specific options are missing or invalid
     */
    ChannelScope create(ConfigurationSection options) throws InvalidChannelException;
}
