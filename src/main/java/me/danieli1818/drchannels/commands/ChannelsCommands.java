package me.danieli1818.drchannels.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.danieli1818.drchannels.commands.subcommands.CreateChannelSubCommand;
import me.danieli1818.drchannels.commands.subcommands.DeleteChannelSubCommand;
import me.danieli1818.drchannels.commands.subcommands.JoinChannelSubCommand;
import me.danieli1818.drchannels.commands.subcommands.LeaveChannelSubCommand;
import me.danieli1818.drchannels.commands.subcommands.ListChannelSubCommand;
import me.danieli1818.drchannels.utils.ArgumentsParser;

public class ChannelsCommands implements CommandExecutor {
	
	private Map<String, SubCommandsExecutor> subCommands;
	
	private final String command = "channels";
	
	private static ChannelsCommands instance;
	
	private ChannelsCommands() {
		this.subCommands = new HashMap<>();
		this.subCommands.put("join", new JoinChannelSubCommand(this.command));
		this.subCommands.put("leave", new LeaveChannelSubCommand(this.command));
		this.subCommands.put("create", new CreateChannelSubCommand(this.command));
		this.subCommands.put("delete", new DeleteChannelSubCommand(this.command));
		this.subCommands.put("list", new ListChannelSubCommand(this.command));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String[] parsedArgs = ArgumentsParser.parse(args);
		if (parsedArgs.length == 0) {
			sender.sendMessage("Error! Invalid Command! Type /" + this.command + " help for help!");
			return false;
		}
		String subCommand = parsedArgs[0];
		SubCommandsExecutor subCommandsExecutor = this.subCommands.get(subCommand);
		if (subCommandsExecutor == null) {
			sender.sendMessage("Error! Invalid Command! Type /" + this.command + " help for help!");
			return false;
		}
		return subCommandsExecutor.onCommand(sender, subCommand, subCommand, Arrays.copyOfRange(parsedArgs, 1, parsedArgs.length));
	}

	public static ChannelsCommands getInstance() {
		if (instance == null) {
			instance = new ChannelsCommands();
		}
		return instance;
	}
	
}
