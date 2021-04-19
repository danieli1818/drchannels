package me.danieli1818.drchannels.commands.subcommands;


import org.bukkit.command.CommandSender;

import me.danieli1818.drchannels.channels.management.ChannelsManager;
import me.danieli1818.drchannels.commands.SubCommandsExecutor;

public class ListChannelSubCommand implements SubCommandsExecutor {

	private String prefix;
	
	public ListChannelSubCommand(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String subCommand, String label, String[] args) {
		return ChannelsManager.getInstance().onListChannelsRequest(sender);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getHelp(int page) {
		String[] help = new String[1];
		help[0] = "/" + this.prefix + " list - To list all the channels you can view!";
		return help;
	}

}
