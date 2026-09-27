package me.danieli1818.drchannels.chat;

import me.danieli1818.drchannels.channel.Channel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChannelChatServiceTest {

    private static final Channel CHANNEL = new Channel("trade", "global", "Trade", "[T 100%] ",
            (sender, recipient) -> true, null, null, false, true, List.of());

    @Test
    void replacesPlaceholderWithEscapedTag() {
        assertEquals("<[T 100%%] %1$s> %2$s", ChannelChatService.applyTag("<{CHANNEL}%1$s> %2$s", CHANNEL));
    }

    @Test
    void prependsTagWhenPlaceholderIsMissing() {
        assertEquals("[T 100%%] <%1$s> %2$s", ChannelChatService.applyTag("<%1$s> %2$s", CHANNEL));
    }
}
