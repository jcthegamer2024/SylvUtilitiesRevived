# Text replacements

Enable this independent module in `plugins/SylvUtilities/config.yml`:

```yaml
modules:
  text-replacements:
    enabled: true
```

Edit `plugins/SylvUtilities/text-replacements.yml`:

```yaml
replacements:
  "[u]": "Ⓤ"
  "[heart]": "♥"
```

Use `/sylv reload` (OP or `sylvutilities.reload`) after editing either file. The response and startup console identify the active chat integration and mapping count.

With CarbonChat installed on the Paper server, the module uses its `CarbonChatEvent` API and changes the message body within Carbon's existing channel/rendering flow. Otherwise it listens to Paper's `AsyncChatEvent`. Only one integration runs, avoiding duplicate replacements. CarbonChat API 3.0.0-beta.39 is the compile target; proxy-only Carbon installations are outside this server plugin's scope.

Replacements are literal and case-sensitive, with longer matches taking priority. Output is plain text, not MiniMessage, and is not replaced again. Existing surrounding formatting is preserved. Empty values remove matching text; empty keys are invalid. This module handles Minecraft chat messages, not commands, item lore, or all server text, and needs neither Nexo nor DiscordSRV.
