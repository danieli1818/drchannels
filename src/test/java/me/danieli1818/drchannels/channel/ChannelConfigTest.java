package me.danieli1818.drchannels.channel;

import me.danieli1818.drchannels.api.ChannelTypeRegistry;
import me.danieli1818.drchannels.api.InvalidChannelException;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelConfigTest {

    @TempDir
    Path directory;
    private File file;
    private ChannelConfig config;

    @BeforeEach
    void setUp() {
        final ChannelTypeRegistry types = new ChannelTypeRegistry();
        BuiltInChannelTypes.registerAll(types);
        file = directory.resolve("channels.yml").toFile();
        config = new ChannelConfig(types, file, Logger.getLogger("test"));
    }

    @Test
    void derivesDefaultPermissions() throws InvalidChannelException {
        final Channel channel = config.parse("staff", section("global"), false);
        assertEquals("drchannels.channel.staff", channel.joinPermission());
        assertEquals("drchannels.channel.staff", channel.speakPermission());
        assertTrue(channel.leavable());
        assertFalse(channel.autoJoin());
        assertEquals("staff", channel.displayName());
    }

    @Test
    void noneMeansOpenAndSpeakPermissionIsIndependent() throws InvalidChannelException {
        final MemoryConfiguration section = section("global");
        section.set("permission", "none");
        section.set("speak-permission", "news.post");
        final Channel channel = config.parse("news", section, false);
        assertNull(channel.joinPermission());
        assertEquals("news.post", channel.speakPermission());
    }

    @Test
    void defaultChannelIsAlwaysOpen() throws InvalidChannelException {
        final MemoryConfiguration section = section("global");
        section.set("permission", "some.permission");
        final Channel channel = config.parse("global", section, true);
        assertNull(channel.joinPermission());
        assertNull(channel.speakPermission());
    }

    @Test
    void acceptsScalarOrListAliases() throws InvalidChannelException {
        final MemoryConfiguration scalar = section("global");
        scalar.set("aliases", "G");
        assertEquals(List.of("g"), config.parse("global", scalar, false).aliases());

        final MemoryConfiguration list = section("global");
        list.set("aliases", List.of("a", "B"));
        assertEquals(List.of("a", "b"), config.parse("global", list, false).aliases());
    }

    @Test
    void rejectsInvalidDefinitions() {
        assertThrows(InvalidChannelException.class, () -> config.parse("Bad Id", section("global"), false));
        assertThrows(InvalidChannelException.class, () -> config.parse("x", new MemoryConfiguration(), false));
        assertThrows(InvalidChannelException.class, () -> config.parse("x", section("nope"), false));
        assertThrows(InvalidChannelException.class, () -> config.parse("x", section("local"), false));
        assertThrows(InvalidChannelException.class, () -> config.parse("x", section("worlds"), false));
    }

    @Test
    void loadSkipsInvalidChannelsAndSavesNewOnes() throws IOException {
        Files.writeString(file.toPath(), """
                channels:
                  global:
                    type: global
                  broken:
                    type: local
                """);
        final Map<String, Channel> loaded = config.load("global");
        assertEquals(List.of("global"), List.copyOf(loaded.keySet()));
        assertNull(loaded.get("global").joinPermission());

        final MemoryConfiguration local = section("local");
        local.set("radius", 50);
        config.save("near", local);
        assertTrue(config.load("global").containsKey("near"));
    }

    @Test
    void neverOverwritesAnUnreadableFile() throws IOException {
        final String broken = "channels:\n  global: [unclosed\n";
        Files.writeString(file.toPath(), broken);
        assertThrows(IOException.class, () -> config.load("global"));
        assertThrows(IOException.class, () -> config.save("market", section("global")));
        assertThrows(IOException.class, () -> config.remove("global"));
        assertEquals(broken, Files.readString(file.toPath()));
    }

    @Test
    void saveKeepsHandEditsAndRejectsExistingIds() throws IOException {
        Files.writeString(file.toPath(), "channels:\n  global:\n    type: global\n");
        config.load("global");
        Files.writeString(file.toPath(), "channels:\n  global:\n    type: global\n  trade:\n    type: global\n");

        config.save("market", section("global"));
        assertEquals(List.of("global", "trade", "market"), List.copyOf(config.load("global").keySet()));
        assertThrows(IllegalArgumentException.class, () -> config.save("trade", section("global")));
    }

    private static MemoryConfiguration section(String type) {
        final MemoryConfiguration section = new MemoryConfiguration();
        section.set("type", type);
        return section;
    }
}
