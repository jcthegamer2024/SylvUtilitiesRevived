package org.notionsmp.sylvUtilities.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.notionsmp.sylvUtilities.SylvUtilities;

public interface SylvSubcommand {
    String name();

    LiteralArgumentBuilder<CommandSourceStack> build(SylvUtilities plugin);
}

