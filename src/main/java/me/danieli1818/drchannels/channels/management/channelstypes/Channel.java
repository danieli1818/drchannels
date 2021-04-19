package me.danieli1818.drchannels.channels.management.channelstypes;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public interface Channel extends ConfigurationSerializable {

	public boolean canJoin(Player player);
	
	public boolean canView(CommandSender sender);
	
	public String getPrefix();
	
	public Collection<UUID> getPlayersInChannel();
	
	public Collection<UUID> getPlayersToSendMessageTo(CommandSender sender, String message);
	
	public boolean isPlayerInChannel(UUID uuid);
	
	public boolean addPlayer(UUID uuid);
	
	public boolean removePlayer(UUID uuid);
	
	public boolean canLeave(Player player);
	
	public String getMessagePrefix();
	
}
