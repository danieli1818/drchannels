package me.danieli1818.drchannels.chat;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Minimal view over a chat event, so routing logic is shared between Bukkit's chat event and
 * EssentialsChat's chat events (which share no common type).
 *
 * @param recipients the mutable recipient set
 * @param format     getter for a {@link String#format} chat format ({@code %1$s} display name, {@code %2$s} message)
 */
public record ChatEventView(
        Player player,
        Set<Player> recipients,
        Supplier<String> format,
        Consumer<String> formatSetter,
        Runnable canceller
) {
}
