package org.notionsmp.sylvUtilities.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Comparator;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.notionsmp.sylvUtilities.SylvUtilities;
import org.notionsmp.sylvUtilities.command.SylvSubcommand;
import org.notionsmp.sylvUtilities.escalator.Escalator;
import org.notionsmp.sylvUtilities.escalator.EscalatorDirection;
import org.notionsmp.sylvUtilities.escalator.EscalatorManager;

public final class EscalatorSubcommand implements SylvSubcommand {
    private final EscalatorManager escalators;

    public EscalatorSubcommand(final EscalatorManager escalators) {
        this.escalators = escalators;
    }

    @Override
    public String name() {
        return "escalator";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build(final SylvUtilities plugin) {
        return Commands.literal(this.name())
            .then(this.create(plugin))
            .then(this.remove(plugin))
            .then(this.delete(plugin))
            .then(this.edit(plugin))
            .then(this.list(plugin));
    }

    private LiteralArgumentBuilder<CommandSourceStack> create(final SylvUtilities plugin) {
        return Commands.literal("create")
            .then(Commands.argument("id", StringArgumentType.word())
                .then(Commands.argument("topX", DoubleArgumentType.doubleArg())
                    .then(Commands.argument("topY", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("topZ", DoubleArgumentType.doubleArg())
                            .then(Commands.argument("bottomX", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("bottomY", DoubleArgumentType.doubleArg())
                                    .then(Commands.argument("bottomZ", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("direction", StringArgumentType.word())
                                            .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.0))
                                                .executes(ctx -> {
                                                    final String id = StringArgumentType.getString(ctx, "id");
                                                    final var world = ctx.getSource().getLocation().getWorld();
                                                    final Location top = new Location(
                                                        world,
                                                        DoubleArgumentType.getDouble(ctx, "topX"),
                                                        DoubleArgumentType.getDouble(ctx, "topY"),
                                                        DoubleArgumentType.getDouble(ctx, "topZ")
                                                    );
                                                    final Location bottom = new Location(
                                                        world,
                                                        DoubleArgumentType.getDouble(ctx, "bottomX"),
                                                        DoubleArgumentType.getDouble(ctx, "bottomY"),
                                                        DoubleArgumentType.getDouble(ctx, "bottomZ")
                                                    );
                                                    final EscalatorDirection direction = EscalatorDirection.parse(StringArgumentType.getString(ctx, "direction"));
                                                    final double speed = DoubleArgumentType.getDouble(ctx, "speed");

                                                    if (world == null || direction == null) {
                                                        plugin.msg(this.feedbackSender(ctx.getSource()), "Invalid direction/world");
                                                        return 0;
                                                    }
                                                    final boolean created = this.escalators.create(id, top, bottom, direction, speed);
                                                    if (!created) {
                                                        plugin.msg(this.feedbackSender(ctx.getSource()), "Could not create escalator");
                                                        return 0;
                                                    }
                                                    plugin.msg(this.feedbackSender(ctx.getSource()), "Created escalator " + id);
                                                    return Command.SINGLE_SUCCESS;
                                                }))))))))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> remove(final SylvUtilities plugin) {
        return Commands.literal("remove").then(this.deleteExec(plugin));
    }

    private LiteralArgumentBuilder<CommandSourceStack> delete(final SylvUtilities plugin) {
        return Commands.literal("delete").then(this.deleteExec(plugin));
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> deleteExec(final SylvUtilities plugin) {
        return Commands.argument("id", StringArgumentType.word())
            .executes(ctx -> {
                final String id = StringArgumentType.getString(ctx, "id");
                final boolean deleted = this.escalators.delete(id);
                if (!deleted) {
                    plugin.msg(this.feedbackSender(ctx.getSource()), "Escalator not found");
                    return 0;
                }
                plugin.msg(this.feedbackSender(ctx.getSource()), "Deleted escalator " + id);
                return Command.SINGLE_SUCCESS;
            });
    }

    private LiteralArgumentBuilder<CommandSourceStack> edit(final SylvUtilities plugin) {
        return Commands.literal("edit")
            .then(Commands.argument("id", StringArgumentType.word())
                .then(this.editUpLoc(plugin))
                .then(this.editBottomLoc(plugin))
                .then(this.editSpeed(plugin))
                .then(this.editDirection(plugin)));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editUpLoc(final SylvUtilities plugin) {
        return Commands.literal("upLoc")
            .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                    .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            final String id = StringArgumentType.getString(ctx, "id");
                            final var world = ctx.getSource().getLocation().getWorld();
                            if (world == null) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), "Invalid world");
                                return 0;
                            }
                            final Location loc = new Location(world,
                                DoubleArgumentType.getDouble(ctx, "x"),
                                DoubleArgumentType.getDouble(ctx, "y"),
                                DoubleArgumentType.getDouble(ctx, "z"));
                            if (!this.escalators.editTop(id, loc)) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), "Could not edit escalator");
                                return 0;
                            }
                            plugin.msg(this.feedbackSender(ctx.getSource()), "Updated " + id + " upLoc");
                            return Command.SINGLE_SUCCESS;
                        }))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editBottomLoc(final SylvUtilities plugin) {
        return Commands.literal("bottomLoc")
            .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                    .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                        .executes(ctx -> {
                            final String id = StringArgumentType.getString(ctx, "id");
                            final var world = ctx.getSource().getLocation().getWorld();
                            if (world == null) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), "Invalid world");
                                return 0;
                            }
                            final Location loc = new Location(world,
                                DoubleArgumentType.getDouble(ctx, "x"),
                                DoubleArgumentType.getDouble(ctx, "y"),
                                DoubleArgumentType.getDouble(ctx, "z"));
                            if (!this.escalators.editBottom(id, loc)) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), "Could not edit escalator");
                                return 0;
                            }
                            plugin.msg(this.feedbackSender(ctx.getSource()), "Updated " + id + " bottomLoc");
                            return Command.SINGLE_SUCCESS;
                        }))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editSpeed(final SylvUtilities plugin) {
        return Commands.literal("speed")
            .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                .executes(ctx -> {
                    final String id = StringArgumentType.getString(ctx, "id");
                    final double speed = DoubleArgumentType.getDouble(ctx, "value");
                    if (!this.escalators.editSpeed(id, speed)) {
                        plugin.msg(this.feedbackSender(ctx.getSource()), "Could not edit escalator");
                        return 0;
                    }
                    plugin.msg(this.feedbackSender(ctx.getSource()), "Updated " + id + " speed");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editDirection(final SylvUtilities plugin) {
        return Commands.literal("direction")
            .then(Commands.argument("value", StringArgumentType.word())
                .executes(ctx -> {
                    final String id = StringArgumentType.getString(ctx, "id");
                    final EscalatorDirection direction = EscalatorDirection.parse(StringArgumentType.getString(ctx, "value"));
                    if (direction == null || !this.escalators.editDirection(id, direction)) {
                        plugin.msg(this.feedbackSender(ctx.getSource()), "Could not edit escalator");
                        return 0;
                    }
                    plugin.msg(this.feedbackSender(ctx.getSource()), "Updated " + id + " direction");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> list(final SylvUtilities plugin) {
        return Commands.literal("list")
            .executes(ctx -> {
                if (this.escalators.all().isEmpty()) {
                    plugin.msg(this.feedbackSender(ctx.getSource()), "No escalators");
                    return Command.SINGLE_SUCCESS;
                }
                plugin.msg(this.feedbackSender(ctx.getSource()), "Escalators:");
                this.escalators.all().stream()
                    .sorted(Comparator.comparing(Escalator::id))
                    .forEach(escalator -> plugin.msg(this.feedbackSender(ctx.getSource()),
                        "- " + escalator.id() + " (" + escalator.direction().name().toLowerCase() + ", " + escalator.speedBlocksPerSecond() + ")"));
                return Command.SINGLE_SUCCESS;
            });
    }

    private CommandSender feedbackSender(final CommandSourceStack source) {
        if (source.getExecutor() instanceof CommandSender sender) {
            return sender;
        }
        return source.getSender();
    }
}
