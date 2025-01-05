package world.bentobox.gravityflux;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.gravityflux.commands.FluxStormCommand;
import world.bentobox.gravityflux.listeners.FluxStormListener;

/**
 * Main class - provides a survival game with flipping gravity
 * @author tastybento
 */
public class GravityFlux extends Addon {

    // Settings
    private Settings settings;
    private final Config<Settings> config = new Config<>(this, Settings.class);
    private final List<GameModeAddon> registeredGameModes = new ArrayList<>();
    private FluxStorm fluxer;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = config.loadConfigObject();
        if (settings == null) {
            // Settings did not load correctly. Disable.
            logError("Settings did not load correctly - disabling GravityFlux - please check config.yml");
            this.setState(State.DISABLED);
            return;
        }
        config.saveConfigObject(settings);

        // Register commands for GameModes
        registeredGameModes.clear();
        getPlugin().getAddonsManager().getGameModeAddons().stream()
                .filter(gm -> !settings.getDisabledGameModes().contains(gm.getDescription().getName())).forEach(gm -> {
                    log("GravityFlux hooking into " + gm.getDescription().getName());
                    gm.getAdminCommand().ifPresent(adminCommand -> new FluxStormCommand(adminCommand, this));
                    registeredGameModes.add(gm);
                });
        // Flux!
        fluxer = new FluxStorm(this);
        // Kick off for all registered worlds
        getRegisteredGameModes().forEach(gm -> fluxer.runTimedStorm(gm.getOverWorld()));
        // Register listeners
        this.registerListener(new FluxStormListener(fluxer));

    }

    @Override
    public void onDisable() {
        fluxer.shutdown();
    }

    /**
     * @return the settings
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * @return the registeredGameModes
     */
    public List<GameModeAddon> getRegisteredGameModes() {
        return registeredGameModes;
    }

    /**
     * @return the fluxer
     */
    public FluxStorm getFluxer() {
        return fluxer;
    }

}
