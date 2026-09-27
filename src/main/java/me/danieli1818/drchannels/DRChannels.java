package me.danieli1818.drchannels;

import me.danieli1818.drchannels.api.ChannelTypeRegistry;
import me.danieli1818.drchannels.channel.BuiltInChannelTypes;
import me.danieli1818.drchannels.channel.ChannelConfig;
import me.danieli1818.drchannels.channel.ChannelRegistry;
import me.danieli1818.drchannels.chat.BukkitChatListener;
import me.danieli1818.drchannels.chat.ChannelChatService;
import me.danieli1818.drchannels.chat.EssentialsChatListener;
import me.danieli1818.drchannels.chatter.ChatterManager;
import me.danieli1818.drchannels.chatter.PlayerConnectionListener;
import me.danieli1818.drchannels.command.ChannelCommand;
import me.danieli1818.drchannels.command.CommandContext;
import me.danieli1818.drchannels.util.Messages;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class DRChannels extends JavaPlugin {

    private static final String CHANNELS_FILE = "channels.yml";
    private static final String ESSENTIALS_CHAT_EVENT = "net.essentialsx.api.v2.events.chat.GlobalChatEvent";

    private final ChannelTypeRegistry channelTypes = new ChannelTypeRegistry();
    private final Messages messages = new Messages();
    private ChannelRegistry channels;
    private ChannelChatService chat;

    @Override
    public void onLoad() {
        BuiltInChannelTypes.registerAll(channelTypes);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!new File(getDataFolder(), CHANNELS_FILE).exists()) {
            saveResource(CHANNELS_FILE, false);
        }

        channels = new ChannelRegistry(new ChannelConfig(channelTypes, new File(getDataFolder(), CHANNELS_FILE), getLogger()), getLogger());
        final ChatterManager chatters = new ChatterManager(this, channels);
        channels.onChange(chatters::reconcileAll);
        chat = new ChannelChatService(channels, chatters, messages, getLogger());

        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerConnectionListener(chatters, chat), this);
        registerChatHook(pluginManager);

        final PluginCommand command = Objects.requireNonNull(getCommand("channel"), "channel command missing from plugin.yml");
        final ChannelCommand executor = new ChannelCommand(new CommandContext(channels, chatters, chat, messages), channelTypes, this::reload);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        // Deferred to the first tick so plugins depending on DRChannels can register channel types in their onEnable.
        getServer().getScheduler().runTask(this, this::reload);
    }

    /**
     * Reloads config.yml, messages and channels.yml, then re-validates every online player.
     *
     * @return {@code false} if channels.yml could not be read and the current channels were kept
     */
    public boolean reload() {
        reloadConfig();
        // Include messages added to the bundled config.yml after the server's copy was written.
        getConfig().options().copyDefaults(true);
        messages.load(getConfig().getConfigurationSection("messages"));
        chat.standaloneFormat(getConfig().getString("format", "{CHANNEL}{DISPLAYNAME}&7: &r{MESSAGE}"));
        return channels.reload(getConfig().getString("default-channel"));
    }

    /**
     * Registry other plugins use to add their own channel types.
     */
    public ChannelTypeRegistry getChannelTypes() {
        return channelTypes;
    }

    private void registerChatHook(PluginManager pluginManager) {
        final boolean essentialsChat = pluginManager.isPluginEnabled("EssentialsChat") && isClassPresent(ESSENTIALS_CHAT_EVENT);
        pluginManager.registerEvents(new BukkitChatListener(chat, !essentialsChat), this);
        if (essentialsChat) {
            pluginManager.registerEvents(new EssentialsChatListener(chat), this);
            getLogger().info("Hooked into EssentialsChat; using its chat formats.");
        } else {
            getLogger().info("EssentialsChat (2.19+) not found; using the format from config.yml.");
        }
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
