package ch.usi.swisscraft.client;

import java.util.List;

public interface ChunkDataRetriever {
    //Returns a list of height values: left to right, top to bottom
    public List<Integer> retrieveHeightMap(int xTopLeft, int yTopLeft, int increment);
}
