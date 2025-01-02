package world.bentobox.gravityflux.generators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.util.Util;
import world.bentobox.gravityflux.GravityFlux;

public class GFGenerator extends ChunkGenerator implements Listener {

    protected final GravityFlux addon;
    private @Nullable World world;

    public GFGenerator(GravityFlux addon) {
        this.addon = addon;
    }

    public void generateNoise(WorldInfo wi, Random rand, int x, int z, ChunkData chunkData) {
        // Void
        chunkData.setRegion(0, wi.getMinHeight(), 0, 16, wi.getMaxHeight(), 16, Material.AIR);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (addon.getNetherWorld() != null && addon.getNetherWorld().equals(e.getWorld())
                && addon.getOverWorld().isChunkGenerated(e.getChunk().getX(), e.getChunk().getZ())) {
            // Copy chunk from overworld
            invertChunk(addon.getOverWorld().getChunkAt(e.getChunk().getX(), e.getChunk().getZ()).getChunkSnapshot(),
                    e.getWorld(), e.getChunk());
        }
    }

    private void invertChunk(ChunkSnapshot cs, World w, Chunk chunk) {
        int height = w.getMaxHeight();
        int minY = w.getMinHeight();
        for (int y = minY; y < height; y++) {
            // Start at the bottom
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // Calculate the source y based on the reversed order
                    //int sourceY = (height - 1) - (y - minY);
                    int sourceY = y;
                    BlockData bd = cs.getBlockData(x, sourceY, z);
                    if (bd.getMaterial().isAir()) {
                        continue;
                    }
                    //BentoBox.getInstance().logDebug(
                    //         (chunk.getX() << 4 + x) + " " + y + " " + (chunk.getZ() << 4 + z) + " " + bd.getMaterial());

                    //Util.getPasteHandler().setBlock(new Location(w, x, y, z), Material.COBWEB.createBlockData());
                    chunk.getBlock(x, y, z).setBlockData(bd);
                    //chunk.getBlock(x, y, z).setBlockData(Material.COBWEB.createBlockData());
                }
            }
        }
    }

    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

}
