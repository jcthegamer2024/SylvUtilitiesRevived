# DiscordNexoEmojiBridge

Install DiscordSRV 1.30.5+ and Nexo 1.29.1+, then start SylvUtilities once to generate its config files.

Enable the bridge in `plugins/SylvUtilities/config.yml`:

```yaml
modules:
  discord-nexo-emoji-bridge:
    enabled: true
```

Edit `plugins/SylvUtilities/discord-emojis.yml`. Each entry maps a Discord emoji name to an existing Nexo glyph ID:

```yaml
emojis:
  lmfao: lmfao
  happy: smile
  party: party_parrot
```

Restart the server to apply changes. `:lmfao:` becomes the Nexo `lmfao` glyph in Discord-to-Minecraft chat. Discord custom emojis and animated custom emojis are supported by name too: DiscordSRV normally converts their markup into `:name:` before the bridge runs. Names are case-sensitive; unmapped emojis stay unchanged. Unknown glyphs and invalid entries are skipped with a console warning.

DiscordSRV must already relay the channel to Minecraft. Configure its `DiscordChatChannelEmojiBehavior` to `name` for standard Unicode emoji aliases, or `show` to leave Unicode emojis visible. Emoji content discarded by DiscordSRV filters cannot be recovered by this bridge.

Players need the Nexo resource pack. Only configured glyph IDs are parsed; Discord message text is never interpreted as MiniMessage. The bridge preserves the surrounding message components and does not relay messages itself. Missing dependencies disable only this module. The bridge targets DiscordSRV's post-process event; a chat-plugin hook that converts its final components back to legacy text may lose custom glyph fonts.
