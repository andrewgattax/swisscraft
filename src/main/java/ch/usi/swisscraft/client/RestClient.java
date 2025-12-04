package ch.usi.swisscraft.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class RestClient {
    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        int xmin = 2600000;
        int ymin = 1200000;
        int increment = 16;
        int xmax = xmin + increment - 1;

        String geomJson = String.format(
            "{\"type\":\"LineString\",\"coordinates\":[[%d,%d],[%d,%d]]}",
            xmin, ymin, xmax, ymin
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

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(System.out::println)
            .join();
    }
}