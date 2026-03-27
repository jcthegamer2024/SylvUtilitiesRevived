package org.notionsmp.sylvUtilities.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.List;
import org.notionsmp.sylvUtilities.SylvUtilities;

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
        for (final SylvSubcommand subcommand : this.subcommands) {
            root.then(subcommand.build(this.plugin));
        }
        return root.build();
    }
}

