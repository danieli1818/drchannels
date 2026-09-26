package me.danieli1818.drchannels.chat;

import me.danieli1818.drchannels.channel.Channel;
import me.danieli1818.drchannels.channel.ChannelRegistry;
import me.danieli1818.drchannels.chatter.ChatterManager;
import me.danieli1818.drchannels.util.Messages;
import me.danieli1818.drchannels.util.Text;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Routes chat messages into channels. This is the only place that decides who receives a message;
 * the chat listeners only adapt their event to {@link ChatEventView}.
 */
public final class ChannelChatService {

    private static final String CHANNEL_PLACEHOLDER = "{CHANNEL}";
    private static final long QUICK_SEND_TTL_NANOS = TimeUnit.SECONDS.toNanos(2);

    private record QuickSend(String channelId, long expiresAt) {
        boolean isValid() {
            return expiresAt - System.nanoTime() > 0;
        }
    }

    private final ChannelRegistry channels;
    private final ChatterManager chatters;
    private final Messages messages;
    private final Map<UUID, QuickSend> quickSends = new ConcurrentHashMap<>();
    private volatile String standaloneFormat = "";

    public ChannelChatService(ChannelRegistry channels, ChatterManager chatters, Messages messages) {
        this.channels = channels;
        this.chatters = chatters;
        this.messages = messages;
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
     */
    public void quickSend(Player player, Channel channel, String message) {
        quickSends.put(player.getUniqueId(), new QuickSend(channel.id(), System.nanoTime() + QUICK_SEND_TTL_NANOS));
        player.chat(message);
    }

    public void forget(Player player) {
        quickSends.remove(player.getUniqueId());
    }

    /**
     * Restricts the event's recipients to the sender's channel and applies the channel tag.
     *
     * @param useStandaloneFormat whether to replace the event's format with the configured one
     */
    public void route(ChatEventView event, boolean useStandaloneFormat) {
        final Player sender = event.player();
        final Channel channel = resolve(sender);
        if (channel == null) {
            messages.send(sender, "not-in-channel");
            event.canceller().run();
            return;
        }
        if (!channel.canSpeak(sender)) {
            messages.send(sender, "cannot-speak", "channel", channel.id());
            event.canceller().run();
            return;
        }

        event.recipients().removeIf(recipient -> !recipient.equals(sender) && !receives(recipient, sender, channel));
        event.formatSetter().accept(applyTag(useStandaloneFormat ? standaloneFormat : event.format().get(), channel));
    }

    private boolean receives(Player recipient, Player sender, Channel channel) {
        return chatters.isMember(recipient, channel) && channel.canJoin(recipient) && channel.reaches(sender, recipient);
    }

    private @Nullable Channel resolve(Player sender) {
        final QuickSend quickSend = quickSends.remove(sender.getUniqueId());
        final Optional<Channel> override = quickSend != null && quickSend.isValid()
                ? channels.byId(quickSend.channelId()).filter(channel -> chatters.isMember(sender, channel))
                : Optional.empty();
        return override.or(() -> chatters.focus(sender)).orElse(null);
    }

    static String applyTag(String format, Channel channel) {
        final String tag = Text.escapeFormat(channel.tag());
        return format.contains(CHANNEL_PLACEHOLDER) ? format.replace(CHANNEL_PLACEHOLDER, tag) : tag + format;
    }
}
