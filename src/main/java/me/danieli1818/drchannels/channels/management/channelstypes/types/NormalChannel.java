package me.danieli1818.drchannels.channels.management.channelstypes.types;

import java.util.Map;

import me.danieli1818.drchannels.channels.management.channelstypes.BaseChannel;
import me.danieli1818.drchannels.channels.management.channelstypes.Channel;


public class NormalChannel extends BaseChannel implements Channel {

	public NormalChannel(String prefix, String permissionView, String permissionTalk) {
		super(prefix, permissionView, permissionTalk);
	}
	
	public NormalChannel(String prefix, String permissionView, String permissionTalk, String messagePrefix) {
		super(prefix, permissionView, permissionTalk, messagePrefix);
	}
	
	public NormalChannel(BaseChannel baseChannel) {
		super(baseChannel.getPrefix(), baseChannel.getViewPermission(), baseChannel.getTalkPermission(), baseChannel.getMessagePrefix());
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> serializationMap = super.serialize();
		return serializationMap;
	}
	
	public static NormalChannel deserialize(Map<String, Object> serializationMap) {
		return new NormalChannel(BaseChannel.deserialize(serializationMap));
	}

}
