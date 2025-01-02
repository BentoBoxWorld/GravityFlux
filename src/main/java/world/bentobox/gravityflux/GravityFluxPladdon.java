package world.bentobox.gravityflux;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

public class GravityFluxPladdon extends Pladdon {

    private GravityFlux addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new GravityFlux();
        }
        return addon;
    }

}
