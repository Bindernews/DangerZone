package com.vortexel.dangerzone.common.difficulty;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.lwjgl.util.Rectangle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public class BiomeBoundBuilder {

    private static final int BLOCKS_INCLUDED_THRESHOLD = 16 * 4; // out of 16 * 16

    private World world;
    private Biome biome;
    private BlockPos originPos;
    private ChunkPos originChunk;

    public BiomeBoundBuilder(World world, BlockPos pos) {
        this.world = world;
        this.biome = world.getBiome(pos);
        this.originPos = pos;
        this.originChunk = new ChunkPos(pos);
    }

    public ArrayList<Rectangle> getBoundingBoxes() {
        Rectangle allBounds = new Rectangle();
        HashSet<ChunkPos> chunks = floodFillChunks(allBounds);
        return null;
    }

    private ArrayList<Rectangle> boundBoxes(HashSet<ChunkPos> chunks, Rectangle area) {

        return null;
    }

    private int countIn(HashSet<ChunkPos> chunks, Rectangle area) {
        int maxX = area.getX() + area.getWidth();
        int maxZ = area.getY() + area.getHeight();
        int count = 0;
        for (int x = area.getX(); x <= maxX; x++) {
            for (int z = area.getY(); z <= maxZ; z++) {
                ChunkPos c = new ChunkPos(x, z);
                if (chunks.contains(c)) {
                    count += 1;
                }
            }
        }
        return count;
    }

    private HashSet<ChunkPos> floodFillChunks(Rectangle out_bounds) {
        // Included chunks
        HashSet<ChunkPos> inChunks = new HashSet<>();
        // Chunks we've already processed
        HashSet<ChunkPos> seenChunks = new HashSet<>();
        seenChunks.add(originChunk);
        // List of chunks to process
        Stack<ChunkPos> todo = new Stack<>();
        todo.push(originChunk);
        // Maximum chunk bounds
        int minX = originChunk.x;
        int maxX = originChunk.x;
        int minZ = originChunk.z;
        int maxZ = originChunk.z;

        ChunkPos[] positions = new ChunkPos[4];
        while (!todo.empty()) {
            ChunkPos c = todo.pop();
            positions[0] = new ChunkPos(c.x - 1, c.z);
            positions[1] = new ChunkPos(c.x + 1, c.z);
            positions[2] = new ChunkPos(c.x, c.z - 1);
            positions[3] = new ChunkPos(c.x, c.z + 1);
            // Go through each adjacent chunk and add it to the list if it's "in"
            for (ChunkPos cOff : positions) {
                if (!seenChunks.contains(cOff)) {
                    seenChunks.add(cOff);
                    if (isChunkIn(cOff)) {
                        inChunks.add(cOff);
                        todo.push(cOff);
                        // Update our bounding box
                        minX = Math.min(cOff.x, minX);
                        maxX = Math.max(cOff.x, maxX);
                        minZ = Math.min(cOff.z, minZ);
                        maxZ = Math.max(cOff.z, maxZ);
                    }
                }
            }
        }
        // Return
        out_bounds.setBounds(minX, minZ, maxX - minX, maxZ - minZ);
        return inChunks;
    }

    protected boolean isChunkIn(ChunkPos pos) {
        Biome[] biomes = new Biome[16 * 16];
        biomes = world.getBiomeProvider().getBiomes(biomes, pos.getXStart(), pos.getZStart(), 16, 16);
        int numberIn = 0;
        for (int i = 0; i < biomes.length; i++) {
            if (biomes[i].equals(biome)) {
                numberIn++;
            }
        }
        return numberIn >= BLOCKS_INCLUDED_THRESHOLD;
    }

    /*
    let AllChunks = Find contiguous chunks which have the same biome (use paint-fill algorithm where if at least 50%
      of the blocks in a chunk are of the selected biome, then it counts).
    def boundBoxes(Chunks):
      let B = A bounding box which contains all the chunks
      if area(B) * 0.5 > area(Chunks):
        out = []
        out += boundBoxes(Chunks[North-East-quadrant])
        out += boundBoxes(Chunks[North-West-quadrant])
        out += boundBoxes(Chunks[South-East-quadrant])
        out += boundBoxes(Chunks[South-West-quadrant])
        return out
      else:
        return [B]

    Alternate:
    Divide chunks into "chunk groups" either by number of chunks (e.g. 8x8 chunks = group) or by size (e.g. always
    divide into 16 chunk groups in a 4x4 grid, regardless of how many chunks there are in each group).
    If a chunk group is over 80% "in" then it's an acceptable bounding box, otherwise a tighter bound must be imposed
    by calling the function recursively with the inner-chunk group.

    When querying if a chunk is in or out, store the "in" chunks in a hash set where the hash code is (x + (y * 46103))
    this gives us a maximum "grid size" of approximately 46000 chunks horizontally and vertically, but we don't have
    to actually make a huge grid.
     */
}
