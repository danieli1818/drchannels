package me.danieli1818.drchannels.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.danieli1818.drchannels.channels.management.ChannelsManager;
import me.danieli1818.drchannels.commands.SubCommandsExecutor;
import me.danieli1818.drchannels.utils.MessagesSender;

public class JoinChannelSubCommand implements SubCommandsExecutor {
	
	private String prefix;
	
	public JoinChannelSubCommand(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public boolean onCommand(CommandSender sender, String subCommand, String label, String[] args) {
		if (args.length == 0) {
			MessagesSender.getInstance().sendMessage("Error! Invalid command syntax! Did you mean /" + this.prefix + " join [channel name]", sender);
			return false;
		}
		if (!(sender instanceof Player)) {
			MessagesSender.getInstance().sendMessage("Error! You have to be a player to run this command!", sender);
			return false;
		}
		String channelName = String.join(" ", args).toLowerCase();
		Player player = (Player)sender;
		if (channelName.equals("all")) {
			ChannelsManager.getInstance().joinAllPermittedChannels(player);
		} else {
			ChannelsManager.getInstance().onPlayerJoinRequest(player, channelName);
		}
		return true;
	}

	@Override
	public String getDescription() {
		return "Join Channel";
	}

	@Override
	public String[] getHelp(int page) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
