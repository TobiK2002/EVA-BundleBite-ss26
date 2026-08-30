package toja.bundlebite.nfas;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import rest.client.ApiClient;
import rest.client.ConsoleClient;
import rest.server.NotificationServer;
import rest.server.RequestLoggingFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft die nichtfunktionale Anforderung, dass Änderungen an
 * Sammelbestellungen serverseitig protokolliert werden.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupOrderLoggingIntegrationTest {

    private static final int CHANGE_COUNT = 50;

    @LocalServerPort
    private int port;

    private ApiClient apiClient;
    private UUID restaurantId;
    private UUID groupOrderId;
    private String userEmail;
    private Logger requestLogger;
    private Logger notificationLogger;
    private CapturingAppender requestLogAppender;
    private CapturingAppender notificationLogAppender;
    private String successMessage;

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void allConcurrentGroupOrderChangesAreLoggedOnTheServer() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");
        UUID dishId = createRestaurantUserAndDish();
        groupOrderId = createGroupOrder();
        List<ConsoleClient.OrderEntryResponse> orderEntries = createOrderEntries(dishId);

        startCapturingServerLogs();
        performConcurrentChanges(orderEntries);

        String updatePathPrefix = "PUT /api/group-orders/" + groupOrderId + "/order-entries/";
        long loggedRestChanges = requestLogAppender.messages().stream()
                .filter(message -> message.startsWith(updatePathPrefix) && message.contains(" -> 200 "))
                .count();
        String notificationText = "GroupOrder-ID: " + groupOrderId
                + "Es wurde ein Eintrag in der GroupOrder aktualisiert.";
        long loggedNotifications = notificationLogAppender.messages().stream()
                .filter(message -> message.contains(notificationText))
                .count();

        assertEquals(CHANGE_COUNT, loggedRestChanges,
                "Jede Änderung muss vom RequestLoggingFilter protokolliert werden");
        assertEquals(CHANGE_COUNT, loggedNotifications,
                "Jede Änderung muss vom NotificationServer protokolliert werden");
        successMessage = "NFA ERFÜLLT – Alle " + CHANGE_COUNT
                + " Änderungen wurden in beiden Server-Logs protokolliert.";
    }

    @AfterEach
    void cleanUp() throws Exception {
        try {
            stopCapturingServerLogs();

            if (apiClient == null) {
                return;
            }
            if (groupOrderId != null) {
                apiClient.delete("/group-orders/" + groupOrderId);
            }
            if (userEmail != null) {
                apiClient.delete("/users/" + userEmail);
            }
            if (restaurantId != null) {
                apiClient.delete("/restaurants/" + restaurantId);
            }
        } finally {
            if (successMessage != null) {
                System.out.println(successMessage);
            }
        }
    }

    private UUID createRestaurantUserAndDish() throws Exception {
        restaurantId = apiClient.post(
                "/restaurants",
                new ConsoleClient.CreateRestaurantRequest(
                        "Logging-Test Restaurant", "Teststraße 1 04109 Leipzig", 0),
                ConsoleClient.RestaurantResponse.class
        ).id();

        userEmail = "logging-test-" + UUID.randomUUID() + "@example.test";
        apiClient.post(
                "/users",
                new ConsoleClient.CreateUserRequest(
                        "Logging Testnutzer", userEmail, "Teststraße 1 04109 Leipzig"),
                ConsoleClient.UserResponse.class
        );

        return apiClient.post(
                "/restaurants/" + restaurantId + "/dishes",
                new ConsoleClient.CreateDishRequest(
                        "Logging-Testgericht", "Gericht für den Logging-Test", 9.99,
                        List.of("Teig", "Tomaten")),
                ConsoleClient.DishResponse.class
        ).id();
    }

    private UUID createGroupOrder() throws Exception {
        return apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(restaurantId, userEmail, 5),
                ConsoleClient.GroupOrderResponse.class
        ).id();
    }

    private List<ConsoleClient.OrderEntryResponse> createOrderEntries(UUID dishId) throws Exception {
        List<ConsoleClient.OrderEntryResponse> orderEntries = new ArrayList<>();
        for (int index = 0; index < CHANGE_COUNT; index++) {
            orderEntries.add(apiClient.post(
                    "/group-orders/" + groupOrderId + "/order-entries",
                    new ConsoleClient.CreateOrderEntryRequest(userEmail, dishId, 1),
                    ConsoleClient.OrderEntryResponse.class
            ));
        }
        return orderEntries;
    }

    private void performConcurrentChanges(List<ConsoleClient.OrderEntryResponse> orderEntries) throws Exception {
        CountDownLatch clientsReady = new CountDownLatch(CHANGE_COUNT);
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(CHANGE_COUNT);

        try {
            List<Future<?>> changes = new ArrayList<>();
            for (int index = 0; index < CHANGE_COUNT; index++) {
                UUID orderEntryId = orderEntries.get(index).id();
                int newQuantity = index + 2;
                changes.add(executor.submit(() -> {
                    ApiClient client = new ApiClient("http://localhost:" + port + "/api");
                    clientsReady.countDown();
                    assertTrue(startSignal.await(10, TimeUnit.SECONDS), "Startsignal wurde nicht empfangen");
                    client.put(
                            "/group-orders/" + groupOrderId + "/order-entries/" + orderEntryId,
                            new ConsoleClient.UpdateOrderEntryRequest(newQuantity)
                    );
                    return null;
                }));
            }

            assertTrue(clientsReady.await(10, TimeUnit.SECONDS), "Nicht alle Clients waren bereit");
            startSignal.countDown();
            for (Future<?> change : changes) {
                change.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void startCapturingServerLogs() {
        requestLogger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        notificationLogger = (Logger) LoggerFactory.getLogger(NotificationServer.class);
        requestLogAppender = new CapturingAppender();
        notificationLogAppender = new CapturingAppender();
        requestLogger.addAppender(requestLogAppender);
        notificationLogger.addAppender(notificationLogAppender);
        requestLogAppender.start();
        notificationLogAppender.start();
    }

    private void stopCapturingServerLogs() {
        if (requestLogger != null && requestLogAppender != null) {
            requestLogger.detachAppender(requestLogAppender);
            requestLogAppender.stop();
        }
        if (notificationLogger != null && notificationLogAppender != null) {
            notificationLogger.detachAppender(notificationLogAppender);
            notificationLogAppender.stop();
        }
    }

    private static class CapturingAppender extends AppenderBase<ILoggingEvent> {
        private final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();

        @Override
        protected void append(ILoggingEvent event) {
            messages.add(event.getFormattedMessage());
        }

        private List<String> messages() {
            return List.copyOf(messages);
        }
    }
}
