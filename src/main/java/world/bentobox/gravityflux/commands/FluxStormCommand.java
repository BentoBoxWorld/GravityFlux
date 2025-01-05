package world.bentobox.gravityflux.commands;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.gravityflux.FluxStorm.StormPhase;
import world.bentobox.gravityflux.GravityFlux;

/**
 * Starts a flux storm
 */
public class FluxStormCommand extends CompositeCommand {

    private final GravityFlux addon;

    public FluxStormCommand(CompositeCommand parent, GravityFlux addon) {
        super(parent, "fluxstorm", "flux");
        this.addon = addon;
    }

    @Override
    public void setup() {
        this.setPermission("gravityflux.admin.flux");
        this.setDescription("gravityflux.admin.flux.description");

    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (!args.isEmpty()) {
            this.showHelp(this, user);
            return false;
        }
        StormPhase phase = addon.getFluxer().getStormPhase(getWorld());
        if (phase == StormPhase.PEAK || phase == StormPhase.SURGE || phase == StormPhase.WARN) {
            addon.getFluxer().endStorm(getWorld());
            user.sendMessage("gravityflux.admin.flux.ending");
            return true;
        }
        addon.getFluxer().fluxStorm(getWorld(), true);
        user.sendMessage("general.success");
        return true;
    }
}
