package me.danieli1818.drchannels.chat;

import me.danieli1818.drchannels.channel.Channel;
import me.danieli1818.drchannels.channel.ChannelRegistry;
import me.danieli1818.drchannels.chatter.ChatterManager;
import me.danieli1818.drchannels.util.Messages;
import me.danieli1818.drchannels.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Routes chat messages into channels. This is the only place that decides who receives a message.
 * <p>
 * Recipients are always filtered on Bukkit's {@link AsyncPlayerChatEvent}: its recipient set is mutable on
 * both Spigot and Paper (on Paper it becomes the viewers of the later {@code AsyncChatEvent}), whereas
 * EssentialsChat's own events expose an unmodifiable set on Paper. The channel chosen there is remembered
 * so the EssentialsChat format can be tagged afterwards via {@link #tagFormat}.
 */
public final class ChannelChatService {

    private static final String CHANNEL_PLACEHOLDER = "{CHANNEL}";

    private final ChannelRegistry channels;
    private final ChatterManager chatters;
    private final Messages messages;
    private final Logger logger;
    /** One-shot channel overrides for {@code /channel <channel> <message>}, by player. */
    private final Map<UUID, String> quickSends = new ConcurrentHashMap<>();
    /** Channel each player's in-flight message was routed to, awaiting the EssentialsChat format stage. */
    private final Map<UUID, Channel> routed = new ConcurrentHashMap<>();
    private volatile String standaloneFormat = "";

    public ChannelChatService(ChannelRegistry channels, ChatterManager chatters, Messages messages, Logger logger) {
        this.channels = channels;
        this.chatters = chatters;
        this.messages = messages;
        this.logger = logger;
    }

    /**
     * @param template the user-facing format used when EssentialsChat is not installed
     */
    public void standaloneFormat(String template) {
        this.standaloneFormat = Text.compileChatFormat(template);
    }

    /**
     * Sends a single message to {@code channel} without changing the player's focus. The message goes
     * through the normal chat pipeline, so mutes, costs and formatting from other plugins still apply.
     * Must be called on the main thread, where {@link Player#chat} fires the chat events synchronously.
     */
    public void quickSend(Player player, Channel channel, String message) {
        quickSends.put(player.getUniqueId(), channel.id());
        try {
            player.chat(message);
        } finally {
            quickSends.remove(player.getUniqueId());
        }
    }

    public void forget(Player player) {
        quickSends.remove(player.getUniqueId());
        routed.remove(player.getUniqueId());
    }

    /**
     * Restricts the event's recipients to the sender's channel.
     *
     * @param applyStandaloneFormat whether to set the configured format (no EssentialsChat) or leave the
     *                              format to be tagged by {@link #tagFormat}
     */
    public void route(AsyncPlayerChatEvent event, boolean applyStandaloneFormat) {
        final Player sender = event.getPlayer();
        routed.remove(sender.getUniqueId());
        final Channel channel = resolve(sender);
        if (channel == null) {
            messages.send(sender, "not-in-channel");
            event.setCancelled(true);
            return;
        }
        if (!channel.canSpeak(sender)) {
            messages.send(sender, "cannot-speak", "channel", channel.id());
            event.setCancelled(true);
            return;
        }

        try {
            event.getRecipients().removeIf(recipient -> !recipient.equals(sender) && !receives(recipient, sender, channel));
        } catch (UnsupportedOperationException e) {
            // Fail closed: never deliver a channel message to everyone.
            logger.warning("Cancelled a chat message from " + sender.getName() + ": the event's recipients cannot be modified.");
            event.setCancelled(true);
            return;
        }

        if (applyStandaloneFormat) {
            event.setFormat(applyTag(standaloneFormat, channel));
        } else {
            routed.put(sender.getUniqueId(), channel);
        }
    }

    /**
     * @return {@code format} with the tag of the channel the sender's message was routed to
     */
    public String tagFormat(Player sender, String format) {
        final Channel channel = Optional.ofNullable(routed.remove(sender.getUniqueId()))
                .or(() -> chatters.focus(sender))
                .orElse(null);
        return channel == null ? format : applyTag(format, channel);
    }

    private boolean receives(Player recipient, Player sender, Channel channel) {
        return chatters.isMember(recipient, channel) && channel.canJoin(recipient) && channel.reaches(sender, recipient);
    }

    private @Nullable Channel resolve(Player sender) {
        final String quickSend = quickSends.get(sender.getUniqueId());
        final Optional<Channel> override = quickSend == null
                ? Optional.empty()
                : channels.byId(quickSend).filter(channel -> chatters.isMember(sender, channel));
        return override.or(() -> chatters.focus(sender)).orElse(null);
    }

    static String applyTag(String format, Channel channel) {
        final String tag = Text.escapeFormat(channel.tag());
        return format.contains(CHANNEL_PLACEHOLDER) ? format.replace(CHANNEL_PLACEHOLDER, tag) : tag + format;
    }
}
