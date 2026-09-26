package me.danieli1818.drchannels.chatter;

import me.danieli1818.drchannels.channel.Channel;
import me.danieli1818.drchannels.channel.ChannelRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns every player's channel membership and focus. All mutations happen on the main thread and are
 * persisted immediately to the player's {@link PersistentDataContainer}.
 */
public final class ChatterManager {

    public enum JoinResult { JOINED, FOCUSED, ALREADY_FOCUSED, NO_PERMISSION }

    public enum LeaveResult { LEFT, NOT_MEMBER, NOT_LEAVABLE }

    private static final PersistentDataType<List<String>, List<String>> STRING_LIST = PersistentDataType.LIST.strings();

    private final ChannelRegistry channels;
    private final Map<UUID, Chatter> chatters = new ConcurrentHashMap<>();
    private final NamespacedKey joinedKey;
    private final NamespacedKey leftKey;
    private final NamespacedKey focusKey;

    public ChatterManager(Plugin plugin, ChannelRegistry channels) {
        this.channels = channels;
        this.joinedKey = new NamespacedKey(plugin, "joined");
        this.leftKey = new NamespacedKey(plugin, "left");
        this.focusKey = new NamespacedKey(plugin, "focus");
    }

    /**
     * Loads a player's saved state and applies login rules (auto-join and default channel).
     */
    public void load(Player player) {
        final Chatter chatter = read(player);
        chatters.put(player.getUniqueId(), chatter);
        reconcile(player, chatter, true);
    }

    public void unload(Player player) {
        chatters.remove(player.getUniqueId());
    }

    /**
     * Re-validates every online player against the current channels, e.g. after a reload or delete.
     * Players not loaded yet (plugin enabled while they were online) are treated as logging in.
     */
    public void reconcileAll() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final Chatter chatter = chatters.get(player.getUniqueId());
            if (chatter == null) {
                load(player);
            } else {
                reconcile(player, chatter, false);
            }
        }
    }

    public JoinResult join(Player player, Channel channel) {
        if (!channel.canJoin(player)) {
            return JoinResult.NO_PERMISSION;
        }
        final Chatter chatter = chatter(player);
        final boolean added = chatter.joined.add(channel.id());
        chatter.left.remove(channel.id());
        if (!added && channel.id().equals(chatter.focus())) {
            return JoinResult.ALREADY_FOCUSED;
        }
        chatter.focus(channel.id());
        write(player, chatter);
        return added ? JoinResult.JOINED : JoinResult.FOCUSED;
    }

    public LeaveResult leave(Player player, Channel channel) {
        final Chatter chatter = chatter(player);
        if (!chatter.joined.contains(channel.id())) {
            return LeaveResult.NOT_MEMBER;
        }
        if (!channel.leavable()) {
            return LeaveResult.NOT_LEAVABLE;
        }
        chatter.joined.remove(channel.id());
        if (channel.autoJoin()) {
            chatter.left.add(channel.id());
        }
        if (channel.id().equals(chatter.focus())) {
            chatter.focus(fallbackFocus(chatter));
        }
        write(player, chatter);
        return LeaveResult.LEFT;
    }

    /**
     * Safe to call from the async chat thread.
     */
    public boolean isMember(Player player, Channel channel) {
        final Chatter chatter = chatters.get(player.getUniqueId());
        return chatter != null && chatter.joined.contains(channel.id());
    }

    /**
     * Safe to call from the async chat thread.
     */
    public Optional<Channel> focus(Player player) {
        final Chatter chatter = chatters.get(player.getUniqueId());
        return chatter == null || chatter.focus() == null ? Optional.empty() : channels.byId(chatter.focus());
    }

    private void reconcile(Player player, Chatter chatter, boolean login) {
        for (final Channel channel : channels.all()) {
            if (channel.autoJoin() && !chatter.left.contains(channel.id()) && channel.canJoin(player)) {
                chatter.joined.add(channel.id());
            }
        }
        if (login) {
            channels.defaultChannel().ifPresent(channel -> {
                chatter.joined.add(channel.id());
                chatter.left.remove(channel.id());
            });
        }
        chatter.joined.removeIf(id -> channels.byId(id).filter(channel -> channel.canJoin(player)).isEmpty());
        chatter.left.removeIf(id -> channels.byId(id).isEmpty());

        final String focus = chatter.focus();
        if (focus == null || !chatter.joined.contains(focus)) {
            chatter.focus(fallbackFocus(chatter));
        }
        write(player, chatter);
    }

    /**
     * @return the default channel if joined, otherwise the first joined channel in configuration order
     */
    private @Nullable String fallbackFocus(Chatter chatter) {
        return channels.defaultChannel()
                .filter(channel -> chatter.joined.contains(channel.id()))
                .or(() -> channels.all().stream().filter(channel -> chatter.joined.contains(channel.id())).findFirst())
                .map(Channel::id)
                .orElse(null);
    }

    private Chatter chatter(Player player) {
        return chatters.computeIfAbsent(player.getUniqueId(), uuid -> read(player));
    }

    private Chatter read(Player player) {
        final PersistentDataContainer data = player.getPersistentDataContainer();
        return new Chatter(
                data.getOrDefault(joinedKey, STRING_LIST, List.of()),
                data.getOrDefault(leftKey, STRING_LIST, List.of()),
                data.get(focusKey, PersistentDataType.STRING));
    }

    private void write(Player player, Chatter chatter) {
        final PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(joinedKey, STRING_LIST, List.copyOf(chatter.joined));
        data.set(leftKey, STRING_LIST, List.copyOf(chatter.left));
        final String focus = chatter.focus();
        if (focus == null) {
            data.remove(focusKey);
        } else {
            data.set(focusKey, PersistentDataType.STRING, focus);
        }
    }
}
