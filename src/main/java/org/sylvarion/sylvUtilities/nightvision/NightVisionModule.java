package org.sylvarion.sylvUtilities.nightvision;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.sylvarion.sylvUtilities.SylvUtilities;

public final class NightVisionModule {
    public static final String PERMISSION = "sylvutilities.nightvision";

    private final SylvUtilities plugin;

    public NightVisionModule(final SylvUtilities plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        this.plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        Commands.literal("nv")
                                .requires(source -> source.getSender().hasPermission(PERMISSION))
                                .executes(context -> {
                                    if (context.getSource().getSender() instanceof final Player player) {
                                        this.toggle(player);
                                    } else {
                                        this.plugin.msg(context.getSource().getSender(),
                                                "<red>Only players can use this command.");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                                .build(),
                        "Toggle infinite night vision"));
    }

    private void toggle(final Player player) {
        final PotionEffect current = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        final boolean alreadyOn = current != null && current.getDuration() == PotionEffect.INFINITE_DURATION;

        if (alreadyOn) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            this.plugin.msg(player, "<gray>Night vision <red>disabled<gray>.");
        } else {
            // infinite duration, level 1, not ambient, no particles, show icon
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0, false, false, true));
            this.plugin.msg(player, "<gray>Night vision <green>enabled<gray>.");
        }
    }
}