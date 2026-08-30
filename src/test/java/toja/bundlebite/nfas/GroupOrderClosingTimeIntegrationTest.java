package toja.bundlebite.nfas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import rest.client.ApiClient;
import rest.client.ConsoleClient;
import tools.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft die nichtfunktionale Anforderung, dass Sammelbestellungen spätestens
 * eine Sekunde nach Ablauf ihres Zeitfensters automatisch geschlossen werden.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupOrderClosingTimeIntegrationTest {

    private static final int GROUP_ORDER_COUNT = 50;
    private static final int RUNTIME_MINUTES = 1;
    private static final long MAX_AVERAGE_CLOSING_TIME_MILLIS = 61_000;
    private static final long POLLING_INTERVAL_MILLIS = 10;

    @LocalServerPort
    private int port;

    private ApiClient apiClient;
    private UUID restaurantId;
    private String creatorEmail;
    private final Set<UUID> createdGroupOrderIds = new HashSet<>();

    @Test
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    void groupOrdersCloseOnAverageWithinOneSecondAfterTheirTimeWindow() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");
        createRestaurantAndUser();

        Map<UUID, Long> creationTimesNanos = createGroupOrders();
        waitUntilFirstGroupOrderCanHaveExpired(creationTimesNanos.values());
        Map<UUID, Long> closingTimesNanos = waitForAllGroupOrdersToClose(creationTimesNanos.keySet());

        // Diese Bestellungen wurden bereits automatisch geschlossen und müssen
        // im Cleanup nicht noch einmal per HTTP gelöscht werden.
        createdGroupOrderIds.removeAll(closingTimesNanos.keySet());

        double averageClosingTimeMillis = creationTimesNanos.entrySet().stream()
                .mapToLong(entry -> closingTimesNanos.get(entry.getKey()) - entry.getValue())
                .average()
                .orElseThrow()
                / 1_000_000.0;

        boolean requirementMet = averageClosingTimeMillis <= MAX_AVERAGE_CLOSING_TIME_MILLIS;
        String result = "Durchschnittliche Schließzeit: %.2f ms (Grenzwert: %d ms)"
                .formatted(averageClosingTimeMillis, MAX_AVERAGE_CLOSING_TIME_MILLIS);

        if (requirementMet) {
            System.out.println("NFA ERFÜLLT – " + result);
        } else {
            System.err.println("NFA NICHT ERFÜLLT – " + result);
        }

        assertTrue(requirementMet, () -> "Sammelbestellungen wurden durchschnittlich zu spät geschlossen. " + result);
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (apiClient == null) {
            return;
        }

        for (UUID groupOrderId : createdGroupOrderIds) {
            try {
                apiClient.delete("/group-orders/" + groupOrderId);
            } catch (RuntimeException ignored) {
                // Bereits automatisch geschlossene Sammelbestellungen sind korrekt bereinigt.
            }
        }

        if (creatorEmail != null) {
            apiClient.delete("/users/" + creatorEmail);
        }
        if (restaurantId != null) {
            apiClient.delete("/restaurants/" + restaurantId);
        }
    }

    private void createRestaurantAndUser() throws Exception {
        ConsoleClient.RestaurantResponse restaurant = apiClient.post(
                "/restaurants",
                new ConsoleClient.CreateRestaurantRequest(
                        "Schließzeit-Test Restaurant", "Teststraße 1 04109 Leipzig", 0),
                ConsoleClient.RestaurantResponse.class
        );
        restaurantId = restaurant.id();

        creatorEmail = "closing-time-" + UUID.randomUUID() + "@example.test";
        apiClient.post(
                "/users",
                new ConsoleClient.CreateUserRequest(
                        "Schließzeit Testnutzer", creatorEmail, "Teststraße 1 04109 Leipzig"),
                ConsoleClient.UserResponse.class
        );
    }

    private Map<UUID, Long> createGroupOrders() throws Exception {
        Map<UUID, Long> creationTimesNanos = new HashMap<>();

        for (int index = 0; index < GROUP_ORDER_COUNT; index++) {
            long creationTimeNanos = System.nanoTime();
            ConsoleClient.GroupOrderResponse groupOrder = apiClient.post(
                    "/group-orders",
                    new ConsoleClient.CreateGroupOrderRequest(
                            restaurantId, creatorEmail, RUNTIME_MINUTES),
                    ConsoleClient.GroupOrderResponse.class
            );
            createdGroupOrderIds.add(groupOrder.id());
            creationTimesNanos.put(groupOrder.id(), creationTimeNanos);
        }

        return creationTimesNanos;
    }

    private void waitUntilFirstGroupOrderCanHaveExpired(Iterable<Long> creationTimesNanos)
            throws InterruptedException {
        long firstCreationTimeNanos = Long.MAX_VALUE;
        for (long creationTimeNanos : creationTimesNanos) {
            firstCreationTimeNanos = Math.min(firstCreationTimeNanos, creationTimeNanos);
        }

        long earliestExpirationTimeNanos = firstCreationTimeNanos
                + TimeUnit.MINUTES.toNanos(RUNTIME_MINUTES);
        long remainingNanos = earliestExpirationTimeNanos - System.nanoTime();

        if (remainingNanos > 0) {
            System.out.println("Warte bis zum Ablauf des Zeitfensters der ersten Sammelbestellung …");
            TimeUnit.NANOSECONDS.sleep(remainingNanos);
        }
    }

    private Map<UUID, Long> waitForAllGroupOrdersToClose(Set<UUID> groupOrderIds) throws Exception {
        Set<UUID> openGroupOrderIds = new HashSet<>(groupOrderIds);
        Map<UUID, Long> closingTimesNanos = new HashMap<>();

        while (!openGroupOrderIds.isEmpty()) {
            List<ConsoleClient.GroupOrderResponse> activeGroupOrders = apiClient.getList(
                    "/group-orders", new TypeReference<>() {
                    }
            );
            Set<UUID> activeGroupOrderIds = activeGroupOrders.stream()
                    .map(ConsoleClient.GroupOrderResponse::id)
                    .collect(java.util.stream.Collectors.toSet());
            long observationTimeNanos = System.nanoTime();

            openGroupOrderIds.removeIf(groupOrderId -> {
                if (activeGroupOrderIds.contains(groupOrderId)) {
                    return false;
                }
                closingTimesNanos.put(groupOrderId, observationTimeNanos);
                return true;
            });

            if (!openGroupOrderIds.isEmpty()) {
                Thread.sleep(POLLING_INTERVAL_MILLIS);
            }
        }

        return closingTimesNanos;
    }
}
