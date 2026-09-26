package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.channel.Channel;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class InfoCommand extends SubCommand {

    InfoCommand(CommandContext context) {
        super(context, "info", null, "<channel>", false);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return false;
        }
        findChannel(sender, args[0]).ifPresent(channel -> messages.send(sender, "info", placeholders(channel,
                "type", channel.type(),
                "join-permission", permission(channel.joinPermission()),
                "speak-permission", permission(channel.speakPermission()),
                "auto-join", String.valueOf(channel.autoJoin()),
                "leavable", String.valueOf(channel.leavable()),
                "aliases", channel.aliases().isEmpty() ? "-" : String.join(", ", channel.aliases()))));
        return true;
    }

    private static String permission(@Nullable String permission) {
        return permission == null ? "-" : permission;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? completeChannels(args[0], channel -> true) : List.of();
    }
}
