# DRChannels

Chat channels plugin for Spigot/Paper 1.21+ (Java 21) with optional EssentialsX integration.

## Build & test

- `mvn clean package` builds `target/DRChannels-<version>.jar` and runs the JUnit 5 tests.
- Always use `clean` if you see odd errors such as "Unable to create test class": the VS Code Java
  language server also compiles into `target/`.
- Tests cover pure logic only (`Text`, `ChannelConfig`, tag formatting). Bukkit config classes work without a
  server; anything needing a `Player` must be verified on a test server.

## Architecture (`me.danieli1818.drchannels`)

| Package | Role |
|---|---|
| `api` | Public extension API: `ChannelType` → `ChannelScope`, `ChannelTypeRegistry`, `ChannelOptions`, `InvalidChannelException` |
| `channel` | `Channel` (immutable record), `BuiltInChannelTypes`, `ChannelConfig` (channels.yml I/O + the single parser), `ChannelRegistry` (atomic snapshot) |
| `chatter` | Per-player membership/focus. `ChatterManager` is the only mutator; state lives in the player's PersistentDataContainer |
| `chat` | `ChannelChatService` makes every routing decision; the listeners only delegate to it |
| `command` | `/channel` dispatcher (`ChannelCommand`) + one `SubCommand` subclass per subcommand |
| `util` | `Text` (colours, format escaping), `Messages` (config.yml `messages:`) |

## Invariants — do not break these

- **Recipients are filtered on Bukkit's `AsyncPlayerChatEvent` at `LOW`, never on EssentialsChat's events.**
  On Paper, `GlobalChatEvent`/`LocalChatEvent` expose an unmodifiable recipient set; modifying it throws and
  the message leaks to everyone. `EssentialsChatListener` only tags the format. `LOW` sits between
  EssentialsChat's format (`LOWEST`) and its events (`NORMAL`), so the routed channel is known when tagging.
- **Only remove recipients** (`removeIf`), never clear/re-add — Essentials ignore, mute and vanish rely on it.
  If a recipient set is immutable, cancel the message (fail closed).
- **`EssentialsChatListener` is the only class that imports EssentialsX.** Essentials is a softdepend; it is
  registered only after checking the plugin and class exist.
- **Async safety:** `ChannelScope`s, `ChatterManager.isMember/focus` and the registry are read from the async
  chat thread. Keep them thread-safe and side-effect free; all mutations happen on the main thread.
- **channels.yml is never overwritten from stale or invalid state:** `ChannelConfig` re-reads the file before
  every write and refuses to load or save invalid YAML. A failed reload keeps the current channels.
- **`reconcile` never removes memberships**; only `/ch delete` (via `ChatterManager.purge`) does. Routing
  already rechecks permissions on every message.
- **Permissions:** Bukkit does not expand wildcard child permissions, so `Channel.BYPASS_PERMISSION`
  (`drchannels.channel.*`) is checked explicitly. Speaking requires join permission as well.
- **Visibility:** commands treat channels a sender cannot join (and is not in) as nonexistent. Use
  `SubCommand.findChannel` / `completeChannels`, which apply this rule.

## Conventions

- One code path per concern: parse channel definitions only through `ChannelConfig.parse`, route only through
  `ChannelChatService`, mutate membership only through `ChatterManager`.
- New channel type: register a lambda in `BuiltInChannelTypes`, read options with `ChannelOptions`, throw
  `InvalidChannelException` with an admin-readable message, and document it in the `channels.yml` header,
  the README and `CreateCommand.OPTION_KEYS`.
- New message: add it to `src/main/resources/config.yml` under `messages:`. Old server configs get it through
  `copyDefaults`. Command results map to keys as `<command>.<ENUM_NAME>`.
- Style: 4-space indent, `final` locals, records for immutable data, Javadoc on non-obvious public members.

## Releases

- `main` is the default branch. Tags are `vX.Y.Z` on `main`; `v1.0.0` is the original pre-rewrite code.
- For a release, set `pom.xml` `<version>` to `X.Y.Z` (no `-SNAPSHOT`) in the tagged commit;
  `plugin.yml` gets the version through resource filtering.
