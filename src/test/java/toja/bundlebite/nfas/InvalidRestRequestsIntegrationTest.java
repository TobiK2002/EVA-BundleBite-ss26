package toja.bundlebite.nfas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import rest.client.ApiClient;
import rest.client.ConsoleClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Prüft die nichtfunktionale Anforderung, dass ungültige REST-Anfragen den
 * Server nicht zum Absturz bringen und mit einer Fehlerantwort beantwortet werden.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InvalidRestRequestsIntegrationTest {

    @LocalServerPort
    private int port;

    private ApiClient apiClient;
    private UUID restaurantId;
    private UUID closedGroupOrderId;
    private UUID validGroupOrderId;
    private UUID dishId;
    private String userEmail;

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void invalidRequestsReturnErrorsAndServerRemainsAvailable() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");
        createRestaurantUserAndDish();

        expectErrorResponse("Abruf einer nicht existierenden Sammelbestellung", () ->
                apiClient.get("/group-orders/" + UUID.randomUUID(), ConsoleClient.GroupOrderResponse.class));
        expectErrorResponse("Abruf eines nicht existierenden Nutzers", () ->
                apiClient.get("/users/nicht-vorhanden-" + UUID.randomUUID() + "@example.test",
                        ConsoleClient.UserResponse.class));

        closedGroupOrderId = apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(restaurantId, userEmail, 5),
                ConsoleClient.GroupOrderResponse.class
        ).id();
        apiClient.delete("/group-orders/" + closedGroupOrderId);

        expectErrorResponse("Hinzufügen zu einer geschlossenen Sammelbestellung", () ->
                apiClient.post(
                        "/group-orders/" + closedGroupOrderId + "/order-entries",
                        new ConsoleClient.CreateOrderEntryRequest(userEmail, dishId, 1),
                        ConsoleClient.OrderEntryResponse.class));

        // Eine gültige Anfrage nach den Fehlerfällen zeigt, dass der Server weiterhin funktioniert.
        validGroupOrderId = assertDoesNotThrow(() -> apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(restaurantId, userEmail, 5),
                ConsoleClient.GroupOrderResponse.class
        ), "Der Server muss nach ungültigen Anfragen weiter erreichbar sein").id();

        ConsoleClient.GroupOrderResponse groupOrder = assertDoesNotThrow(() -> apiClient.get(
                "/group-orders/" + validGroupOrderId,
                ConsoleClient.GroupOrderResponse.class
        ));
        assertNotNull(groupOrder, "Eine gültige Sammelbestellung muss weiterhin abrufbar sein");
        assertTrue(groupOrder.id().equals(validGroupOrderId),
                "Der Server muss die nach den Fehlerfällen erstellte Sammelbestellung korrekt zurückgeben");
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (apiClient == null) {
            return;
        }

        if (validGroupOrderId != null) {
            apiClient.delete("/group-orders/" + validGroupOrderId);
        }
        if (userEmail != null) {
            apiClient.delete("/users/" + userEmail);
        }
        if (restaurantId != null) {
            apiClient.delete("/restaurants/" + restaurantId);
        }
    }

    private void createRestaurantUserAndDish() throws Exception {
        restaurantId = apiClient.post(
                "/restaurants",
                new ConsoleClient.CreateRestaurantRequest(
                        "Fehleranfragen-Test Restaurant", "Teststraße 1 04109 Leipzig", 0),
                ConsoleClient.RestaurantResponse.class
        ).id();

        userEmail = "invalid-request-" + UUID.randomUUID() + "@example.test";
        apiClient.post(
                "/users",
                new ConsoleClient.CreateUserRequest(
                        "Fehleranfragen Testnutzer", userEmail, "Teststraße 1 04109 Leipzig"),
                ConsoleClient.UserResponse.class
        );

        dishId = apiClient.post(
                "/restaurants/" + restaurantId + "/dishes",
                new ConsoleClient.CreateDishRequest(
                        "Fehleranfragen-Testgericht", "Gericht für ungültige Anfragen", 9.99,
                        new ArrayList<>(List.of("Teig", "Tomaten"))),
                ConsoleClient.DishResponse.class
        ).id();
    }

    private void expectErrorResponse(String description, ThrowingRequest request) throws Exception {
        try {
            request.execute();
            fail(description + " hätte mit einer Fehlerantwort abgelehnt werden müssen");
        } catch (RuntimeException exception) {
            String errorMessage = exception.getMessage();
            assertNotNull(errorMessage, description + " muss eine Fehlermeldung enthalten");
            assertTrue(errorMessage.startsWith("HTTP "),
                    () -> description + " muss als HTTP-Fehler beantwortet werden: " + errorMessage);
        }
    }

    @FunctionalInterface
    private interface ThrowingRequest {
        void execute() throws Exception;
    }
}
