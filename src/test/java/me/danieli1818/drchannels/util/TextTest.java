package me.danieli1818.drchannels.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextTest {

    @Test
    void colorizesLegacyCodes() {
        assertEquals("§aHi §lthere", Text.colorize("&aHi &lthere"));
    }

    @Test
    void colorizesHexCodes() {
        assertEquals("§x§f§f§0§0§0§0Red", Text.colorize("&#ff0000Red"));
    }

    @Test
    void escapesPercentSigns() {
        assertEquals("100%% sure", Text.escapeFormat("100% sure"));
    }

    @Test
    void compilesChatFormat() {
        assertEquals("{CHANNEL}%1$s§7: §r%2$s 50%%", Text.compileChatFormat("{CHANNEL}{DISPLAYNAME}&7: &r{MESSAGE} 50%"));
    }
}
