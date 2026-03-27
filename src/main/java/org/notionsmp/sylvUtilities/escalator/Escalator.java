package org.notionsmp.sylvUtilities.escalator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class Escalator {
    private final String id;
    private Location top;
    private Location bottom;
    private EscalatorDirection direction;
    private double speedBlocksPerSecond;

    public Escalator(
        final String id,
        final Location top,
        final Location bottom,
        final EscalatorDirection direction,
        final double speedBlocksPerSecond
    ) {
        this.id = id;
        this.top = top.clone();
        this.bottom = bottom.clone();
        this.direction = direction;
        this.speedBlocksPerSecond = speedBlocksPerSecond;
    }

    public String id() {
        return this.id;
    }

    public World world() {
        return this.top.getWorld();
    }

    public Location top() {
        return this.top.clone();
    }

    public Location bottom() {
        return this.bottom.clone();
    }

    public EscalatorDirection direction() {
        return this.direction;
    }

    public double speedBlocksPerSecond() {
        return this.speedBlocksPerSecond;
    }

    public void setTop(final Location top) {
        this.top = top.clone();
    }

    public void setBottom(final Location bottom) {
        this.bottom = bottom.clone();
    }

    public void setDirection(final EscalatorDirection direction) {
        this.direction = direction;
    }

    public void setSpeedBlocksPerSecond(final double speedBlocksPerSecond) {
        this.speedBlocksPerSecond = speedBlocksPerSecond;
    }

    public Location start() {
        return centerIfBlockAligned(this.direction == EscalatorDirection.UP ? this.bottom : this.top);
    }

    public Location end() {
        return centerIfBlockAligned(this.direction == EscalatorDirection.UP ? this.top : this.bottom);
    }

    public Vector directionVector() {
        final Location start = this.start();
        final Location end = this.end();
        return end.toVector().subtract(start.toVector());
    }

    public double length() {
        return this.directionVector().length();
    }

    public boolean isInStartArea(final Location location) {
        if (location == null) {
            return false;
        }
        final Location start = this.start();
        if (start.getWorld() == null || location.getWorld() == null) {
            return false;
        }
        if (!start.getWorld().getUID().equals(location.getWorld().getUID())) {
            return false;
        }
        final double dx = location.getX() - start.getX();
        final double dz = location.getZ() - start.getZ();
        if ((dx * dx + dz * dz) > 0.75 * 0.75) {
            return false;
        }
        return Math.abs(location.getY() - start.getY()) <= 1.6;
    }

    private static Location centerIfBlockAligned(final Location input) {
        final Location loc = input.clone();
        if (isAligned(loc.getX())) {
            loc.setX(loc.getX() + 0.5);
        }
        if (isAligned(loc.getZ())) {
            loc.setZ(loc.getZ() + 0.5);
        }
        return loc;
    }

    private static boolean isAligned(final double value) {
        return Math.abs(value - Math.rint(value)) < 1.0E-6;
    }
}
