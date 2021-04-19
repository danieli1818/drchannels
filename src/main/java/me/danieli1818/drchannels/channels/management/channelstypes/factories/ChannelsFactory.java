package me.danieli1818.drchannels.channels.management.channelstypes.factories;

import me.danieli1818.drchannels.channels.management.channelstypes.Channel;

public interface ChannelsFactory {

	public Channel create(String[] args, String name);
	
	public boolean canCreate(String[] args);
	
	public String getFormat();
	
}
