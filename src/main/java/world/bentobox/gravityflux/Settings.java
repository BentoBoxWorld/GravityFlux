package world.bentobox.gravityflux;

import java.util.HashSet;
import java.util.Set;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;

/**
 * All the plugin settings are here
 * @author Tastybento
 */
@StoreAt(filename = "config.yml", path = "addons/GravityFlux") // Explicitly call out what name this should have.
@ConfigComment("GravityFlux Configuration [version]")
public class Settings implements ConfigObject {

    @ConfigComment("")
    @ConfigComment("This list stores GameModes in which addon should not work.")
    @ConfigComment("To disable addon it is necessary to write its name in new line that starts with -. Example:")
    @ConfigComment("disabled-gamemodes:")
    @ConfigComment(" - BSkyBlock")
    @ConfigEntry(path = "disabled-gamemodes")
    private Set<String> disabledGameModes = new HashSet<>();

    @ConfigComment("Flux Storms occur randomly between these times")
    @ConfigComment("Minimum time between flux storms in minutes")
    @ConfigEntry(path = "flux-storm.min-time")
    private int minTime = 10;

    @ConfigComment("maximum time between flux storms in minutes")
    @ConfigEntry(path = "flux-storm.max-time")
    private int maxTime = 30;

    @ConfigComment("Duration of storms in minutes")
    @ConfigEntry(path = "flux-storm.duration")
    private int duration = 3;

    /**
     * @return the disabledGameModes
     */
    public Set<String> getDisabledGameModes() {
        return disabledGameModes;
    }

    /**
     * @param disabledGameModes the disabledGameModes to set
     */
    public void setDisabledGameModes(Set<String> disabledGameModes) {
        this.disabledGameModes = disabledGameModes;
    }

    /**
     * @return the minTime
     */
    public int getMinTime() {
        if (minTime < 0) {
            minTime = 0;
        }
        return minTime;
    }

    /**
     * @param minTime the minTime to set
     */
    public void setMinTime(int minTime) {
        this.minTime = minTime;
    }

    /**
     * @return the maxTime
     */
    public int getMaxTime() {
        if (maxTime < 0) {
            maxTime = 0;
        }
        return maxTime;
    }

    /**
     * @param maxTime the maxTime to set
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

}
