package org.notionsmp.sylvUtilities.escalator;

import java.util.UUID;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.Mannequin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class EscalatorPlayer {
    private final Escalator escalator;
    private final UUID playerId;
    private final Mannequin mannequin;
    private final Interaction vehicle;
    private final boolean previousInvisible;
    private final PotionEffect previousInvisibility;
    private double travelled;
    private final double length;
    private final Vector unit;
    private final float yaw;
    private boolean finishing;

    public EscalatorPlayer(final Escalator escalator, final Player player, final Mannequin mannequin, final Interaction vehicle) {
        this.escalator = escalator;
        this.playerId = player.getUniqueId();
        this.mannequin = mannequin;
        this.vehicle = vehicle;
        this.previousInvisible = player.isInvisible();
        this.previousInvisibility = player.getPotionEffect(PotionEffectType.INVISIBILITY);
        this.travelled = 0.0;
        this.length = Math.max(0.0, escalator.length());
        final Vector dir = escalator.directionVector();
        this.unit = dir.lengthSquared() == 0.0 ? new Vector(0, 0, 0) : dir.clone().normalize();
        this.yaw = computeYaw(this.unit);
        this.finishing = false;
    }

    public UUID playerId() {
        return this.playerId;
    }

    public UUID vehicleId() {
        return this.vehicle.getUniqueId();
    }

    public boolean isFinishing() {
        return this.finishing;
    }

    public void preparePlayer(final Player player) {
        player.setInvisible(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true, false, false));
        this.vehicle.addPassenger(player);
    }

    public boolean tick(final Player player) {
        if (!player.isOnline()) {
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
        base.setYaw(this.yaw);
        base.setPitch(0.0f);

        this.mannequin.teleport(base);
        this.vehicle.teleport(base.clone().add(0.0, 0.4, 0.0));

        if (this.travelled >= this.length - 1.0E-6) {
            this.finish(player);
            return true;
        }
        return false;
    }

    public void finish(final Player player) {
        this.finishing = true;
        final Location location = this.mannequin.getLocation().clone();
        player.leaveVehicle();
        player.teleport(location);

        this.mannequin.remove();
        this.vehicle.remove();

        if (this.previousInvisibility != null) {
            player.addPotionEffect(this.previousInvisibility);
        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        player.setInvisible(this.previousInvisible);
    }

    public void forceStop(final Player player) {
        this.finishing = true;
        if (!this.mannequin.isDead()) {
            this.mannequin.remove();
        }
        if (!this.vehicle.isDead()) {
            this.vehicle.remove();
        }
        player.leaveVehicle();
        if (this.previousInvisibility != null) {
            player.addPotionEffect(this.previousInvisibility);
        } else {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
        player.setInvisible(this.previousInvisible);
    }

    public static Mannequin spawnMannequin(final Player player, final Location location) {
        final ResolvableProfile profile = ResolvableProfile.resolvableProfile(player.getPlayerProfile());
        return location.getWorld().spawn(location, Mannequin.class, m -> {
            m.setInvulnerable(true);
            m.setCollidable(false);
            m.setSilent(true);
            m.setPersistent(false);
            m.setImmovable(true);
            m.setGravity(false);
            m.setCustomNameVisible(false);
            m.setDescription(null);
            m.setProfile(profile);
        });
    }

    public static Interaction spawnVehicle(final Location location) {
        return location.getWorld().spawn(location.clone().add(0.0, 0.2, 0.0), Interaction.class, i -> {
            i.setInteractionHeight(0.1f);
            i.setInteractionWidth(0.1f);
            i.setPersistent(false);
        });
    }

    private static float computeYaw(final Vector direction) {
        if (direction.lengthSquared() == 0.0) {
            return 0.0f;
        }
        return (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
    }
}
