package org.sylvarion.sylvUtilities.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sylvarion.sylvUtilities.SylvUtilities;
import org.sylvarion.sylvUtilities.command.SylvSubcommand;
import org.sylvarion.sylvUtilities.escalator.Escalator;
import org.sylvarion.sylvUtilities.escalator.EscalatorDirection;
import org.sylvarion.sylvUtilities.escalator.EscalatorManager;

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
            .then(this.list(plugin))
            .then(this.show(plugin))
            .then(this.tp(plugin))
            .then(this.teleport(plugin))
            .then(this.info(plugin));
    }

    private LiteralArgumentBuilder<CommandSourceStack> create(final SylvUtilities plugin) {
        return Commands.literal("create")
            .then(Commands.argument("id", StringArgumentType.word())
                .then(Commands.argument("topX", DoubleArgumentType.doubleArg()).suggests(this.suggestX())
                    .then(Commands.argument("topY", DoubleArgumentType.doubleArg()).suggests(this.suggestY())
                        .then(Commands.argument("topZ", DoubleArgumentType.doubleArg()).suggests(this.suggestZ())
                            .then(Commands.argument("bottomX", DoubleArgumentType.doubleArg()).suggests(this.suggestX())
                                .then(Commands.argument("bottomY", DoubleArgumentType.doubleArg()).suggests(this.suggestY())
                                    .then(Commands.argument("bottomZ", DoubleArgumentType.doubleArg()).suggests(this.suggestZ())
                                        .then(Commands.argument("direction", StringArgumentType.word()).suggests(this.suggestDirection())
                                            .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.0))
                                                .executes(ctx -> this.createExec(plugin, ctx, null))
                                                .then(Commands.argument("sound", StringArgumentType.word())
                                                    .executes(ctx -> this.createExec(plugin, ctx, StringArgumentType.getString(ctx, "sound")))))))))))));
    }

    private int createExec(final SylvUtilities plugin, final CommandContext<CommandSourceStack> ctx, final String soundKey) {
        final String id = StringArgumentType.getString(ctx, "id");
        final var world = ctx.getSource().getLocation().getWorld();
        if (world == null) {
            plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Invalid world."));
            return 0;
        }
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

        if (direction == null) {
            plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Invalid direction.") + " <gray>Use <aqua>up</aqua> or <aqua>down</aqua>.</gray>");
            return 0;
        }
        if (!this.escalators.create(id, top, bottom, direction, speed, soundKey)) {
            plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Could not create escalator.") + " <dark_gray>(</dark_gray><gray>ID exists or worlds mismatch</gray><dark_gray>)</dark_gray>");
            return 0;
        }
        plugin.msg(this.feedbackSender(ctx.getSource()), "<green>Created escalator</green> " + this.mmId(id) + "<green>.</green> "
            + this.mmButtonRun("info", "/sylv escalator info " + id, "<#7CFFCB>[Info]</#7CFFCB>")
            + " " + this.mmButtonRun("tp", "/sylv escalator tp " + id, "<#66D9FF>[Teleport]</#66D9FF>"));
        return Command.SINGLE_SUCCESS;
    }

    private LiteralArgumentBuilder<CommandSourceStack> remove(final SylvUtilities plugin) {
        return Commands.literal("remove").then(this.deleteExec(plugin));
    }

    private LiteralArgumentBuilder<CommandSourceStack> delete(final SylvUtilities plugin) {
        return Commands.literal("delete").then(this.deleteExec(plugin));
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> deleteExec(final SylvUtilities plugin) {
        return Commands.argument("id", StringArgumentType.word()).suggests(this.suggestIdExisting())
            .executes(ctx -> {
                final String id = StringArgumentType.getString(ctx, "id");
                final boolean deleted = this.escalators.delete(id);
                if (!deleted) {
                    plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Escalator not found.") + " <gray>ID:</gray> " + this.mmId(id));
                    return 0;
                }
                plugin.msg(this.feedbackSender(ctx.getSource()), "<#FF6B6B>Deleted escalator</#FF6B6B> " + this.mmId(id) + "<#FF6B6B>.</#FF6B6B>");
                return Command.SINGLE_SUCCESS;
            });
    }

    private LiteralArgumentBuilder<CommandSourceStack> edit(final SylvUtilities plugin) {
        return Commands.literal("edit")
            .then(Commands.argument("id", StringArgumentType.word()).suggests(this.suggestIdExisting())
                .then(this.editUpLoc(plugin))
                .then(this.editBottomLoc(plugin))
                .then(this.editSpeed(plugin))
                .then(this.editDirection(plugin))
                .then(this.editSound(plugin)));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editUpLoc(final SylvUtilities plugin) {
        return Commands.literal("upLoc")
            .then(Commands.argument("x", DoubleArgumentType.doubleArg()).suggests(this.suggestX())
                .then(Commands.argument("y", DoubleArgumentType.doubleArg()).suggests(this.suggestY())
                    .then(Commands.argument("z", DoubleArgumentType.doubleArg()).suggests(this.suggestZ())
                        .executes(ctx -> {
                            final String id = StringArgumentType.getString(ctx, "id");
                            final var world = ctx.getSource().getLocation().getWorld();
                            if (world == null) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Invalid world."));
                                return 0;
                            }
                            final Location loc = new Location(world,
                                DoubleArgumentType.getDouble(ctx, "x"),
                                DoubleArgumentType.getDouble(ctx, "y"),
                                DoubleArgumentType.getDouble(ctx, "z"));
                            if (!this.escalators.editTop(id, loc)) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Could not edit escalator.") + " <gray>ID:</gray> " + this.mmId(id));
                                return 0;
                            }
                            plugin.msg(this.feedbackSender(ctx.getSource()), "<#7CFFCB>Updated</#7CFFCB> " + this.mmId(id) + " <gray>upLoc</gray> <dark_gray>→</dark_gray> " + this.mmLocShort(loc));
                            return Command.SINGLE_SUCCESS;
                        }))));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editBottomLoc(final SylvUtilities plugin) {
        return Commands.literal("bottomLoc")
            .then(Commands.argument("x", DoubleArgumentType.doubleArg()).suggests(this.suggestX())
                .then(Commands.argument("y", DoubleArgumentType.doubleArg()).suggests(this.suggestY())
                    .then(Commands.argument("z", DoubleArgumentType.doubleArg()).suggests(this.suggestZ())
                        .executes(ctx -> {
                            final String id = StringArgumentType.getString(ctx, "id");
                            final var world = ctx.getSource().getLocation().getWorld();
                            if (world == null) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Invalid world."));
                                return 0;
                            }
                            final Location loc = new Location(world,
                                DoubleArgumentType.getDouble(ctx, "x"),
                                DoubleArgumentType.getDouble(ctx, "y"),
                                DoubleArgumentType.getDouble(ctx, "z"));
                            if (!this.escalators.editBottom(id, loc)) {
                                plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Could not edit escalator.") + " <gray>ID:</gray> " + this.mmId(id));
                                return 0;
                            }
                            plugin.msg(this.feedbackSender(ctx.getSource()), "<#7CFFCB>Updated</#7CFFCB> " + this.mmId(id) + " <gray>bottomLoc</gray> <dark_gray>→</dark_gray> " + this.mmLocShort(loc));
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
                        plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Could not edit escalator.") + " <gray>ID:</gray> " + this.mmId(id));
                        return 0;
                    }
                    plugin.msg(this.feedbackSender(ctx.getSource()), "<#7CFFCB>Updated</#7CFFCB> " + this.mmId(id) + " <gray>speed</gray> <dark_gray>→</dark_gray> <white>" + this.formatNumber(speed) + "</white><gray> b/s</gray>");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editDirection(final SylvUtilities plugin) {
        return Commands.literal("direction")
            .then(Commands.argument("value", StringArgumentType.word()).suggests(this.suggestDirection())
                .executes(ctx -> {
                    final String id = StringArgumentType.getString(ctx, "id");
                    final EscalatorDirection direction = EscalatorDirection.parse(StringArgumentType.getString(ctx, "value"));
                    if (direction == null || !this.escalators.editDirection(id, direction)) {
                        plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Could not edit escalator.") + " <gray>ID:</gray> " + this.mmId(id));
                        return 0;
                    }
                    plugin.msg(this.feedbackSender(ctx.getSource()), "<#7CFFCB>Updated</#7CFFCB> " + this.mmId(id) + " <gray>direction</gray> <dark_gray>→</dark_gray> <white>" + this.formatDirection(direction) + "</white>");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> editSound(final SylvUtilities plugin) {
        return Commands.literal("sound")
            .then(Commands.argument("value", StringArgumentType.word())
                .executes(ctx -> {
                    final String id = StringArgumentType.getString(ctx, "id");
                    final String key = StringArgumentType.getString(ctx, "value");
                    if (!this.escalators.editSound(id, key)) {
                        plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Could not edit escalator.") + " <gray>ID:</gray> " + this.mmId(id));
                        return 0;
                    }
                    plugin.msg(this.feedbackSender(ctx.getSource()), "<#7CFFCB>Updated</#7CFFCB> " + this.mmId(id) + " <gray>sound</gray> <dark_gray>→</dark_gray> " + this.mmSound(key));
                    return Command.SINGLE_SUCCESS;
                }))
            .then(Commands.literal("none")
                .executes(ctx -> {
                    final String id = StringArgumentType.getString(ctx, "id");
                    if (!this.escalators.editSound(id, null)) {
                        plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Could not edit escalator.") + " <gray>ID:</gray> " + this.mmId(id));
                        return 0;
                    }
                    plugin.msg(this.feedbackSender(ctx.getSource()), "<#7CFFCB>Updated</#7CFFCB> " + this.mmId(id) + " <gray>sound</gray> <dark_gray>→</dark_gray> <dark_gray>none</dark_gray>");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> show(final SylvUtilities plugin) {
        return Commands.literal("show")
            .then(Commands.argument("id", StringArgumentType.word()).suggests(this.suggestIdExisting())
                .executes(ctx -> {
                    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                        plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Only players can do that."));
                        return 0;
                    }
                    final String id = StringArgumentType.getString(ctx, "id");
                    if (this.escalators.get(id) == null) {
                        plugin.msg(player, this.mmError("Escalator not found.") + " <gray>ID:</gray> " + this.mmId(id));
                        return 0;
                    }
                    this.escalators.toggleShow(player, id);
                    plugin.msg(player, "<#66D9FF>Toggled show</#66D9FF> " + this.mmId(id) + "<#66D9FF>.</#66D9FF>");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> list(final SylvUtilities plugin) {
        return Commands.literal("list")
            .executes(ctx -> {
                final CommandSender sender = this.feedbackSender(ctx.getSource());
                final Collection<Escalator> all = this.escalators.all();
                if (all.isEmpty()) {
                    plugin.msg(sender, "<yellow>No escalators found.</yellow> <dark_gray>(</dark_gray><gray>create one with</gray> <white>/sylv escalator create</white><dark_gray>)</dark_gray>");
                    return Command.SINGLE_SUCCESS;
                }
                plugin.msg(sender, "<gold><bold>Escalators</bold></gold> <dark_gray>(</dark_gray><gray>" + all.size() + "</gray><dark_gray>)</dark_gray>");
                all.stream()
                    .sorted(Comparator.comparing(Escalator::id))
                    .forEach(escalator -> plugin.msg(sender, this.mmListEntry(escalator)));
                return Command.SINGLE_SUCCESS;
            });
    }

    private LiteralArgumentBuilder<CommandSourceStack> tp(final SylvUtilities plugin) {
        return Commands.literal("tp").then(this.tpExec(plugin));
    }

    private LiteralArgumentBuilder<CommandSourceStack> teleport(final SylvUtilities plugin) {
        return Commands.literal("teleport").then(this.tpExec(plugin));
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> tpExec(final SylvUtilities plugin) {
        return Commands.argument("id", StringArgumentType.word()).suggests(this.suggestIdExisting())
            .executes(ctx -> {
                if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                    plugin.msg(this.feedbackSender(ctx.getSource()), this.mmError("Only players can do that."));
                    return 0;
                }
                final String id = StringArgumentType.getString(ctx, "id");
                final Escalator escalator = this.escalators.get(id);
                if (escalator == null) {
                    plugin.msg(player, this.mmError("Escalator not found.") + " <gray>ID:</gray> " + this.mmId(id));
                    return 0;
                }
                final Location target = this.centerIfBlockAligned(escalator.top());
                if (target.getWorld() == null) {
                    plugin.msg(player, this.mmError("Invalid world."));
                    return 0;
                }
                target.setYaw(player.getLocation().getYaw());
                target.setPitch(player.getLocation().getPitch());
                player.teleportAsync(target);
                plugin.msg(player, "<#66D9FF>Teleported to</#66D9FF> " + this.mmId(escalator.id()) + "<#66D9FF>.</#66D9FF> <dark_gray>(</dark_gray><gray>top</gray><dark_gray>)</dark_gray>");
                return Command.SINGLE_SUCCESS;
            });
    }

    private LiteralArgumentBuilder<CommandSourceStack> info(final SylvUtilities plugin) {
        return Commands.literal("info")
            .then(Commands.argument("id", StringArgumentType.word()).suggests(this.suggestIdExisting())
                .executes(ctx -> {
                    final CommandSender sender = this.feedbackSender(ctx.getSource());
                    final String id = StringArgumentType.getString(ctx, "id");
                    final Escalator escalator = this.escalators.get(id);
                    if (escalator == null) {
                        plugin.msg(sender, this.mmError("Escalator not found.") + " <gray>ID:</gray> " + this.mmId(id));
                        return 0;
                    }

                    final Location top = escalator.top();
                    final Location bottom = escalator.bottom();
                    final World world = top.getWorld();
                    final String worldName = world == null ? "unknown" : world.getName();
                    final String commandId = escalator.id();

                    plugin.msg(sender, "<dark_gray>━━━━━━━━━</dark_gray> <gradient:#24AAE1:#ECA609><bold>Escalator Info</bold></gradient> <dark_gray>━━━━━━━━━</dark_gray>");
                    plugin.msg(sender, "<gray>ID:</gray> " + this.mmIdWithTp(commandId, escalator) + " <dark_gray>|</dark_gray> " + this.mmButtonRun("tp", "/sylv escalator tp " + commandId, "<#66D9FF>[Teleport]</#66D9FF>") + " "
                        + this.mmButtonRun("show", "/sylv escalator show " + commandId, "<#66D9FF>[Show]</#66D9FF>") + " "
                        + this.mmButtonSuggest("delete", "/sylv escalator delete " + commandId, "<#FF6B6B><bold>[Delete]</bold></#FF6B6B>"));
                    plugin.msg(sender, "<gray>World:</gray> <white>" + worldName + "</white>");
                    plugin.msg(sender, "<gray>Direction:</gray> <white>" + this.formatDirection(escalator.direction()) + "</white> " + this.mmButtonSuggest("edit", "/sylv escalator edit " + commandId + " direction " + escalator.direction().name().toLowerCase(Locale.ROOT), "<#FFD166>[Edit]</#FFD166>"));
                    plugin.msg(sender, "<gray>Speed:</gray> <white>" + this.formatNumber(escalator.speedBlocksPerSecond()) + "</white><gray> b/s</gray> " + this.mmButtonSuggest("edit", "/sylv escalator edit " + commandId + " speed " + this.formatNumber(escalator.speedBlocksPerSecond()), "<#FFD166>[Edit]</#FFD166>"));
                    plugin.msg(sender, "<gray>Sound:</gray> " + this.mmSound(escalator.soundKey()) + " " + this.mmSoundEditButtons(commandId, escalator.soundKey()));
                    plugin.msg(sender, "<gray>UpLoc:</gray> " + this.mmLocShort(top) + " " + this.mmButtonSuggest("edit", "/sylv escalator edit " + commandId + " upLoc " + this.coordForCommand(top.getX()) + " " + this.coordForCommand(top.getY()) + " " + this.coordForCommand(top.getZ()), "<#FFD166>[Edit]</#FFD166>"));
                    plugin.msg(sender, "<gray>BottomLoc:</gray> " + this.mmLocShort(bottom) + " " + this.mmButtonSuggest("edit", "/sylv escalator edit " + commandId + " bottomLoc " + this.coordForCommand(bottom.getX()) + " " + this.coordForCommand(bottom.getY()) + " " + this.coordForCommand(bottom.getZ()), "<#FFD166>[Edit]</#FFD166>"));
                    plugin.msg(sender, "<dark_gray>Tip:</dark_gray> <gray>Hover the</gray> " + this.mmId(commandId) + " <gray>and click to teleport.</gray>");
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private SuggestionProvider<CommandSourceStack> suggestIdExisting() {
        return (ctx, builder) -> {
            for (final String id : this.escalators.ids()) {
                builder.suggest(id);
            }
            return builder.buildFuture();
        };
    }

    private SuggestionProvider<CommandSourceStack> suggestDirection() {
        return (ctx, builder) -> {
            builder.suggest("up");
            builder.suggest("down");
            return builder.buildFuture();
        };
    }

    private SuggestionProvider<CommandSourceStack> suggestX() {
        return this.suggestCoord(Axis.X);
    }

    private SuggestionProvider<CommandSourceStack> suggestY() {
        return this.suggestCoord(Axis.Y);
    }

    private SuggestionProvider<CommandSourceStack> suggestZ() {
        return this.suggestCoord(Axis.Z);
    }

    private SuggestionProvider<CommandSourceStack> suggestCoord(final Axis axis) {
        return (ctx, builder) -> {
            final Location loc = ctx.getSource().getLocation();
            builder.suggest(String.valueOf(axis.value(loc)));
            return builder.buildFuture();
        };
    }

    private CommandSender feedbackSender(final CommandSourceStack source) {
        if (source.getExecutor() instanceof CommandSender sender) {
            return sender;
        }
        return source.getSender();
    }

    private String mmListEntry(final Escalator escalator) {
        final String id = escalator.id();
        final String tooltip = this.escapeQuoted(this.mmEscalatorHover(escalator));
        final String idPart = "<hover:show_text:'" + tooltip + "'><click:run_command:'" + this.escapeQuoted("/sylv escalator tp " + id) + "'>" + this.mmId(id) + "</click></hover>";
        final String infoButton = this.mmButtonRun("info", "/sylv escalator info " + id, "<#7CFFCB>[Info]</#7CFFCB>");
        final String showButton = this.mmButtonRun("show", "/sylv escalator show " + id, "<#66D9FF>[Show]</#66D9FF>");
        final String deleteButton = this.mmButtonSuggest("delete", "/sylv escalator delete " + id, "<#FF6B6B>[Del]</#FF6B6B>");
        return "<dark_gray>•</dark_gray> " + idPart
            + " <dark_gray>-</dark_gray> <gray>" + this.formatDirection(escalator.direction()) + "</gray>"
            + " <dark_gray>•</dark_gray> <white>" + this.formatNumber(escalator.speedBlocksPerSecond()) + "</white><gray>b/s</gray>"
            + " <dark_gray>|</dark_gray> " + infoButton + " " + showButton + " " + deleteButton;
    }

    private String mmEscalatorHover(final Escalator escalator) {
        final Location top = escalator.top();
        final World world = top.getWorld();
        final String worldName = world == null ? "unknown" : world.getName();
        return "<gradient:#24AAE1:#ECA609><bold>Escalator</bold></gradient><newline>"
            + "<gray>ID:</gray> " + this.mmId(escalator.id()) + "<newline>"
            + "<gray>World:</gray> <white>" + worldName + "</white><newline>"
            + "<gray>Top:</gray> " + this.mmLocShort(top) + "<newline>"
            + "<gray>Speed:</gray> <white>" + this.formatNumber(escalator.speedBlocksPerSecond()) + "</white><gray> b/s</gray><newline>"
            + "<dark_gray>Click to teleport</dark_gray>";
    }

    private String mmIdWithTp(final String id, final Escalator escalator) {
        final String tooltip = this.escapeQuoted(this.mmEscalatorHover(escalator));
        return "<hover:show_text:'" + tooltip + "'><click:run_command:'" + this.escapeQuoted("/sylv escalator tp " + id) + "'>" + this.mmId(id) + "</click></hover>";
    }

    private String mmId(final String id) {
        return "<#66D9FF><bold>" + id + "</bold></#66D9FF>";
    }

    private String mmError(final String text) {
        return "<#FF6B6B><bold>Error:</bold></#FF6B6B> <red>" + text + "</red>";
    }

    private String mmSound(final String soundKey) {
        if (soundKey == null) {
            return "<dark_gray>none</dark_gray>";
        }
        return "<#FFD166>" + soundKey + "</#FFD166>";
    }

    private String mmSoundEditButtons(final String id, final String soundKey) {
        final String setButton;
        if (soundKey == null) {
            setButton = this.mmButtonSuggest("set", "/sylv escalator edit " + id + " sound ", "<#FFD166>[Set]</#FFD166>");
        } else {
            setButton = this.mmButtonSuggest("set", "/sylv escalator edit " + id + " sound " + soundKey, "<#FFD166>[Edit]</#FFD166>");
        }
        final String noneButton = this.mmButtonSuggest("none", "/sylv escalator edit " + id + " sound none", "<dark_gray>[None]</dark_gray>");
        return setButton + " " + noneButton;
    }

    private String mmButtonRun(final String hoverLabel, final String command, final String text) {
        final String hover = this.escapeQuoted("<gray>Click:</gray> <white>" + hoverLabel + "</white>");
        return "<hover:show_text:'" + hover + "'><click:run_command:'" + this.escapeQuoted(command) + "'>" + text + "</click></hover>";
    }

    private String mmButtonSuggest(final String hoverLabel, final String command, final String text) {
        final String hover = this.escapeQuoted("<gray>Suggest:</gray> <white>" + hoverLabel + "</white>");
        return "<hover:show_text:'" + hover + "'><click:suggest_command:'" + this.escapeQuoted(command) + "'>" + text + "</click></hover>";
    }

    private String mmLocShort(final Location loc) {
        if (loc == null) {
            return "<dark_gray>unknown</dark_gray>";
        }
        final World world = loc.getWorld();
        final String worldName = world == null ? "unknown" : world.getName();
        return "<white>" + worldName + "</white><dark_gray>@</dark_gray>"
            + "<gray>(</gray><white>" + this.formatNumber(loc.getX()) + "</white><dark_gray>,</dark_gray> <white>" + this.formatNumber(loc.getY()) + "</white><dark_gray>,</dark_gray> <white>" + this.formatNumber(loc.getZ()) + "</white><gray>)</gray>";
    }

    private String formatDirection(final EscalatorDirection direction) {
        if (direction == null) {
            return "unknown";
        }
        return direction.name().toLowerCase(Locale.ROOT);
    }

    private String formatNumber(final double value) {
        if (Math.abs(value - Math.rint(value)) < 1.0E-6) {
            return String.valueOf((int) Math.rint(value));
        }
        final String formatted = String.format(Locale.ROOT, "%.3f", value);
        int end = formatted.length();
        while (end > 0 && formatted.charAt(end - 1) == '0') {
            end--;
        }
        if (end > 0 && formatted.charAt(end - 1) == '.') {
            end--;
        }
        return formatted.substring(0, end);
    }

    private String coordForCommand(final double value) {
        return this.formatNumber(value);
    }

    private String escapeQuoted(final String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("'", "\\'");
    }

    private Location centerIfBlockAligned(final Location input) {
        final Location loc = input.clone();
        if (this.isAligned(loc.getX())) {
            loc.setX(loc.getX() + 0.5);
        }
        if (this.isAligned(loc.getZ())) {
            loc.setZ(loc.getZ() + 0.5);
        }
        return loc;
    }

    private boolean isAligned(final double value) {
        return Math.abs(value - Math.rint(value)) < 1.0E-6;
    }

    private enum Axis {
        X {
            @Override
            int value(final Location loc) {
                return loc.getBlockX();
            }
        },
        Y {
            @Override
            int value(final Location loc) {
                return loc.getBlockY();
            }
        },
        Z {
            @Override
            int value(final Location loc) {
                return loc.getBlockZ();
            }
        };

        abstract int value(Location loc);
    }
}
