package me.danieli1818.drchannels.channels.management.channelstypes.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.danieli1818.drchannels.channels.management.channelstypes.BaseChannel;
import me.danieli1818.drchannels.channels.management.channelstypes.Channel;

public class LocalChannel extends BaseChannel implements Channel {

	private int radius;
	
	public LocalChannel(String prefix, String permissionView, String permissionTalk, int radius) {
		super(prefix, permissionView, permissionTalk);
		this.radius = radius;
	}
	
	public LocalChannel(BaseChannel baseChannel, int radius) {
		super(baseChannel.getPrefix(), baseChannel.getViewPermission(), baseChannel.getTalkPermission(), baseChannel.getMessagePrefix());
		this.radius = radius;
	}

	public int getRadius() {
		return this.radius;
	}
	
	@Override
	public Collection<UUID> getPlayersToSendMessageTo(CommandSender sender, String message) {
		if (!(sender instanceof Player)) {
			return new HashSet<UUID>();
		}
		Player player = (Player)sender;
		Location location = player.getLocation();
		Set<UUID> players = new HashSet<>();
		for (Entity entity : player.getNearbyEntities(this.radius, this.radius, this.radius)) {
			if (entity.getLocation().distanceSquared(location) <= this.radius * this.radius && entity instanceof Player && isPlayerInChannel(((Player)entity).getUniqueId())) {
				players.add(((Player)entity).getUniqueId());
			}
		}
		players.add(player.getUniqueId());
		return players;
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> serializationMap = super.serialize();
		serializationMap.put("radius", this.radius);
		return serializationMap;
	}
	
	public static LocalChannel deserialize(Map<String, Object> serializationMap) {
		int radius;
		Object radiusObject = serializationMap.get("radius");
		if (radiusObject == null || !(radiusObject instanceof Integer)) {
			return null;
		}
		radius = (Integer)radiusObject;
		return new LocalChannel(BaseChannel.deserialize(serializationMap), radius);
	}
	
	
}
