package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.channel.Channel;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

final class ListCommand extends SubCommand {

    ListCommand(CommandContext context) {
        super(context, "list", null, "", false);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        final Player player = sender instanceof Player p ? p : null;
        final Channel focus = player == null ? null : chatters.focus(player).orElse(null);
        messages.send(sender, "list.header");
        for (final Channel channel : channels.all()) {
            final boolean member = player != null && chatters.isMember(player, channel);
            if (member || channel.canJoin(sender)) {
                sender.sendMessage(messages.format("list.entry", placeholders(channel,
                        "state", state(channel, focus, member).toString(),
                        "aliases", aliases(channel))));
            }
        }
        return true;
    }

    private static ChatColor state(Channel channel, @Nullable Channel focus, boolean member) {
        if (focus != null && channel.id().equals(focus.id())) {
            return ChatColor.YELLOW;
        }
        return member ? ChatColor.GREEN : ChatColor.DARK_GRAY;
    }

    private String aliases(Channel channel) {
        return channel.aliases().isEmpty() ? "" : messages.format("list.aliases", "aliases", String.join(", ", channel.aliases()));
    }
}
