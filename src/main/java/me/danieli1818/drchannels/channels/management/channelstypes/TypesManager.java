package me.danieli1818.drchannels.channels.management.channelstypes;

import java.util.HashMap;
import java.util.Map;

import me.danieli1818.drchannels.channels.management.channelstypes.factories.ChannelsFactory;
import me.danieli1818.drchannels.channels.management.channelstypes.factories.LocalChannelsFactory;
import me.danieli1818.drchannels.channels.management.channelstypes.factories.NormalChannelsFactory;

public class TypesManager {

	private static final Map<String, ChannelsFactory> typesFactories;
	static {
		typesFactories = new HashMap<>();
		typesFactories.put("normal", new NormalChannelsFactory());
		typesFactories.put("local", new LocalChannelsFactory());
	}
	
	public static ChannelsFactory getTypeFactory(String type) {
		type = type.toLowerCase();
		return typesFactories.get(type);
	}
	
}
