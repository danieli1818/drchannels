package me.danieli1818.drchannels.channels.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import me.danieli1818.drchannels.channels.management.channelstypes.Channel;
import me.danieli1818.drchannels.channels.management.channelstypes.TypesManager;
import me.danieli1818.drchannels.channels.management.channelstypes.factories.ChannelsFactory;
import me.danieli1818.drchannels.utils.FileConfigurationsManager;
import me.danieli1818.drchannels.utils.MessagesSender;
import me.danieli1818.drchannels.utils.SchedulerUtils;
import net.md_5.bungee.api.ChatColor;

public class ChannelsManager {

	private Map<String, Channel> channels;
	private Map<UUID, String> currentChannelPerPlayer;
	private List<String> defaultChannels;
	private Map<String, String> aliases;
	private Plugin plugin;
	
	private static ChannelsManager instance = null;
	
	private ChannelsManager(Plugin plugin) {
		this.channels = new HashMap<String, Channel>();
		this.currentChannelPerPlayer = new HashMap<UUID, String>();
		this.aliases = new HashMap<>();
		this.plugin = plugin;
		this.defaultChannels = new ArrayList<>();
	}
	
	public static ChannelsManager getInstance() {
		return instance;
	}
	
	public static ChannelsManager getInstance(Plugin plugin) {
		if (instance == null) {
			instance = new ChannelsManager(plugin);
		}
		return instance;
	}
	
	private boolean canJoinChannel(String channel, Player player) {
		if (doesChannelExist(channel)) {
			return this.channels.get(channel).canJoin(player);
		}
		return false;
		
	}
	
	private boolean canLeaveChannel(String channel, Player player) {
		if (doesChannelExist(channel)) {
			return this.channels.get(channel).canLeave(player);
		}
		return false;
		
	}
	
	private boolean isPlayerInChannel(String channel, Player player) {
		if (doesChannelExist(channel)) {
			return this.channels.get(channel).isPlayerInChannel(player.getUniqueId());
		}
		return false;
	}
	
	private boolean doesChannelExist(String channel) {
		return this.channels.containsKey(channel);
	}
	
	public boolean onPlayerJoinRequest(Player player, String channel) {
		if (!this.channels.containsKey(channel) && this.aliases.containsKey(channel)) {
			channel = this.aliases.get(channel);
		}
		if (canJoinChannel(channel, player)) {
			if (!isPlayerInChannel(channel, player)) {
				addPlayerToChannel(player, channel);
				MessagesSender.getInstance().sendMessage("Successfully joined \"" + channel + "\" channel!", player);
				return true;
			} else if (!channel.equals(getPlayerCurrentChannel(player.getUniqueId()))) {
				setPlayerCurrentChannel(channel, player.getUniqueId());
				MessagesSender.getInstance().sendMessage("Successfully set main channel as \"" + channel + "\"", player);
			} else {
				MessagesSender.getInstance().sendMessage("Error! You are already in this channel!", player);
			}
		} else {
			MessagesSender.getInstance().sendMessage("You can't join this channel!", player);
		}
		return false;
	}
	
	public boolean onPlayerLeaveRequest(Player player, String channel) {
		if (!this.channels.containsKey(channel) && this.aliases.containsKey(channel)) {
			channel = this.aliases.get(channel);
		}
		if (canLeaveChannel(channel, player)) {
			if (isPlayerInChannel(channel, player)) {
				removePlayerFromChannel(player, channel);
				MessagesSender.getInstance().sendMessage("Successfully left \"" + channel + "\" channel!", player);
				return true;
			} else {
				MessagesSender.getInstance().sendMessage("Error! You aren't in this channel!", player);
			}
		} else {
			MessagesSender.getInstance().sendMessage("You can't join this channel!", player);
		}
		return false;
	}
	
	private void addPlayerToChannel(Player player, String channel) {
		this.channels.get(channel).addPlayer(player.getUniqueId());
		this.currentChannelPerPlayer.put(player.getUniqueId(), channel);
	}
	
	private void removePlayerFromChannel(Player player, String channel) {
		this.channels.get(channel).removePlayer(player.getUniqueId());
		if (channel.equals(this.currentChannelPerPlayer.get(player.getUniqueId()))) {
			this.currentChannelPerPlayer.put(player.getUniqueId(), null);
		}
	}
	
	public void loadChannels() {
		this.channels.clear();
		FileConfiguration fileConfiguration = FileConfigurationsManager.getInstance().getFileConfiguration("channels.yml");
		if (fileConfiguration == null) {
			return;
		}
		loadChannelsFromFileConfiguration(fileConfiguration);
		loadDefaultChannels();
		loadAliases();
	}
	
