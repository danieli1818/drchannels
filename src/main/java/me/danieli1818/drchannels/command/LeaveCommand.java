package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.channel.Channel;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

final class LeaveCommand extends SubCommand {

    LeaveCommand(CommandContext context) {
        super(context, "leave", null, "[channel]", true);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return false;
        }
        final Player player = (Player) sender;
        final Optional<Channel> channel = args.length == 1 ? findChannel(sender, args[0]) : chatters.focus(player);
        if (args.length == 0 && channel.isEmpty()) {
            messages.send(sender, "not-in-channel");
        }
        channel.ifPresent(target -> messages.send(sender, "leave." + chatters.leave(player, target).name(), placeholders(target)));
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 && sender instanceof Player player
                ? completeChannels(args[0], channel -> chatters.isMember(player, channel))
                : List.of();
    }
}
