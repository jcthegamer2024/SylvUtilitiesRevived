package org.notionsmp.sylvUtilities.escalator;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    public void onQuit(final PlayerQuitEvent event) {
        this.manager.stopShow(event.getPlayer());
        this.manager.stopPlayer(event.getPlayer());
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        this.manager.stopShow(event.getEntity());
        this.manager.stopPlayer(event.getEntity());
    }

    @EventHandler
    public void onStopSpectating(final PlayerStopSpectatingEntityEvent event) {
        final Player player = event.getPlayer();
        final EscalatorPlayer session = this.manager.session(player.getUniqueId());
        if (session == null) {
            return;
        }
        if (session.isFinishing()) {
            return;
        }
        if (event.getSpectatorTarget() != null && event.getSpectatorTarget().getUniqueId().equals(session.mannequinId())) {
            event.setCancelled(true);
        }
    }
}
