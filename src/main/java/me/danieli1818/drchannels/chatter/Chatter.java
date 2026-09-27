package me.danieli1818.drchannels.chatter;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player channel state. Mutated only by {@link ChatterManager} on the main thread; safely readable
 * from the async chat thread.
 */
final class Chatter {

    /** Channels the player is in. */
    final Set<String> joined = ConcurrentHashMap.newKeySet();
    /** Auto-join channels the player explicitly left, so they are not re-joined on login. */
    final Set<String> left = ConcurrentHashMap.newKeySet();
    private volatile @Nullable String focus;

    Chatter(Collection<String> joined, Collection<String> left, @Nullable String focus) {
        this.joined.addAll(joined);
        this.left.addAll(left);
        this.focus = focus;
    }

    @Nullable String focus() {
        return focus;
    }

    void focus(@Nullable String focus) {
        this.focus = focus;
    }
}
