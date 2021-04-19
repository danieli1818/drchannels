package me.danieli1818.drchannels.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.danieli1818.drchannels.channels.management.ChannelsManager;
import me.danieli1818.drchannels.commands.SubCommandsExecutor;
import me.danieli1818.drchannels.utils.MessagesSender;

public class DeleteChannelSubCommand implements SubCommandsExecutor {

	private String prefix;
	
	public DeleteChannelSubCommand(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String subCommand, String label, String[] args) {
		if (args.length < 1) {
			MessagesSender.getInstance().sendMessage("Error! Invalid command syntax! Did you mean /" + this.prefix + " delete [channel name]", sender);
			return false;
		}
		if (!(sender instanceof Player)) {
			MessagesSender.getInstance().sendMessage("Error! You have to be a player to run this command!", sender);
			return false;
		}
		String name = args[0];
		return ChannelsManager.getInstance().onDeleteChannelRequest(sender, name);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getHelp(int page) {
		// TODO Auto-generated method stub
		return null;
	}

}
