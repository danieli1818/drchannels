package me.danieli1818.drchannels.commands.subcommands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.danieli1818.drchannels.channels.management.ChannelsManager;
import me.danieli1818.drchannels.commands.SubCommandsExecutor;
import me.danieli1818.drchannels.utils.MessagesSender;
import net.md_5.bungee.api.ChatColor;

public class CreateChannelSubCommand implements SubCommandsExecutor {

	private String prefix;
	
	public CreateChannelSubCommand(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String subCommand, String label, String[] args) {
		if (args.length <= 1) {
			MessagesSender.getInstance().sendMessage("Error! Invalid command syntax! Did you mean /" + this.prefix + " create [type] [channel name]", sender);
			return false;
		}
		if (!(sender instanceof Player)) {
			MessagesSender.getInstance().sendMessage("Error! You have to be a player to run this command!", sender);
			return false;
		}
		String type = args[0];
		String name = args[1];
		String[] arguments;
		if (args.length > 2) {
			arguments = Arrays.copyOfRange(args, 2, args.length);
		} else {
			arguments = new String[0];
		}
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = ChatColor.translateAlternateColorCodes('&', arguments[i]);
		}
		return ChannelsManager.getInstance().onCreateChannelRequest(sender, type, name, arguments);
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
