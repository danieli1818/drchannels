package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.channel.Channel;
import me.danieli1818.drchannels.channel.ChannelRegistry;
import me.danieli1818.drchannels.chatter.ChatterManager;
import me.danieli1818.drchannels.util.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@code /channel <name> ...} subcommand. Permission and player-only checks are done by
 * {@link ChannelCommand} before {@link #execute} is called.
 */
public abstract class SubCommand {

    protected final CommandContext context;
    protected final ChannelRegistry channels;
    protected final ChatterManager chatters;
    protected final Messages messages;
    private final String name;
    private final @Nullable String permission;
    private final String usage;
    private final boolean playerOnly;

    protected SubCommand(CommandContext context, String name, @Nullable String permission, String usage, boolean playerOnly) {
        this.context = context;
        this.channels = context.channels();
        this.chatters = context.chatters();
        this.messages = context.messages();
        this.name = name;
        this.permission = permission;
        this.usage = usage;
        this.playerOnly = playerOnly;
    }

    /**
     * @param args arguments after the subcommand name
     * @return {@code false} to show the usage message
     */
    public abstract boolean execute(CommandSender sender, String[] args);

    /**
     * @param args arguments after the subcommand name; the last one is being completed
     */
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }

    public final String name() {
        return name;
    }

    public final String usage() {
        return "/channel " + name + (usage.isEmpty() ? "" : " " + usage);
    }

    public final boolean playerOnly() {
        return playerOnly;
    }

    public final boolean isPermitted(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }

    /**
     * A channel is visible to a sender who may join it or is already in it; other channels are treated as
     * nonexistent so permission-gated channels are not revealed.
     */
    protected boolean isVisible(CommandSender sender, Channel channel) {
        return channel.canJoin(sender) || (sender instanceof Player player && chatters.isMember(player, channel));
    }

    /**
     * Finds a visible channel by id or alias, telling the sender if there is none.
     */
    protected Optional<Channel> findChannel(CommandSender sender, String name) {
        final Optional<Channel> channel = channels.find(name).filter(found -> isVisible(sender, found));
        if (channel.isEmpty()) {
            messages.send(sender, "unknown-channel", "channel", name);
        }
        return channel;
    }

    /**
     * Completes ids and aliases of visible channels matching {@code filter}.
     */
    protected List<String> completeChannels(CommandSender sender, String prefix, Predicate<Channel> filter) {
        final Stream<String> names = channels.all().stream()
                .filter(channel -> isVisible(sender, channel) && filter.test(channel))
                .flatMap(channel -> Stream.concat(Stream.of(channel.id()), channel.aliases().stream()));
        return StringUtil.copyPartialMatches(prefix, names::iterator, new ArrayList<>());
    }

    /**
     * @return the standard {@code channel}, {@code tag} and {@code name} placeholders followed by {@code extra}
     */
    protected static String[] placeholders(Channel channel, String... extra) {
        final String[] base = {"channel", channel.id(), "tag", channel.tag(), "name", channel.displayName()};
        final String[] all = Arrays.copyOf(base, base.length + extra.length);
        System.arraycopy(extra, 0, all, base.length, extra.length);
        return all;
    }
}
