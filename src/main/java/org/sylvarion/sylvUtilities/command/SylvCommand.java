package org.sylvarion.sylvUtilities.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.List;
import org.sylvarion.sylvUtilities.SylvUtilities;

public final class SylvCommand {
    private final SylvUtilities plugin;
    private final List<SylvSubcommand> subcommands = new ArrayList<>();

    public SylvCommand(final SylvUtilities plugin) {
        this.plugin = plugin;
    }

    public SylvCommand register(final SylvSubcommand subcommand) {
        this.subcommands.add(subcommand);
        return this;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        final var root = Commands.literal("sylv");
        root.then(Commands.literal("reload")
                .requires(source -> source.getSender().hasPermission("sylvutilities.reload"))
                .executes(context -> {
                    this.plugin.msg(context.getSource().getSender(), this.plugin.reloadModules());
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                }));
        for (final SylvSubcommand subcommand : this.subcommands) {
            root.then(subcommand.build(this.plugin));
        }
        return root.build();
    }
}

