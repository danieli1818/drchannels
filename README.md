# DRChannels

Chat channels for Spigot and Paper 1.21+, with optional EssentialsX integration.

- **Channel types**: `global`, `world`, `local` (radius), `worlds` (list of worlds) and `team` (scoreboard team). Other plugins can register their own.
- **Permissions** decide who may join, read and speak in each channel, and are checked for every message.
- **Default channel**: everyone can join it and is placed in it on every login. Chat falls back to it when a player leaves their focused channel.
- **EssentialsX**: when EssentialsChat is installed, its chat formats, group formats and prefixes are used. Channels only add their tag and restrict recipients. Essentials `/ignore`, `/mute` and vanish keep working because DRChannels only ever *removes* recipients.

## Installation

Drop the jar into `plugins/`. EssentialsX and EssentialsChat are optional.

With EssentialsChat, put `{CHANNEL}` in `chat.format` / `group-formats` in the Essentials config to choose where the tag goes (otherwise it is prepended), and keep `chat.radius: 0`. Use a `local` channel instead of Essentials' radius.

```yaml
chat:
  radius: 0
  format: '{CHANNEL}{DISPLAYNAME}&7: &r{MESSAGE}'
```

Without EssentialsChat, the `format` in `plugins/DRChannels/config.yml` is used.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/ch` | Show your focused channel and all channels | `drchannels.use` |
| `/ch <channel>` | Join (if needed) and talk in a channel | channel permission |
| `/ch <channel> <message>` | Send one message to a channel you're in | channel permission |
| `/ch join <channel>` / `/ch leave [channel]` | Join or leave a channel | channel permission |
| `/ch list` / `/ch info <channel>` | List channels / show a channel's settings | `drchannels.use` |
| `/ch create <id> <type> [key=value ...]` | Create a channel, saved to channels.yml | `drchannels.create` |
| `/ch delete <channel>` | Delete a channel | `drchannels.delete` |
| `/ch reload` | Reload config.yml and channels.yml | `drchannels.reload` |

Aliases: `/channel`, `/ch`, `/drc`, `/drchannels`. Example:
`/ch create market global tag="&a[M] " permission=none aliases=m,mk auto-join=true`

## Channel permissions

Each channel requires `drchannels.channel.<id>` to join and read, unless `permission:` sets another node or `none`. `speak-permission:` can require a different node for talking (for read-only channels). `drchannels.admin` grants everything.

See `channels.yml` for all options.

## Adding a channel type from another plugin

Depend on DRChannels and register the type in your `onEnable` (channels load on the first server tick):

```java
DRChannels drChannels = JavaPlugin.getPlugin(DRChannels.class);
drChannels.getChannelTypes().register("same-gamemode", options ->
        (sender, recipient) -> sender.getGameMode() == recipient.getGameMode());
```

A type returns a `ChannelScope`: `reaches(sender, recipient)` decides who hears a message, and the optional `canSpeak(sender)` decides whether the sender can talk right now. Scopes are called from the async chat thread and must be thread-safe. Read type options with `ChannelOptions`, and throw `InvalidChannelException` for bad options.

## Building

```
mvn package
```

Requires Java 21. The jar is written to `target/`.
