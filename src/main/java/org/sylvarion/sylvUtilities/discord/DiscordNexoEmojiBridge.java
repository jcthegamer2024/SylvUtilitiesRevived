package org.sylvarion.sylvUtilities.discord;

import com.nexomc.nexo.glyphs.GlyphTag;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sylvarion.sylvUtilities.SylvUtilities;

/** Converts only configured aliases; Discord message text is never parsed as MiniMessage. */
public final class DiscordNexoEmojiBridge {
    private static final Pattern EMOJI = Pattern.compile("<a?:([A-Za-z0-9_]+):[0-9]+>|:([A-Za-z0-9_]+):");
    private final SylvUtilities plugin;
    private Map<String, Component> replacements = Map.of();
    private boolean subscribed;

    public DiscordNexoEmojiBridge(final SylvUtilities plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        final var config = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "discord-emojis.yml"));
        final var section = config.getConfigurationSection("emojis");
        final Map<String, Component> loaded = new HashMap<>();
        if (section != null) {
            final var miniMessage = MiniMessage.builder().tags(GlyphTag.INSTANCE.getRESOLVER()).build();
            for (final String alias : section.getKeys(false)) {
                final Object value = section.get(alias);
                if (!alias.matches("[A-Za-z0-9_]+") || !(value instanceof String glyph)
                        || !glyph.matches("[a-zA-Z0-9_./:-]+")) {
                    this.plugin.getLogger().warning("Invalid emoji mapping: " + alias + "; expected an emoji name and glyph ID.");
                    continue;
                }
                try {
                    final String tag = "<glyph:" + glyph + ">";
                    final var component = miniMessage.deserialize(tag);
                    if (net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                            .serialize(component).contains("<glyph:")) {
                        throw new IllegalArgumentException("Unknown glyph: " + glyph);
                    }
                    // DiscordSRV shades Adventure. JSON preserves fonts/styles across the two APIs.
                    loaded.put(alias, GsonComponentSerializer.gson().deserialize(
                            net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(component)));
                } catch (RuntimeException exception) {
                    this.plugin.getLogger().warning("Skipping emoji " + alias + ": " + exception.getMessage());
                }
            }
        }
        this.replacements = Map.copyOf(loaded);
        DiscordSRV.api.subscribe(this);
        this.subscribed = true;
        this.plugin.getLogger().info("DiscordNexoEmojiBridge enabled with " + loaded.size() + " emoji mappings.");
    }

    @Subscribe
    public void onDiscordMessage(final DiscordGuildMessagePostProcessEvent event) {
        if (event.isCancelled() || this.replacements.isEmpty()) return;
        event.setMinecraftMessage(translate(event.getMinecraftMessage(), this.replacements));
    }

    static Component translate(final Component message, final Map<String, Component> replacements) {
        return message.replaceText(builder -> builder
                .match(EMOJI)
                .replacement((match, original) -> replacements.getOrDefault(
                        match.group(1) != null ? match.group(1) : match.group(2), original.build())));
    }

    public void disable() {
        if (this.subscribed) {
            DiscordSRV.api.unsubscribe(this);
            this.subscribed = false;
        }
    }
}
