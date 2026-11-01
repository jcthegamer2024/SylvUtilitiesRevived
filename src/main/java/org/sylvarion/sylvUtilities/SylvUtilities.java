package org.sylvarion.sylvUtilities;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.sylvarion.sylvUtilities.command.SylvCommand;
import org.sylvarion.sylvUtilities.command.subcommand.EscalatorSubcommand;
import org.sylvarion.sylvUtilities.escalator.EscalatorListener;
import org.sylvarion.sylvUtilities.escalator.EscalatorManager;
import org.sylvarion.sylvUtilities.nightvision.NightVisionModule;

public final class SylvUtilities extends JavaPlugin {

    private EscalatorManager escalators;
    private final String prefix = "<gradient:#254091:#24AAE1:#939599:#ECA609>sʏʟᴠᴀʀɪᴏɴ<dark_gray> » <reset>";

    @Override
    public void onEnable() {
        this.escalators = new EscalatorManager(this);
        this.escalators.load();
        this.escalators.start();
        this.getServer().getPluginManager().registerEvents(new EscalatorListener(this.escalators), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            final var root = new SylvCommand(this)
                    .register(new EscalatorSubcommand(this.escalators));
            commands.register(root.build(), "Sylvarion Utilities", List.of());
        });

        new NightVisionModule(this).enable();

        this.msg(Bukkit.getConsoleSender(), "Sucessfuly started Sylvarion Utilities v" + this.getPluginMeta().getVersion());
    }

    @Override
    public void onDisable() {
        if (this.escalators != null) {
            this.escalators.stop();
            this.escalators.save();
        }
    }

    public void msg(final CommandSender sender, final String message) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(this.prefix + message));
    }

    public EscalatorManager escalators() {
        return this.escalators;
    }

    public String prefix() {
        return this.prefix;
    }
}