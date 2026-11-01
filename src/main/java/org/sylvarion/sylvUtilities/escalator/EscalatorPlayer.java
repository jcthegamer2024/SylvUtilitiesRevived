package org.sylvarion.sylvUtilities.escalator;

import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class EscalatorPlayer {
    /**
     * Vertical offset of the invisible vehicle. If riders look sunk into the floor,
     * increase this (e.g. 0.3). If they float too high, decrease it (e.g. -0.3).
     */
    private static final double RIDE_Y_OFFSET = 0.0;

    private final Escalator escalator;
    private final UUID playerId;
    private final ArmorStand vehicle;
    private final boolean previousInvulnerable;
    private double travelled;
    private final double length;
    private final Vector unit;
    private final float yaw;
    private boolean finishing;
    private int soundCooldownTicks;

    public EscalatorPlayer(final Escalator escalator, final Player player, final ArmorStand vehicle) {
        this.escalator = escalator;
        this.playerId = player.getUniqueId();
        this.vehicle = vehicle;
        this.previousInvulnerable = player.isInvulnerable();
        this.travelled = 0.0;
        this.length = Math.max(0.0, escalator.length());
        final Vector dir = escalator.directionVector();
        this.unit = dir.lengthSquared() == 0.0 ? new Vector(0, 0, 0) : dir.clone().normalize();
        this.yaw = computeYaw(this.unit);
        this.finishing = false;
        this.soundCooldownTicks = 0;
    }

    public UUID playerId() {
        return this.playerId;
    }

    public UUID mannequinId() {
        return this.vehicle.getUniqueId();
    }

    public boolean isFinishing() {
        return this.finishing;
    }

    public void preparePlayer(final Player player) {
        player.setInvulnerable(true);
        if (!this.vehicle.addPassenger(player)) {
            this.finishing = true;
            player.setInvulnerable(this.previousInvulnerable);
            this.vehicle.remove();
        }
    }

    public boolean tick(final Player player) {
        if (this.finishing) {
            return true;
        }
        if (!player.isOnline()) {
            this.finishing = true;
            this.vehicle.remove();
            return true;
        }
        if (player.isDead() || !this.vehicle.getPassengers().contains(player)) {
            this.forceStop(player);
            return true;
        }
        if (this.length == 0.0) {
            this.finish(player);
            return true;
        }

        final double step = Math.max(0.0, this.escalator.speedBlocksPerSecond()) / 20.0;
        this.travelled = Math.min(this.length, this.travelled + step);

        final Location base = this.escalator.start();
        final Vector offset = this.unit.clone().multiply(this.travelled);
        base.add(offset);
        base.add(0.0, RIDE_Y_OFFSET, 0.0);
        base.setYaw(this.yaw);
        base.setPitch(0.0f);

        this.vehicle.teleport(base);
        this.tryPlaySound(player, base);

        if (this.travelled >= this.length - 1.0E-6) {
            this.finish(player);
            return true;
        }
        return false;
    }

    private void tryPlaySound(final Player player, final Location location) {
        final String soundKey = this.escalator.soundKey();
        if (soundKey == null || soundKey.isBlank()) {
            return;
        }
        if (this.soundCooldownTicks > 0) {
            this.soundCooldownTicks--;
            return;
        }
        this.soundCooldownTicks = 10;
        try {
            final Sound sound = Sound.sound(Key.key(soundKey), Sound.Source.AMBIENT, 0.6f, 1.0f);
            player.playSound(sound, location.getX(), location.getY(), location.getZ());
        } catch (final Exception ignored) {
        }
    }

    /** Normal end of the ride: put the player at the end point, keeping their look direction. */
    public void finish(final Player player) {
        if (this.finishing) {
            return;
        }
        this.finishing = true;
        final Location exit = this.vehicle.getLocation().clone().add(0.0, -RIDE_Y_OFFSET, 0.0);
        exit.setYaw(player.getLocation().getYaw());
        exit.setPitch(player.getLocation().getPitch());

        this.vehicle.removePassenger(player);
        player.setInvulnerable(this.previousInvulnerable);
        player.teleport(exit);
        this.vehicle.remove();
    }

    /** Abrupt end (quit, death, teleport, plugin shutdown): no repositioning. */
    public void forceStop(final Player player) {
        this.finishing = true;
        player.setInvulnerable(this.previousInvulnerable);
        this.vehicle.remove();
    }

    public static ArmorStand spawnVehicle(final Location location) {
        final Location spawn = location.clone().add(0.0, RIDE_Y_OFFSET, 0.0);
        return spawn.getWorld().spawn(spawn, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setSilent(true);
            stand.setPersistent(false);
            stand.setBasePlate(false);
            stand.setCustomNameVisible(false);
        });
    }

    private static float computeYaw(final Vector direction) {
        if (direction.lengthSquared() == 0.0) {
            return 0.0f;
        }
        return (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
    }
}