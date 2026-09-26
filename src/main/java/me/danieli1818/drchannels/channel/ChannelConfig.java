package me.danieli1818.drchannels.channel;

import me.danieli1818.drchannels.api.ChannelOptions;
import me.danieli1818.drchannels.api.ChannelScope;
import me.danieli1818.drchannels.api.ChannelType;
import me.danieli1818.drchannels.api.ChannelTypeRegistry;
import me.danieli1818.drchannels.api.InvalidChannelException;
import me.danieli1818.drchannels.util.Text;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Reads and writes {@code channels.yml}. {@link #parse} is the single place a channel definition is
 * turned into a {@link Channel}, used both for the file and for {@code /channel create}.
 */
public final class ChannelConfig {

    public static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_-]{1,32}");
    public static final String OPEN_PERMISSION = "none";
    private static final String ROOT = "channels";

    private final ChannelTypeRegistry types;
    private final File file;
    private final Logger logger;
    private YamlConfiguration yaml = new YamlConfiguration();

    public ChannelConfig(ChannelTypeRegistry types, File file, Logger logger) {
        this.types = types;
        this.file = file;
        this.logger = logger;
    }

    /**
     * Reloads the file and parses every channel, logging and skipping invalid ones.
     *
     * @return channels by id, in file order
     */
    public Map<String, Channel> load(@Nullable String defaultId) {
        yaml = YamlConfiguration.loadConfiguration(file);
        final Map<String, Channel> channels = new LinkedHashMap<>();
        final ConfigurationSection root = yaml.getConfigurationSection(ROOT);
        if (root == null) {
            return channels;
        }
        for (final String id : root.getKeys(false)) {
            final ConfigurationSection section = root.getConfigurationSection(id);
            try {
                if (section == null) {
                    throw new InvalidChannelException("definition must be a section");
                }
                final Channel channel = parse(id, section, id.equals(defaultId));
                channels.put(channel.id(), channel);
            } catch (InvalidChannelException e) {
                logger.warning("Skipping channel '" + id + "': " + e.getMessage());
            }
        }
        return channels;
    }

    /**
     * @param open whether to ignore permissions and let everyone join and speak (used for the default channel)
     */
    public Channel parse(String id, ConfigurationSection section, boolean open) throws InvalidChannelException {
        if (!ID_PATTERN.matcher(id).matches()) {
            throw new InvalidChannelException("id must match " + ID_PATTERN.pattern());
        }
        final String typeId = section.getString("type");
        if (typeId == null) {
            throw new InvalidChannelException("missing 'type' (available: " + String.join(", ", types.ids()) + ")");
        }
        final ChannelType type = types.find(typeId)
                .orElseThrow(() -> new InvalidChannelException("unknown type '" + typeId + "' (available: " + String.join(", ", types.ids()) + ")"));
        final ChannelScope scope = type.create(section);

        final String joinPermission = open ? null : permission(section.getString("permission", "drchannels.channel." + id));
        final String speakPermission = open ? null : section.isSet("speak-permission")
                ? permission(section.getString("speak-permission"))
                : joinPermission;
        final List<String> aliases = ChannelOptions.stringList(section, "aliases").stream()
                .map(alias -> alias.toLowerCase(Locale.ROOT))
                .toList();

        return new Channel(
                id,
                typeId.toLowerCase(Locale.ROOT),
                section.getString("display-name", id),
                Text.colorize(section.getString("tag", "")),
                scope,
                joinPermission,
                speakPermission,
                section.getBoolean("auto-join", false),
                section.getBoolean("leavable", true),
                aliases);
    }

    public boolean contains(String id) {
        return yaml.isConfigurationSection(ROOT + "." + id);
    }

    public void save(String id, ConfigurationSection section) throws IOException {
        yaml.createSection(ROOT + "." + id, section.getValues(false));
        yaml.save(file);
    }

    public void remove(String id) throws IOException {
        yaml.set(ROOT + "." + id, null);
        yaml.save(file);
    }

    private static @Nullable String permission(@Nullable String value) {
        return value == null || value.isBlank() || value.equalsIgnoreCase(OPEN_PERMISSION) ? null : value;
    }
}
