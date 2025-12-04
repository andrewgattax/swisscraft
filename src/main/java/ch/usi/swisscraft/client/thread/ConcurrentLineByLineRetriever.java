package ch.usi.swisscraft.client.thread;

import ch.usi.swisscraft.client.ChunkDataRetriever;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ch.usi.swisscraft.client.util.Utilities.getCOMBHeightsFromJson;

public final class ConcurrentLineByLineRetriever implements ChunkDataRetriever {

    private static volatile ConcurrentLineByLineRetriever instance = null;
    public static ConcurrentLineByLineRetriever instance() {
        if (instance == null) {
            synchronized(ConcurrentLineByLineRetriever.class) { // acquire global lock
                if (instance == null) // lazy creation
                    instance = new ConcurrentLineByLineRetriever();
            } // release global lock
        }
        return instance;
    }
    private ConcurrentLineByLineRetriever() {}

    @Override
    public List<Integer> retrieveHeightMap(int xTopLeft, int yTopLeft, int increment) {
        int xmin = xTopLeft;
        int ymax = yTopLeft;
        int ymin = ymax - increment + 1;
        int xmax = xmin + increment - 1;

        ExecutorService executor = Executors.newFixedThreadPool(16);
        List<LineRequestRunnable> tasks = new ArrayList<>();
        for(int y = ymin; y <= ymax; y += 1) {
        //for(int y = ymax; y >= ymin; y -= 1) {
            LineRequestRunnable task = new LineRequestRunnable(xmin, xmax, y, increment);
            tasks.add(task);
            executor.submit(task);

            try {
                Thread.sleep(75);
            } catch(InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }

        executor.shutdown();

        try {
            boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);
            
            if (!finished) {
                throw new RuntimeException("Timeout while waiting for threads to finish.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted during thread await.", e);
        }

        List<Integer> resultList = new ArrayList<>();
        for (LineRequestRunnable task : tasks) {
            if (task.getResult() != null) {
                resultList.addAll(task.getResult());
            } else {
                throw new RuntimeException("One of the threads failed. (retrieving error).");
            }
        }
        if(resultList.size() != increment * increment) {
            throw new IllegalStateException("Not all values were retrieved. Expected: "
                + (increment * increment) + ", Recieved: " + resultList.size() + ".");
        }
        return resultList;
    }

}