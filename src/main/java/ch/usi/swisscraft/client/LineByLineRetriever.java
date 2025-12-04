package ch.usi.swisscraft.client;

import ch.usi.swisscraft.client.thread.ConcurrentLineByLineRetriever;
import ch.usi.swisscraft.client.util.Utilities;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ch.usi.swisscraft.client.util.Utilities.sendLineRequest;

public final class LineByLineRetriever implements ChunkDataRetriever {

    private static volatile LineByLineRetriever instance = null;
    public static LineByLineRetriever instance() {
        if (instance == null) {
            synchronized(LineByLineRetriever.class) { // acquire global lock
                if (instance == null) // lazy creation
                    instance = new LineByLineRetriever();
            } // release global lock
        }
        return instance;
    }
    private LineByLineRetriever() {}

    public List<Integer> retrieveHeightMap(int xTopLeft, int yTopLeft, int increment) {
        int xmin = xTopLeft;
        int ymax = yTopLeft;
        int ymin = ymax - increment + 1;
        int xmax = xmin + increment - 1;

        List<Integer> resultList = new ArrayList<>();

        for(int y = ymin; y <= ymax; y += 1) {
            List<Integer> line = sendLineRequest(xmin, xmax, y, increment);
            resultList.addAll(line);
        }
        if(resultList.size() != increment * increment) {
            throw new IllegalStateException("Not all values were retrieved.");
        }
        return resultList;
    }
}
