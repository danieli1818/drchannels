package me.danieli1818.drchannels.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Filters chat recipients by channel. Runs at LOW: after EssentialsChat formats the message (LOWEST) and
 * before it fires its own chat events (NORMAL), so {@link EssentialsChatListener} can tag the format.
 */
public final class BukkitChatListener implements Listener {

    private final ChannelChatService chat;
    private final boolean applyStandaloneFormat;

    /**
     * @param applyStandaloneFormat whether to apply the config.yml format (EssentialsChat not installed)
     */
    public BukkitChatListener(ChannelChatService chat, boolean applyStandaloneFormat) {
        this.chat = chat;
        this.applyStandaloneFormat = applyStandaloneFormat;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        chat.route(event, applyStandaloneFormat);
    }
}
