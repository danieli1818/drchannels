package me.danieli1818.drchannels.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Chat hook used when EssentialsChat is not installed: routes and formats the Bukkit chat event itself.
 */
public final class BukkitChatListener implements Listener {

    private final ChannelChatService chat;

    public BukkitChatListener(ChannelChatService chat) {
        this.chat = chat;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        chat.route(new ChatEventView(event.getPlayer(), event.getRecipients(),
                event::getFormat, event::setFormat, () -> event.setCancelled(true)), true);
    }
}
