package me.danieli1818.drchannels.api;

import org.bukkit.entity.Player;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Decides which players a channel message reaches. Implementations are queried from the async chat
 * thread, so they must be thread-safe and must not mutate world state.
 */
@FunctionalInterface
public interface ChannelScope {

    /**
     * @return whether a message from {@code sender} reaches {@code recipient}. Membership and
     *         permissions are checked separately; only spatial/logical scope belongs here.
     */
    boolean reaches(Player sender, Player recipient);

    /**
     * @return whether {@code sender} may currently speak in the channel (e.g. is in a supported world).
     */
    default boolean canSpeak(Player sender) {
        return true;
    }

    static ChannelScope of(BiPredicate<Player, Player> reaches, Predicate<Player> canSpeak) {
        return new ChannelScope() {
            @Override
            public boolean reaches(Player sender, Player recipient) {
                return reaches.test(sender, recipient);
            }

            @Override
            public boolean canSpeak(Player sender) {
                return canSpeak.test(sender);
            }
        };
    }
}
