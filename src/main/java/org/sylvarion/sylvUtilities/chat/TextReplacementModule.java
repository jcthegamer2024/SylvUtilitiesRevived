package org.sylvarion.sylvUtilities.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.File;
import java.util.HashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.sylvarion.sylvUtilities.SylvUtilities;

public final class TextReplacementModule implements Listener {
    private final SylvUtilities plugin;
    private TextReplacements rules = new TextReplacements(java.util.Map.of());
    private CarbonTextReplacementHook carbonHook;

    public TextReplacementModule(final SylvUtilities plugin) {
        this.plugin = plugin;
    }

    public String enable() throws java.io.IOException, org.bukkit.configuration.InvalidConfigurationException {
        final var config = new YamlConfiguration();
        config.load(new File(this.plugin.getDataFolder(), "text-replacements.yml"));
        final var mappings = new HashMap<String, String>();
        final var section = config.getConfigurationSection("replacements");
        if (section != null) {
            section.getValues(false).forEach((key, value) -> {
                if (key.isEmpty() || !(value instanceof String)) {
                    this.plugin.getLogger().warning("Skipping invalid text replacement: " + key);
                } else {
                    mappings.put(key, (String) value);
                }
            });
        }
        this.rules = new TextReplacements(mappings);
        final boolean carbon = this.plugin.getServer().getPluginManager().isPluginEnabled("CarbonChat");
        if (carbon) {
            this.carbonHook = new CarbonTextReplacementHook(this.rules);
        } else {
            this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        }
        final String status = "Text replacements enabled with " + this.rules.size()
                + " mappings using " + (carbon ? "CarbonChat" : "Paper") + ".";
        this.plugin.getLogger().info(status);
        return "<green>" + status;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(final AsyncChatEvent event) {
        event.message(this.rules.apply(event.message()));
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.carbonHook != null) {
            this.carbonHook.disable();
            this.carbonHook = null;
        }
    }
}
