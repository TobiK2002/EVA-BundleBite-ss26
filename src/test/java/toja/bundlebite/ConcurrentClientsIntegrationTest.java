package toja.bundlebite;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrationstest zur nichtfunktionalen Anforderung: mindestens 50
 * gleichzeitig aktive Clients muessen Aktionen am Server ausfuehren koennen.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrentClientsIntegrationTest {

    private static final int CLIENT_COUNT = 50;
    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    @LocalServerPort
    private int port;

    private final List<String> createdUsers = new ArrayList<>();
    private UUID restaurantId;
    private UUID groupOrderId;

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void serverSupportsFiftyConcurrentClientsPerformingActions() throws Exception {
        UUID dishId = createRestaurantAndDish();
        createUsers();
        groupOrderId = createGroupOrder();

        CountDownLatch clientsReady = new CountDownLatch(CLIENT_COUNT);
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(CLIENT_COUNT);

        try {
            List<Future<Integer>> responses = new ArrayList<>();

            for (String email : createdUsers) {
                responses.add(executor.submit(() -> {
                    // Jeder Auftrag verwendet einen eigenen HTTP-Client und damit einen eigenen Client-Kontext.
                    HttpClient client = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(10))
                            .build();

                    clientsReady.countDown();
                    assertTrue(startSignal.await(10, TimeUnit.SECONDS), "Startsignal wurde nicht empfangen");

                    String requestBody = "{\"userEmail\":\"" + email + "\",\"dishId\":\""
                            + dishId + "\",\"quantity\":1}";

                    return send(client, "POST", "/api/group-orders/" + groupOrderId + "/order-entries", requestBody)
                            .statusCode();
                }));
            }

            assertTrue(clientsReady.await(30, TimeUnit.SECONDS), "Nicht alle Clients waren bereit");
            startSignal.countDown();

            for (Future<Integer> response : responses) {
                assertEquals(200, response.get(20, TimeUnit.SECONDS), "Ein Client konnte keinen Bestelleintrag anlegen");
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        HttpResponse<String> entriesResponse = send(HttpClient.newHttpClient(), "GET",
                "/api/group-orders/" + groupOrderId + "/order-entries", null);
        assertEquals(200, entriesResponse.statusCode());
        assertEquals(CLIENT_COUNT, countOccurrences(entriesResponse.body(), "\"id\""),
                "Alle 50 Bestelleintraege muessen gespeichert worden sein");
    }

    @AfterEach
    void cleanUp() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        if (groupOrderId != null) {
            send(client, "DELETE", "/api/group-orders/" + groupOrderId, null);
        }
        for (String email : createdUsers) {
            send(client, "DELETE", "/api/users/" + email, null);
        }
        if (restaurantId != null) {
            send(client, "DELETE", "/api/restaurants/" + restaurantId, null);
        }
    }

    private UUID createRestaurantAndDish() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> restaurantResponse = send(client, "POST", "/api/restaurants",
                "{\"name\":\"Lasttest Restaurant\",\"address\":\"Teststraße 1 04109 Leipzig\",\"minOrderValue\":0}");
        assertEquals(200, restaurantResponse.statusCode());
        restaurantId = extractId(restaurantResponse.body());

        HttpResponse<String> dishResponse = send(client, "POST", "/api/restaurants/" + restaurantId + "/dishes",
                "{\"name\":\"Lasttest Gericht\",\"description\":\"Gericht fuer den Paralleltest\",\"price\":9.99,\"ingredients\":[\"Teig\",\"Tomaten\"]}");
        assertEquals(200, dishResponse.statusCode());
        return extractId(dishResponse.body());
    }

    private void createUsers() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        for (int index = 0; index < CLIENT_COUNT; index++) {
            String email = "lasttest-" + UUID.randomUUID() + "@example.test";
            HttpResponse<String> response = send(client, "POST", "/api/users",
                    "{\"name\":\"Test Client " + index + "\",\"email\":\"" + email
                            + "\",\"address\":\"Teststraße 1 04109 Leipzig\"}");
            assertEquals(200, response.statusCode());
            createdUsers.add(email);
        }
    }

    private UUID createGroupOrder() throws Exception {
        HttpResponse<String> response = send(HttpClient.newHttpClient(), "POST", "/api/group-orders",
                "{\"restaurantId\":\"" + restaurantId + "\",\"creatorUserEmail\":\""
                        + createdUsers.get(0) + "\",\"expiresAt\":5}");
        assertEquals(200, response.statusCode());
        return extractId(response.body());
    }

    private HttpResponse<String> send(HttpClient client, String method, String path, String body)
            throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20));
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private UUID extractId(String responseBody) {
        Matcher matcher = ID_PATTERN.matcher(responseBody);
        assertTrue(matcher.find(), "Die Serverantwort enthaelt keine ID: " + responseBody);
        return UUID.fromString(matcher.group(1));
    }

    private int countOccurrences(String text, String token) {
        return text.split(Pattern.quote(token), -1).length - 1;
    }
}
