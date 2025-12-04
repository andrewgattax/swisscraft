package ch.usi.swisscraft.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineByLineRetriever implements ChunkDataRetriever {

    public static void main(String[] args) {
        LineByLineRetriever retriever = new LineByLineRetriever();
        List<Integer> heightmap = retriever.retrieveHeightMap(2718332, 1096050, 16);
        System.out.println(heightmap);
    }

    public List<Integer> retrieveHeightMap(int xTopLeft, int yTopLeft, int increment) {
        int xmin = xTopLeft;
        int ymax = yTopLeft;
        int ymin = ymax - increment + 1;
        int xmax = xmin + increment - 1;

        List<Integer> resultList = new ArrayList<>();

        for(int y = ymax; y >= ymin; y -= 1) {
            List<Integer> line = sendLineRequest(xmin, xmax, y, increment);
            resultList.addAll(line);
        }

        if(resultList.size() != increment * increment) {
            throw new IllegalStateException("Not all values were retrieved.");
        }

        return resultList;
    }

    private static List<Integer> sendLineRequest(int xmin, int xmax ,int y, int increment) {
        HttpClient client = HttpClient.newHttpClient();

        String geomJson = String.format(
            "{\"type\":\"LineString\",\"coordinates\":[[%d,%d],[%d,%d]]}",
            xmin, y, xmax, y
        );

        String url = String.format(
            "https://api3.geo.admin.ch/rest/services/profile.json?geom=%s&sr=%d&nb_points=%d",
            URLEncoder.encode(geomJson, StandardCharsets.UTF_8),
            2056,
            increment
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenApply(LineByLineRetriever::getCOMBHeightsFromJson)
            .join();
    }

    //TODO sta cosa è una porcata, legge il json raw ed estrae i dati tra "COMB": e la virgola successiva,
    // cambiare con libreria
    private static List<Integer> getCOMBHeightsFromJson(String json) {
        List<Double> values = new ArrayList<>();

        // Pattern to match "COMB": followed by a number, stopping at comma
        Pattern pattern = Pattern.compile("\"COMB\":([-+]?\\d*\\.?\\d+)(?=,)");
        Matcher matcher = pattern.matcher(json);

        while (matcher.find()) {
            String valueStr = matcher.group(1);
            values.add(Double.parseDouble(valueStr));
        }

        return values.stream().map(Double::intValue).toList();
    }
}