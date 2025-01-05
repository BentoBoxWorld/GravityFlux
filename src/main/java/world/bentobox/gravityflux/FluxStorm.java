package world.bentobox.gravityflux;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FluxStorm {

    private static final List<EntityType> IMMUNE_ENTITIES = List.of(EntityType.BLAZE, EntityType.ENDER_DRAGON,
            EntityType.GHAST, EntityType.MAGMA_CUBE, EntityType.PHANTOM, EntityType.VEX, EntityType.WITHER,
            EntityType.SHULKER, EntityType.BAT, EntityType.BEE, EntityType.PARROT, EntityType.SNOW_GOLEM);
    private GravityFlux addon;
    private Map<World, Long> index = new HashMap<>();
    private Map<World, Change> stormPhase = new HashMap<>();
    private Set<UUID> affected = new HashSet<>();
    private Set<UUID> leaping = new HashSet<>();
    private Random rand = new Random();
    private Set<Block> flowing = new HashSet<>();
    private boolean forced;

    record Change(
            /**
             * Upwards velocity to apply
             */
            double upVel,
            /**
             * Number of times the velocity should be applied
             * The time will depend on the ticks between applications
             */
            long count,
            /**
             * The number of ticks between velocity applications
             */
            long length, /**
                                       * The phase this applies to
                                       */
            StormPhase phase) {
    }

    public enum StormPhase {
        NONE, WARN, SURGE, PEAK, END
    }

    public FluxStorm(GravityFlux addon) {
        super();
        this.addon = addon;
    }

    void runTimedStorm(World world) {
        long duration = addon.getSettings().getMinTime() + rand.nextLong(addon.getSettings().getMaxTime());
        addon.log("Next flux store in " + world.getName() + " will be in " + duration + " minutes.");
        Bukkit.getScheduler().runTaskLater(addon.getPlugin(), () -> fluxStorm(world, false), duration * 60L * 20L);
    }

    /**
     * Start a flux storm in world
     * @param world world
     */
    public void fluxStorm(World world, boolean forced) {
        this.forced = forced;
        // Define storm phases
        Change edge = new Change(0.15, 10 + rand.nextInt(3) - rand.nextInt(3), 20L, StormPhase.WARN);
        Change mid = new Change(0.20, 10 + rand.nextInt(3) - rand.nextInt(3), 10L, StormPhase.SURGE);
        Change full = new Change(0.50, (addon.getSettings().getDuration() * 60 * 20), 1L, StormPhase.PEAK);

        startStormSequence(world, edge, mid, full, addon.getPlugin());
    }

    private void startStormSequence(World world, Change edge, Change mid, Change full, Plugin plugin) {
        executePhase(world, edge, plugin, () -> executePhase(world, mid, plugin, () -> executePhase(world, full, plugin,
                () -> executePhase(world, mid, plugin, () -> executePhase(world, edge, plugin, null)))));
    }

    /**
     * End the storm
     * @param w world
     */
    public void endStorm(World w) {
        Change change = stormPhase.get(w);
        index.put(w, change.count());
    }

    private void executePhase(World world, Change change, Plugin plugin, Runnable nextPhase) {
        index.put(world, 0L);
        stormPhase.put(world, change);
        addon.log("Flux storm in " + world.getName() + " (" + change.phase() + ")");
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                varyGravity(world, change);
                if (index.compute(world, (k, v) -> (v == null ? 1 : v + 1)) >= change.count()) {
                    index.put(world, 0L);
                    this.cancel();
                    if (nextPhase != null) {
                        nextPhase.run();
                    } else {
                        // End
                        Change end = new Change(0, 0, 0, StormPhase.END);
                        stormPhase.put(world, end);
                        varyGravity(world, end);
                        // Reset flows
                        Map<Location, BlockData> mapBlocksToLocationData = flowing.stream()
                                .collect(Collectors.toMap(Block::getLocation, // Key: Block's Location
                                        block -> block.getBlockData().clone() // Value: Clone of Block's BlockData
                        ));
                        flowing.forEach(b -> b.setType(Material.AIR)); // Clear block
                        // Set it a tick later
                        Bukkit.getScheduler().runTask(plugin,
                                () -> mapBlocksToLocationData.forEach((l, bd) -> l.getBlock().setBlockData(bd)));
                        // Set up the next storm
                        if (!forced) {
                            runTimedStorm(world);
                        }
                    }
                }
            }

        };
        task.runTaskTimer(plugin, change.length(), change.length());
    }

    private void varyGravity(World world, Change change) {
        world.getEntities().stream().filter(en -> en.isVisibleByDefault()).filter(en -> !en.isInsideVehicle())
                .filter(en -> !IMMUNE_ENTITIES.contains(en.getType())).filter(en -> en.isInWorld())
                .filter(en -> !leaping.contains(en.getUniqueId()))
                .filter(en -> en.getLocation().getBlock().getRelative(BlockFace.UP).isPassable()).forEach(en -> {
                    // Pre-full phase
                    if (change.phase() != StormPhase.PEAK) {
                        if (affected.remove(en.getUniqueId())) {
                            ((Player) en).setFlying(false);
                            ((Player) en).setAllowFlight(false);
                            return;
                        }
                    }
                    // Ignore entities without gravity
                    if ((en.hasGravity() || affected.contains(en.getUniqueId()))) {
                        // Boost entity
                        double speed = change.upVel();
                        // Slow falling slows the speed of lift
                        if (en instanceof Player pl && pl.getPotionEffect(PotionEffectType.SLOW_FALLING) != null) {
                            // Slow falling potion effect
                            speed = speed / 2D;
                        }
                        // Set the speed up
                        en.setVelocity(new Vector(0D, change.upVel(), 0D));
                        // If you fall up too much, you die
                        if (en.getLocation().getBlockY() >= world.getMaxHeight() - 2) {
                            if (en instanceof LivingEntity le) {
                                le.damage(2, DamageSource.builder(DamageType.OUT_OF_WORLD).build());
                            } else {
                                // Not living - destroy it
                                en.remove();
                            }
                        }
                        // Put player into flying mode and make a noise
                        if (change.phase() == StormPhase.PEAK && en instanceof Player pl && !pl.isFlying()) {
                            affected.add(en.getUniqueId());
                            pl.setAllowFlight(true);
                            pl.setFlying(true);
                            world.playSound(en, Sound.EVENT_MOB_EFFECT_BAD_OMEN, 1F, 1F);
                        }
                        // Make a noise
                        if (en.getType() == EntityType.PLAYER && change.phase() != StormPhase.PEAK) {
                            world.playSound(en, Sound.ENTITY_CREAKING_DEATH, 1F, 1F);
                        }
            }
        });
    }

    public void shutdown() {
        // Fix gravity for everything before shutting down
        affected.stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                .filter(en -> affected.contains(en.getUniqueId())).forEach(en -> {
            en.getPlayer().setFlying(false);
            en.getPlayer().setAllowFlight(false);
        });
    }

    /**
     * Get the storm phase for world
     * @param world world
     * @return storm phase
     */
    public StormPhase getStormPhase(World world) {
        Change change = stormPhase.get(world);
        if (change == null) {
            return StormPhase.NONE;
        }
        return change.phase();
    }

    /**
     * @return the affected
     */
    public Set<UUID> getAffected() {
        return affected;
    }

    /**
     * @param affected the affected to set
     */
    public void setAffected(Set<UUID> affected) {
        this.affected = affected;
    }

    /**
     * @return the flowing
     */
    public Set<Block> getFlowing() {
        return flowing;
    }

    /**
     * @param flowing the flowing to set
     */
    public void setFlowing(Set<Block> flowing) {
        this.flowing = flowing;
    }

}
