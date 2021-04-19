package me.danieli1818.drchannels.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.earth2me.essentials.Essentials;
import com.google.common.collect.ImmutableCollection;

import me.danieli1818.drchannels.channels.management.ChannelsManager;

public class ChannelsListener implements Listener {
	
	private Essentials ess;
	private static ChannelsListener instance;
	
	private ChannelsListener(Essentials ess) {
		this.ess = ess;
	}
	
	public static ChannelsListener getInstance(Essentials ess) {
		if (instance == null) {
			instance = new ChannelsListener(ess);
		}
		return instance;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		ChannelsManager.getInstance().joinAllPermittedChannels(player);
		ChannelsManager.getInstance().joinDefaultChannelIfNoneCurrentChannel(player);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();
		if (event.getRecipients() instanceof ImmutableCollection) {
			String format = event.getFormat();
			format = format.replace("%1$s", ess.getUser(player).getDisplayName());
			format = format.replace("%2$s", "{MESSAGE}");
			event.setFormat(format);
			ChannelsManager.getInstance().sendMessage(player, event.getFormat(), message);
			event.setCancelled(true);
		} else {
			ChannelsManager.getInstance().sendMessage(event);
		}
		
	}
	
}
