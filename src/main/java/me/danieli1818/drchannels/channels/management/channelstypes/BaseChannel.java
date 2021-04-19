package me.danieli1818.drchannels.channels.management.channelstypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.danieli1818.drchannels.channels.management.channelstypes.types.NormalChannel;

public abstract class BaseChannel implements Channel {

	private String prefix;
	private String permissionView;
	private String permissionTalk;
	private Set<UUID> playersInChannel;
	private String messagePrefix;
	
	public BaseChannel(String prefix, String permissionView, String permissionTalk) {
		if (prefix == null) {
			prefix = "";
		}
		this.prefix = prefix;
		this.permissionView = permissionView;
		this.permissionTalk = permissionTalk;
		this.messagePrefix = "";
		this.playersInChannel = new HashSet<>();
	}
	
	public BaseChannel(String prefix, String permissionView, String permissionTalk, String messagePrefix) {
		this(prefix, permissionView, permissionTalk);
		this.messagePrefix = messagePrefix;
	}
	
	public boolean canJoin(Player player) {
		return player.hasPermission(permissionTalk);
	}
	
	public boolean canView(CommandSender sender) {
		return sender.hasPermission(permissionView);
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public String getViewPermission() {
		return this.permissionView;
	}
	
	public String getTalkPermission() {
		return this.permissionTalk;
	}
	
	public Collection<UUID> getPlayersInChannel() {
		return this.playersInChannel;
	}
	
	@Override
	public Collection<UUID> getPlayersToSendMessageTo(CommandSender sender, String message) {
		return getPlayersInChannel();
	}

	public boolean isPlayerInChannel(UUID uuid) {
		return this.playersInChannel.contains(uuid);
	}

	public boolean addPlayer(UUID uuid) {
		return this.playersInChannel.add(uuid);
	}

	public boolean removePlayer(UUID uuid) {
		return this.playersInChannel.remove(uuid);
	}
	
	public String getMessagePrefix() {
		return this.messagePrefix;
	}

	public Map<String, Object> serialize() {
		Map<String, Object> serializationMap = new HashMap<String, Object>();
		serializationMap.put("prefix", this.prefix);
		serializationMap.put("talk_permission", this.permissionTalk);
		serializationMap.put("view_permission", this.permissionView);
		serializationMap.put("message_prefix", this.messagePrefix);
		return serializationMap;
	}
	
	public static BaseChannel deserialize(Map<String, Object> serializationMap) {
		Object prefix = serializationMap.get("prefix");
		if (prefix == null || !(prefix instanceof String)) {
			prefix = "";
		}
		Object permissionTalk = serializationMap.get("talk_permission");
		if (permissionTalk == null || !(permissionTalk instanceof String)) {
			permissionTalk = "";
		}
		Object permissionView = serializationMap.get("view_permission");
		if (permissionView == null || !(permissionView instanceof String)) {
			permissionView = "";
		}
		Object messagePrefix = serializationMap.get("message_prefix");
		if (messagePrefix == null || !(messagePrefix instanceof String)) {
			messagePrefix = "";
		}
		return new NormalChannel((String)prefix, (String)permissionView, (String)permissionTalk, (String)messagePrefix);
	}

	@Override
	public boolean canLeave(Player player) {
		return isPlayerInChannel(player.getUniqueId());
	}
	
}
