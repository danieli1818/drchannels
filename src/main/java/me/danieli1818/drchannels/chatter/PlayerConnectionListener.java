package me.danieli1818.drchannels.chatter;

import me.danieli1818.drchannels.chat.ChannelChatService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final ChatterManager chatters;
    private final ChannelChatService chat;

    public PlayerConnectionListener(ChatterManager chatters, ChannelChatService chat) {
        this.chatters = chatters;
        this.chat = chat;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        chatters.load(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        chatters.unload(event.getPlayer());
        chat.forget(event.getPlayer());
    }
}