	private void loadChannelsFromFileConfiguration(FileConfiguration fileConfiguration) {
		if (fileConfiguration == null) {
			return;
		}
		Set<String> keys = fileConfiguration.getKeys(false);
		if (keys == null) {
			return;
		}
		for (String key : fileConfiguration.getKeys(false)) {
			key = key.toLowerCase();
			this.channels.put(key, fileConfiguration.getSerializable(key, Channel.class));
		}
	}
	
	private void loadDefaultChannels() {
		FileConfiguration fileConfiguration = FileConfigurationsManager.getInstance().getFileConfiguration("config.yml");
		this.defaultChannels = fileConfiguration.getStringList("default_channels");
	}
	
	private boolean loadAliases() {
		FileConfiguration fileConfiguration = FileConfigurationsManager.getInstance().getFileConfiguration("config.yml");
		ConfigurationSection aliasesSection = fileConfiguration.getConfigurationSection("aliases");
		if (aliasesSection == null) {
			return false;
		}
		for (String alias : aliasesSection.getKeys(false)) {
			String channel = aliasesSection.getString(alias);
			if (channel == null) {
				continue;
			}
			this.aliases.put(alias, channel);
		}
		return true;
	}
	
	public void joinAllPermittedChannels(Player player) {
		for (Channel channel : getAllPermittedChannelsOfPlayer(player)) {
			channel.addPlayer(player.getUniqueId());
		}
	}
	
	public void leaveAllPermittedChannels(Player player) {
		for (Channel channel : getAllChannelsPlayerIn(player.getUniqueId())) {
			if (channel.canJoin(player)) {
				channel.removePlayer(player.getUniqueId());
			}
		}
	}
	
	private List<Channel> getAllPermittedChannelsOfPlayer(Player player) {
		return this.channels.values().stream().filter((Channel c) -> c.canJoin(player)).collect(Collectors.toList());
	}
	
	public boolean joinDefaultChannelIfNoneCurrentChannel(Player player) {
		if (this.defaultChannels != null 
				&& !this.defaultChannels.isEmpty()
				&& this.currentChannelPerPlayer.get(player.getUniqueId()) == null 
				) {
			for (String channelName : this.defaultChannels) {
				Channel channel = this.channels.get(channelName);
				if (channel != null
						&& channel.canJoin(player)) {
					channel.addPlayer(player.getUniqueId());
					this.currentChannelPerPlayer.put(player.getUniqueId(), channelName);
					break;
				}
			}
			return true;
		}
		return false;
	}
	
	public void sendMessage(Player player, String format, String message) {
		Channel channel = this.channels.get(this.currentChannelPerPlayer.get(player.getUniqueId()));
		if (channel == null) {
			System.out.println(player.getName() + " tried to send message: " + message);
			return;
		}
		sendMessage(channel, format, message, player);
	}
	
	public void sendMessage(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();
		Channel channel = this.channels.get(this.currentChannelPerPlayer.get(player.getUniqueId()));
		if (channel == null) {
			System.out.println(player.getName() + " tried to send message: " + message);
			return;
		}
		event.setFormat(event.getFormat().replaceFirst("\\{CHANNEL\\}", channel.getPrefix() + ChatColor.RESET));
		message = channel.getMessagePrefix() + message;
		event.setMessage(message);
		Set<Player> recipients = event.getRecipients();
		recipients.clear();
		Set<Player> channelPlayers = channel.getPlayersToSendMessageTo(player, message).parallelStream().map((UUID uuid) -> this.plugin.getServer().getPlayer(uuid)).collect(Collectors.toSet());
		Set<Player> onlineChannelPlayers = channelPlayers.parallelStream().filter((Player currentPlayer) -> currentPlayer != null && currentPlayer.isOnline()).collect(Collectors.toSet());
		recipients.addAll(onlineChannelPlayers);
	}
	
	private void sendMessage(Channel channel, String format, String message, Player sender) {
		format = format.replaceFirst("\\{CHANNEL\\}", channel.getPrefix());
		format = format.replaceFirst("\\{MESSAGE\\}", message);
		System.out.println(format);
//		message = channel.getPrefix() + message;
		final String finalFormat = format;
		SchedulerUtils.getInstance().scheduleAsyncTask(0, () -> {
			for (UUID uuid : channel.getPlayersToSendMessageTo(sender, message)) {
				Player player = Bukkit.getPlayer(uuid);
				if (player.isOnline()) {
					player.sendMessage(finalFormat);
				}
			}
		});
//		sendMessageToSocialSpys(message);
		
	}
	
