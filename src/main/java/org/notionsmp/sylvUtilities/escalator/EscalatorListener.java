package org.notionsmp.sylvUtilities.escalator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class EscalatorListener implements Listener {
    private final EscalatorManager manager;

    public EscalatorListener(final EscalatorManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        if (event.getFrom().getX() == event.getTo().getX()
            && event.getFrom().getY() == event.getTo().getY()
            && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }
        this.manager.tryStart(event.getPlayer(), event.getTo());
    }

    @EventHandler
    public void onDismount(final EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        final EscalatorPlayer session = this.manager.session(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (session.isFinishing()) {
            return;
        }
        if (event.getDismounted() != null && event.getDismounted().getUniqueId().equals(session.vehicleId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.manager.stopPlayer(event.getPlayer());
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        this.manager.stopPlayer(event.getEntity());
    }
}
