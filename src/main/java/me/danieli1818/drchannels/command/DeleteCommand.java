package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.channel.Channel;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;

final class DeleteCommand extends SubCommand {

    DeleteCommand(CommandContext context) {
        super(context, "delete", "drchannels.delete", "<channel>", false);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return false;
        }
        findChannel(sender, args[0]).ifPresent(channel -> delete(sender, channel));
        return true;
    }

    private void delete(CommandSender sender, Channel channel) {
        if (channels.isDefault(channel)) {
            messages.send(sender, "delete.default");
            return;
        }
        try {
            channels.delete(channel);
            chatters.purge(channel.id());
            messages.send(sender, "delete.success", placeholders(channel));
        } catch (IOException e) {
            messages.send(sender, "save-failed", "reason", e.getMessage());
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? completeChannels(sender, args[0], channel -> !channels.isDefault(channel)) : List.of();
    }
}
