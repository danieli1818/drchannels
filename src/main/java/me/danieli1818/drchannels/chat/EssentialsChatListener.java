package me.danieli1818.drchannels.chat;

import net.essentialsx.api.v2.events.chat.ChatEvent;
import net.essentialsx.api.v2.events.chat.GlobalChatEvent;
import net.essentialsx.api.v2.events.chat.LocalChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Adds the channel tag to EssentialsChat's format, so its group formats, prefixes and placeholders are kept.
 * EssentialsChat copies format changes from these events back to the real chat event on both Spigot and
 * Paper. Recipients are not touched here: on Paper these events expose an unmodifiable recipient set, so
 * filtering happens in {@link BukkitChatListener}. This is the only class that references EssentialsX.
 */
public final class EssentialsChatListener implements Listener {

    private final ChannelChatService chat;

    public EssentialsChatListener(ChannelChatService chat) {
        this.chat = chat;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGlobalChat(GlobalChatEvent event) {
        tag(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLocalChat(LocalChatEvent event) {
        tag(event);
    }

    private void tag(ChatEvent event) {
        event.setFormat(chat.tagFormat(event.getPlayer(), event.getFormat()));
    }
}
