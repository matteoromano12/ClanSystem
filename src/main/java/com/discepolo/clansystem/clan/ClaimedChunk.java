package com.discepolo.clansystem.clan;

import org.bukkit.Chunk;
import java.util.Objects;

public class ClaimedChunk {

    private final String world;
    private final int x;
    private final int z;

    public ClaimedChunk(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public static ClaimedChunk fromChunk(Chunk chunk) {
        return new ClaimedChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClaimedChunk other = (ClaimedChunk) o;
        return x == other.x && z == other.z && world.equals(other.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }
}