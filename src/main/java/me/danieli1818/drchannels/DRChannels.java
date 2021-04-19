package me.danieli1818.drchannels;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;

import me.danieli1818.drchannels.channels.management.ChannelsManager;
import me.danieli1818.drchannels.channels.management.channelstypes.types.LocalChannel;
import me.danieli1818.drchannels.channels.management.channelstypes.types.NormalChannel;
import me.danieli1818.drchannels.commands.ChannelsCommands;
import me.danieli1818.drchannels.listeners.ChannelsListener;
import me.danieli1818.drchannels.utils.FileConfigurationsManager;
import me.danieli1818.drchannels.utils.SchedulerUtils;
import net.md_5.bungee.api.ChatColor;

public class DRChannels extends JavaPlugin {
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		me.danieli1818.drchannels.utils.MessagesSender.getInstance(ChatColor.GREEN + "[" + ChatColor.GOLD + "DRChannels" + ChatColor.GREEN + "]" + ChatColor.BLUE);
		
		FileConfigurationsManager fcm = FileConfigurationsManager.getInstance(this);
		
		fcm.registerConfigurationSerializables(getConfigurationSerializablesClasses());
		fcm.createConfigurationFile("config.yml");
		fcm.createConfigurationFile("channels.yml");
		fcm.reloadAllFiles();
		
		SchedulerUtils.getInstance(this);

		Plugin essentialsPlugin = getServer().getPluginManager().getPlugin("Essentials");
		
		if (essentialsPlugin == null || !(essentialsPlugin instanceof Essentials)) {
			System.err.println("EssentialsX Plugin Is Missing!");
			return;
		}
		
		ChannelsManager channelsManager = ChannelsManager.getInstance(this);
		channelsManager.loadChannels();
		
		getCommand("drchannels").setExecutor(ChannelsCommands.getInstance());
		
		getServer().getPluginManager().registerEvents(ChannelsListener.getInstance((Essentials) essentialsPlugin), this);
		
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
	}
	
	private Collection<Class<? extends ConfigurationSerializable>> getConfigurationSerializablesClasses() {
		Collection<Class<? extends ConfigurationSerializable>> configurationSerializablesClasses = new HashSet<>();
		configurationSerializablesClasses.add(NormalChannel.class);
		configurationSerializablesClasses.add(LocalChannel.class);
		return configurationSerializablesClasses;
	}
	
}
