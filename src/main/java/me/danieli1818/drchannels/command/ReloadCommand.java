package me.danieli1818.drchannels.command;

import org.bukkit.command.CommandSender;

import java.util.function.BooleanSupplier;

final class ReloadCommand extends SubCommand {

    private final BooleanSupplier reloader;

    /**
     * @param reloader reloads the plugin, returning {@code false} if channels.yml could not be read
     */
    ReloadCommand(CommandContext context, BooleanSupplier reloader) {
        super(context, "reload", "drchannels.reload", "", false);
        this.reloader = reloader;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (reloader.getAsBoolean()) {
            messages.send(sender, "reload", "count", String.valueOf(channels.all().size()));
        } else {
            messages.send(sender, "reload-failed");
        }
        return true;
    }
}
