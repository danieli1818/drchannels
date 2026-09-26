package me.danieli1818.drchannels.command;

import org.bukkit.command.CommandSender;

import java.util.function.IntSupplier;

final class ReloadCommand extends SubCommand {

    private final IntSupplier reloader;

    /**
     * @param reloader reloads the plugin and returns the number of loaded channels
     */
    ReloadCommand(CommandContext context, IntSupplier reloader) {
        super(context, "reload", "drchannels.reload", "", false);
        this.reloader = reloader;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        messages.send(sender, "reload", "count", String.valueOf(reloader.getAsInt()));
        return true;
    }
}
