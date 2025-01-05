package world.bentobox.gravityflux.listeners;

import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import world.bentobox.gravityflux.FluxStorm;
import world.bentobox.gravityflux.FluxStorm.StormPhase;

public class FluxStormListener implements Listener {

    private FluxStorm fluxStorm;

    public FluxStormListener(FluxStorm fluxStorm) {
        super();
        this.fluxStorm = fluxStorm;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFlow(BlockFromToEvent e) {
        World w = e.getBlock().getWorld();
        if (this.fluxStorm.getStormPhase(w) == StormPhase.PEAK && e.getBlock().isLiquid()) {
            if (e.getFace() == BlockFace.DOWN) {
                // Stop and store for later
                this.fluxStorm.getFlowing().add(e.getBlock());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTeleport(PlayerTeleportEvent e) {
        if (this.fluxStorm.getAffected().contains(e.getPlayer().getUniqueId())) {
            if (this.fluxStorm.getAffected().contains(e.getPlayer().getUniqueId())) {
                e.getPlayer().setFlying(false);
                e.getPlayer().setAllowFlight(false);
                this.fluxStorm.getAffected().remove(e.getPlayer().getUniqueId());
            }
            this.fluxStorm.getAffected().remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDie(EntityDeathEvent e) {
        if (this.fluxStorm.getAffected().contains(e.getEntity().getUniqueId())) {
            ((Player) e.getEntity()).setFlying(false);
            ((Player) e.getEntity()).setAllowFlight(false);
            this.fluxStorm.getAffected().remove(e.getEntity().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (this.fluxStorm.getAffected().contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().setFlying(false);
            e.getPlayer().setAllowFlight(false);
            this.fluxStorm.getAffected().remove(e.getPlayer().getUniqueId());
        }
    }

}
