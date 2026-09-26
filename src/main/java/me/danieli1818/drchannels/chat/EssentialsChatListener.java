package me.danieli1818.drchannels.chat;

import net.essentialsx.api.v2.events.chat.ChatEvent;
import net.essentialsx.api.v2.events.chat.GlobalChatEvent;
import net.essentialsx.api.v2.events.chat.LocalChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Chat hook used when EssentialsChat is installed. EssentialsChat fires these events after applying its
 * own formatting (group formats, prefixes, placeholders) and copies recipient and format changes back
 * to the real chat event, on both Spigot and Paper. This is the only class that references EssentialsX.
 */
public final class EssentialsChatListener implements Listener {

    private final ChannelChatService chat;

    public EssentialsChatListener(ChannelChatService chat) {
        this.chat = chat;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGlobalChat(GlobalChatEvent event) {
        route(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLocalChat(LocalChatEvent event) {
        route(event);
    }

    private void route(ChatEvent event) {
        chat.route(new ChatEventView(event.getPlayer(), event.getRecipients(),
                event::getFormat, event::setFormat, () -> event.setCancelled(true)), false);
    }
}
