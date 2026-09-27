package me.danieli1818.drchannels.channel;

import me.danieli1818.drchannels.api.ChannelScope;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Immutable channel definition. A {@code null} permission means everyone is allowed.
 *
 * @param tag already colourised text inserted at {@code {CHANNEL}}
 */
public record Channel(
        String id,
        String type,
        String displayName,
        String tag,
        ChannelScope scope,
        @Nullable String joinPermission,
        @Nullable String speakPermission,
        boolean autoJoin,
        boolean leavable,
        List<String> aliases
) {

    /**
     * Grants access to every channel. Checked explicitly because Bukkit does not expand wildcard nodes.
     */
    public static final String BYPASS_PERMISSION = "drchannels.channel.*";

    public Channel {
        aliases = List.copyOf(aliases);
    }

    public boolean canJoin(Permissible permissible) {
        return isAllowed(permissible, joinPermission);
    }

    /**
     * @return whether the player may join the channel, may speak in it, and is currently in its scope
     */
    public boolean canSpeak(Player player) {
        return canJoin(player) && isAllowed(player, speakPermission) && scope.canSpeak(player);
    }

    public boolean reaches(Player sender, Player recipient) {
        return scope.reaches(sender, recipient);
    }

    private static boolean isAllowed(Permissible permissible, @Nullable String permission) {
        return permission == null || permissible.hasPermission(permission) || permissible.hasPermission(BYPASS_PERMISSION);
    }
}
