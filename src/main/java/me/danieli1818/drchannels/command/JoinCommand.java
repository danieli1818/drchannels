package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.channel.Channel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

final class JoinCommand extends SubCommand {

    JoinCommand(CommandContext context) {
        super(context, "join", null, "<channel>", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return false;
        }
        findChannel(sender, args[0]).ifPresent(channel -> join((Player) sender, channel));
        return true;
    }

    /**
     * Joins (if needed) and focuses the channel. Also used by {@code /channel <channel>}.
     */
    void join(Player player, Channel channel) {
        messages.send(player, "join." + chatters.join(player, channel).name(), placeholders(channel));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? completeChannels(sender, args[0], channel -> channel.canJoin(sender)) : List.of();
    }
}
