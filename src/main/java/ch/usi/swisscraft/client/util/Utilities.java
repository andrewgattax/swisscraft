package ch.usi.swisscraft.client.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    //TODO sta cosa è una porcata, legge il json raw ed estrae i dati tra "COMB": e la virgola successiva,
    // cambiare con libreria
    public static List<Integer> getCOMBHeightsFromJson(String json) {
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


    public static List<Integer> sendLineRequest(int xmin, int xmax, int y, int increment) {
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
            .timeout(Duration.ofSeconds(20))
            .GET()
            .build();

        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("HTTP Error " + response.statusCode() + " for URL: " + url + ".");
            }
            List<Integer> result = getCOMBHeightsFromJson(response.body());
            result.forEach(System.out::println);  //TODO remove
            return getCOMBHeightsFromJson(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failure for line y=" + y, e);
        }
    }
}
