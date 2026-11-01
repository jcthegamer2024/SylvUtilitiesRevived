package org.sylvarion.sylvUtilities.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.sylvarion.sylvUtilities.SylvUtilities;

public interface SylvSubcommand {
    String name();

    LiteralArgumentBuilder<CommandSourceStack> build(SylvUtilities plugin);
}

