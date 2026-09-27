package me.danieli1818.drchannels.command;

import me.danieli1818.drchannels.api.ChannelTypeRegistry;
import me.danieli1818.drchannels.api.InvalidChannelException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code /channel create <id> <type> [key=value ...]}. Values may be quoted ({@code tag="&a[M] "});
 * comma-separated values become lists, and numbers and booleans are parsed.
 */
final class CreateCommand extends SubCommand {

    private static final Pattern OPTION = Pattern.compile("\\s*([a-z0-9-]+)=(?:\"([^\"]*)\"|(\\S+))");
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern DECIMAL = Pattern.compile("-?\\d+\\.\\d+");
    private static final List<String> OPTION_KEYS = List.of(
            "display-name=", "tag=", "aliases=", "permission=", "speak-permission=", "auto-join=", "leavable=", "radius=", "worlds=");

    private final ChannelTypeRegistry types;
    private final Predicate<String> reservedName;

    CreateCommand(CommandContext context, ChannelTypeRegistry types, Predicate<String> reservedName) {
        super(context, "create", "drchannels.create", "<id> <type> [key=value ...]", false);
        this.types = types;
        this.reservedName = reservedName;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }
        final String id = args[0].toLowerCase(Locale.ROOT);
        final MemoryConfiguration definition = new MemoryConfiguration();
        definition.set("type", args[1].toLowerCase(Locale.ROOT));
        if (!parseOptions(String.join(" ", Arrays.copyOfRange(args, 2, args.length)), definition)) {
            return false;
        }

        try {
            if (reservedName.test(id)) {
                throw new InvalidChannelException("'" + id + "' is a reserved command name");
            }
            channels.create(id, definition);
            messages.send(sender, "create.success", "channel", id);
        } catch (IllegalArgumentException e) {
            messages.send(sender, "create.exists", "channel", id);
        } catch (InvalidChannelException e) {
            messages.send(sender, "create.invalid", "reason", e.getMessage());
        } catch (IOException e) {
            messages.send(sender, "save-failed", "reason", e.getMessage());
        }
        return true;
    }

    /**
     * @return {@code false} if the text is not a sequence of {@code key=value} options
     */
    private static boolean parseOptions(String text, MemoryConfiguration target) {
        final Matcher matcher = OPTION.matcher(text);
        int end = 0;
        while (matcher.find() && matcher.start() == end) {
            final String quoted = matcher.group(2);
            target.set(matcher.group(1), quoted != null ? quoted : parseValue(matcher.group(3)));
            end = matcher.end();
        }
        return text.substring(end).isBlank();
    }

    private static Object parseValue(String raw) {
        if (raw.contains(",")) {
            return Arrays.stream(raw.split(",")).map(String::trim).filter(value -> !value.isEmpty()).toList();
        }
        if (raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(raw);
        }
        if (INTEGER.matcher(raw).matches()) {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException outOfIntRange) {
                return Double.parseDouble(raw);
            }
        }
        if (DECIMAL.matcher(raw).matches()) {
            return Double.parseDouble(raw);
        }
        return raw;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 2 -> StringUtil.copyPartialMatches(args[1], types.ids(), new ArrayList<>());
            case 1 -> List.of();
            default -> StringUtil.copyPartialMatches(args[args.length - 1], OPTION_KEYS, new ArrayList<>());
        };
    }
}
