package org.notionsmp.sylvUtilities.escalator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.notionsmp.sylvUtilities.SylvUtilities;

public final class EscalatorManager {
    private final SylvUtilities plugin;
    private final EscalatorStorage storage;
    private final Map<String, Escalator> escalators = new HashMap<>();
    private final Map<UUID, EscalatorPlayer> active = new HashMap<>();
    private BukkitTask task;
    private final Team noNameTagTeam;

    public EscalatorManager(final SylvUtilities plugin) {
        this.plugin = plugin;
        this.storage = new EscalatorStorage(plugin);
        this.noNameTagTeam = this.setupNoNameTagTeam();
    }

    public void load() {
        this.escalators.clear();
        for (final Escalator escalator : this.storage.load()) {
            this.escalators.put(escalator.id().toLowerCase(), escalator);
        }
    }

    public void start() {
        if (this.task != null) {
            return;
        }
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        for (final UUID uuid : this.active.keySet().toArray(UUID[]::new)) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                this.stopPlayer(player);
            }
        }
        this.active.clear();
    }

    public void save() {
        this.storage.save(this.escalators.values());
    }

    public Collection<Escalator> all() {
        return Collections.unmodifiableCollection(this.escalators.values());
    }

    public Escalator get(final String id) {
        if (id == null) {
            return null;
        }
        return this.escalators.get(id.toLowerCase());
    }

    public boolean create(final String id, final Location top, final Location bottom, final EscalatorDirection direction, final double speed) {
        if (id == null || id.isBlank() || top == null || bottom == null || top.getWorld() == null || bottom.getWorld() == null) {
            return false;
        }
        if (!top.getWorld().getUID().equals(bottom.getWorld().getUID())) {
            return false;
        }
        final String key = id.toLowerCase();
        if (this.escalators.containsKey(key)) {
            return false;
        }
        if (direction == null || speed < 0.0) {
            return false;
        }
        this.escalators.put(key, new Escalator(id, top, bottom, direction, speed));
        this.save();
        return true;
    }

    public boolean delete(final String id) {
        final Escalator removed = this.escalators.remove(id == null ? null : id.toLowerCase());
        if (removed == null) {
            return false;
        }
        this.save();
        return true;
    }

    public boolean editTop(final String id, final Location top) {
        final Escalator escalator = this.get(id);
        if (escalator == null || top == null || top.getWorld() == null) {
            return false;
        }
        if (!escalator.world().getUID().equals(top.getWorld().getUID())) {
            return false;
        }
        escalator.setTop(top);
        this.save();
        return true;
    }

    public boolean editBottom(final String id, final Location bottom) {
        final Escalator escalator = this.get(id);
        if (escalator == null || bottom == null || bottom.getWorld() == null) {
            return false;
        }
        if (!escalator.world().getUID().equals(bottom.getWorld().getUID())) {
            return false;
        }
        escalator.setBottom(bottom);
        this.save();
        return true;
    }

    public boolean editSpeed(final String id, final double speed) {
        final Escalator escalator = this.get(id);
        if (escalator == null || speed < 0.0) {
            return false;
        }
        escalator.setSpeedBlocksPerSecond(speed);
        this.save();
        return true;
    }

    public boolean editDirection(final String id, final EscalatorDirection direction) {
        final Escalator escalator = this.get(id);
        if (escalator == null || direction == null) {
            return false;
        }
        escalator.setDirection(direction);
        this.save();
        return true;
    }

    public boolean isOnEscalator(final Player player) {
        return player != null && this.active.containsKey(player.getUniqueId());
    }

    public EscalatorPlayer session(final UUID playerId) {
        return this.active.get(playerId);
    }

    public void tryStart(final Player player, final Location to) {
        if (player == null || to == null) {
            return;
        }
        if (this.isOnEscalator(player)) {
            return;
        }
        if (player.isInsideVehicle()) {
            return;
        }
        for (final Escalator escalator : this.escalators.values()) {
            if (escalator.isInStartArea(to)) {
                this.startPlayer(escalator, player);
                return;
            }
        }
    }

    public void stopPlayer(final Player player) {
        if (player == null) {
            return;
        }
        final EscalatorPlayer session = this.active.remove(player.getUniqueId());
        if (session != null) {
            session.forceStop(player);
        }
    }

    private void startPlayer(final Escalator escalator, final Player player) {
        final Location start = escalator.start();
        start.setYaw(player.getLocation().getYaw());
        start.setPitch(0.0f);

        final var mannequin = EscalatorPlayer.spawnMannequin(player, start);
        this.applyNoNameTag(mannequin);
        final var vehicle = EscalatorPlayer.spawnVehicle(start);
        final EscalatorPlayer session = new EscalatorPlayer(escalator, player, mannequin, vehicle);
        session.preparePlayer(player);
        this.active.put(player.getUniqueId(), session);
    }

    private Team setupNoNameTagTeam() {
        if (Bukkit.getScoreboardManager() == null) {
            return null;
        }
        final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam("noNameTag");
        if (team == null) {
            team = scoreboard.registerNewTeam("noNameTag");
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        return team;
    }

    private void applyNoNameTag(final Entity entity) {
        if (this.noNameTagTeam == null || entity == null) {
            return;
        }
        if (!this.noNameTagTeam.hasEntity(entity)) {
            this.noNameTagTeam.addEntity(entity);
        }
    }

    private void tick() {
        for (final UUID uuid : this.active.keySet().toArray(UUID[]::new)) {
            final EscalatorPlayer session = this.active.get(uuid);
            if (session == null) {
                continue;
            }
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                this.active.remove(uuid);
                continue;
            }
            final boolean done = session.tick(player);
            if (done) {
                this.active.remove(uuid);
            }
        }
    }
}
