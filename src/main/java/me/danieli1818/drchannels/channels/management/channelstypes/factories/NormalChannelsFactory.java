package me.danieli1818.drchannels.channels.management.channelstypes.factories;

import me.danieli1818.drchannels.channels.management.channelstypes.Channel;
import me.danieli1818.drchannels.channels.management.channelstypes.types.NormalChannel;

public class NormalChannelsFactory implements ChannelsFactory {

	@Override
	public Channel create(String[] args, String name) {
		if (args.length < 1) {
			return null;
		}
		String prefix = args[0];
		
		return new NormalChannel(prefix, "drchannels.view." + name, "drchannels.talk." + name);
	}

	@Override
	public boolean canCreate(String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
