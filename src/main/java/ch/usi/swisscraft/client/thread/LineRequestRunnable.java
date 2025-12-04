package ch.usi.swisscraft.client.thread;

import java.util.List;

import static ch.usi.swisscraft.client.util.Utilities.sendLineRequest;

public class LineRequestRunnable implements Runnable {
    private final int xmin;
    private final int xmax;
    private final int y;
    private final int increment;
    private List<Integer> result;

    public LineRequestRunnable(int xmin, int xmax, int y, int increment) {
        this.xmin = xmin;
        this.xmax = xmax;
        this.y = y;
        this.increment = increment;
    }

    @Override
    public void run() {
        // Esegue la richiesta e salva il risultato
        this.result = sendLineRequest(xmin, xmax, y, increment);
    }

    public List<Integer> getResult() {
        return result;
    }
}