package me.danieli1818.drchannels.channel;

import me.danieli1818.drchannels.api.ChannelOptions;
import me.danieli1818.drchannels.api.ChannelScope;
import me.danieli1818.drchannels.api.ChannelTypeRegistry;
import me.danieli1818.drchannels.api.InvalidChannelException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The channel types shipped with DRChannels.
 */
public final class BuiltInChannelTypes {

    private BuiltInChannelTypes() {
    }

    public static void registerAll(ChannelTypeRegistry registry) {
        registry.register("global", options -> (sender, recipient) -> true);
        registry.register("world", options -> BuiltInChannelTypes::sameWorld);
        registry.register("local", BuiltInChannelTypes::local);
        registry.register("worlds", BuiltInChannelTypes::worlds);
        registry.register("team", options -> ChannelScope.of(BuiltInChannelTypes::sameTeam, sender -> teamOf(sender) != null));
    }

    private static ChannelScope local(ConfigurationSection options) throws InvalidChannelException {
        final double radius = ChannelOptions.positiveDouble(options, "radius");
        final double radiusSquared = radius * radius;
        return (sender, recipient) -> sameWorld(sender, recipient)
                && sender.getLocation().distanceSquared(recipient.getLocation()) <= radiusSquared;
    }

    private static ChannelScope worlds(ConfigurationSection options) throws InvalidChannelException {
        final Set<String> worlds = ChannelOptions.nonEmptyList(options, "worlds").stream()
                .map(world -> world.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        final Predicate<Player> inWorlds = player -> worlds.contains(player.getWorld().getName().toLowerCase(Locale.ROOT));
        return ChannelScope.of((sender, recipient) -> inWorlds.test(recipient), inWorlds);
    }

    private static boolean sameWorld(Player sender, Player recipient) {
        return sender.getWorld().equals(recipient.getWorld());
    }

    private static boolean sameTeam(Player sender, Player recipient) {
        final Team team = teamOf(sender);
        return team != null && team.hasEntry(recipient.getName());
    }

    private static @Nullable Team teamOf(Player player) {
        return player.getScoreboard().getEntryTeam(player.getName());
    }
}