	private Collection<Channel> getAllChannelsPlayerIn(UUID uuid) {
		List<Channel> channelsPlayerIn = new ArrayList<>();
		for (Channel channel : this.channels.values()) {
			if (channel.isPlayerInChannel(uuid)) {
				channelsPlayerIn.add(channel);
			}
		}
		return channelsPlayerIn;
	}
	
	public boolean onCreateChannelRequest(CommandSender sender, String type, String name, String[] args) {
		String permission = "drchannels.create";
		if (!sender.hasPermission(permission)) {
			MessagesSender.getInstance().sendDontHavePermissionErrorMessage(permission, sender);
			return false;
		}
		ChannelsFactory typeFactory = TypesManager.getTypeFactory(type);
		if (typeFactory == null) {
			MessagesSender.getInstance().sendMessage("Error! Channel type doesn't exist!", sender);
			return false;
		}
		name = name.toLowerCase();
		Channel channel = typeFactory.create(args, name);
		if (channel == null) {
			MessagesSender.getInstance().sendMessage("Error! Arguments are invalid for this channel type!", sender);
			return false;
		}
		this.channels.put(name, channel);
		FileConfigurationsManager.getInstance().addConfigurationSerializablesToConfiguration("channels.yml", this.channels);
		MessagesSender.getInstance().sendMessage("Successfully created \"" + name + "\" channel!", sender);
		return true;
	}
	
	public boolean onDeleteChannelRequest(CommandSender sender, String name) {
		String permission = "drchannels.delete";
		if (!sender.hasPermission(permission)) {
			MessagesSender.getInstance().sendDontHavePermissionErrorMessage(permission, sender);
			return false;
		}
		name = name.toLowerCase();
		if (!doesChannelExist(name)) {
			MessagesSender.getInstance().sendMessage("Error! Channel \"" + name + "\" doesn't exist!", sender);
			return false;
		}
		this.channels.remove(name);
		MessagesSender.getInstance().sendMessage("Successfully deleted \"" + name + "\" channel!", sender);
		return true;
	}
	
	private String getPlayerCurrentChannel(UUID uuid) {
		return this.currentChannelPerPlayer.get(uuid);
	}
	
	private String setPlayerCurrentChannel(String channel, UUID uuid) {
		return this.currentChannelPerPlayer.put(uuid, channel);
	}
	
	public boolean onListChannelsRequest(CommandSender sender) {
		String permission = "drchannels.list";
		if (!sender.hasPermission(permission)) {
			MessagesSender.getInstance().sendMessage("Error! You don't have a permission to run this command!", sender);
			return false;
		}
		String channelsAliasesMessage = "\n";
		for (String channel : getAllViewableChannelsNamesOfSender(sender)) {
			channelsAliasesMessage += channel + " - ";
			Collection<String> channelAliases = getChannelAliases(channel);
			channelsAliasesMessage += String.join(", ", channelAliases.toArray(new String[channelAliases.size()]));
			channelsAliasesMessage += "\n";
		}
		MessagesSender.getInstance().sendMessage(channelsAliasesMessage, sender);
		return true;
	}
	
	private Collection<String> getChannelAliases(String channel) {
		Collection<String> channelAliases = new HashSet<>();
		if (channel == null) {
			return channelAliases;
		}
		for (String alias : this.aliases.keySet()) {
			if (channel.equals(this.aliases.get(alias))) {
				channelAliases.add(alias);
			}
		}
		return channelAliases;
	}
	
	private List<String> getAllViewableChannelsNamesOfSender(CommandSender sender) {
		return this.channels.entrySet().stream().filter((Entry<String, Channel> entry) -> entry.getValue().canView(sender)).map((Entry<String, Channel> entry) -> entry.getKey()).collect(Collectors.toList());
	}
	
//	private void sendMessageToSocialSpys(String message) {
////		final LocalChatSpyEvent spyEvent = new LocalChatSpyEvent(true, sender, "", message, getSocialSpys());
////        this.plugin.getServer().getPluginManager().callEvent(spyEvent);
////        
////        System.out.println(spyEvent.getFormat());
//
//		////////////////
////        for (final Player onlinePlayer : getSocialSpys()) {
////            onlinePlayer.sendMessage("[Spy] " + message);
////        }
//	}
//	
//	private Set<Player> getSocialSpys() {
//		Set<Player> socialSpys = new HashSet<>();
//		for (Player player : DRChannels.getPlugin(DRChannels.class).getServer().getOnlinePlayers()) {
//			if (player.hasPermission("essentials.chat.spy")) {
//				socialSpys.add(player);
//			}
//		}
//		return socialSpys;
//	}
	
}
