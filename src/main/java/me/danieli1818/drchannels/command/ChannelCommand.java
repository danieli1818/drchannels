package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.api.ChannelTypeRegistry;
import me.danieli1818.drchannels.channel.Channel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntSupplier;

/**
 * {@code /channel}. Dispatches to subcommands; otherwise {@code /channel <channel>} joins and focuses a
 * channel and {@code /channel <channel> <message>} sends one message to it.
 */
public final class ChannelCommand implements TabExecutor {

    private final CommandContext context;
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    private final JoinCommand join;
    private final ListCommand list;

    public ChannelCommand(CommandContext context, ChannelTypeRegistry types, IntSupplier reloader) {
        this.context = context;
        this.join = new JoinCommand(context);
        this.list = new ListCommand(context);
        register(join);
        register(new LeaveCommand(context));
        register(list);
        register(new InfoCommand(context));
        register(new CreateCommand(context, types, subCommands::containsKey));
        register(new DeleteCommand(context));
        register(new ReloadCommand(context, reloader));
    }

    private void register(SubCommand command) {
        subCommands.put(command.name(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showOverview(sender);
            return true;
        }
        final SubCommand subCommand = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subCommand != null) {
            dispatch(sender, subCommand, Arrays.copyOfRange(args, 1, args.length));
        } else if (!(sender instanceof Player player)) {
            context.messages().send(sender, "player-only");
        } else {
            join.findChannel(sender, args[0]).ifPresent(channel -> joinOrQuickSend(player, channel, args));
        }
        return true;
    }

    private void dispatch(CommandSender sender, SubCommand subCommand, String[] args) {
        if (!subCommand.isPermitted(sender)) {
            context.messages().send(sender, "no-permission");
        } else if (subCommand.playerOnly() && !(sender instanceof Player)) {
            context.messages().send(sender, "player-only");
        } else if (!subCommand.execute(sender, args)) {
            context.messages().send(sender, "usage", "usage", subCommand.usage());
        }
    }

    private void joinOrQuickSend(Player player, Channel channel, String[] args) {
        if (args.length == 1) {
            join.join(player, channel);
        } else if (!context.chatters().isMember(player, channel)) {
            context.messages().send(player, "leave.NOT_MEMBER", SubCommand.placeholders(channel));
        } else {
            context.chat().quickSend(player, channel, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        }
    }

    private void showOverview(CommandSender sender) {
        if (sender instanceof Player player) {
            context.chatters().focus(player).ifPresentOrElse(
                    focus -> context.messages().send(sender, "current", SubCommand.placeholders(focus)),
                    () -> context.messages().send(sender, "not-in-channel"));
        }
        list.execute(sender, new String[0]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            final List<String> completions = new ArrayList<>(join.complete(sender, args));
            subCommands.values().stream()
                    .filter(subCommand -> subCommand.isPermitted(sender))
                    .map(SubCommand::name)
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                    .forEach(completions::add);
            return completions;
        }
        final SubCommand subCommand = subCommands.get(args[0].toLowerCase(Locale.ROOT));
        return subCommand != null && subCommand.isPermitted(sender)
                ? subCommand.complete(sender, Arrays.copyOfRange(args, 1, args.length))
                : new ArrayList<>();
    }
}
