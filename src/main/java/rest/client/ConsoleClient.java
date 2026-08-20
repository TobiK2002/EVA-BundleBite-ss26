package rest.client;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleClient {

    private static final String BASE_URL = "http://localhost:8080/api";

    private final Scanner scanner = new Scanner(System.in);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID loggedInUserId;
    private UUID loggedInRestaurantId;

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("Willkommen bei BundleBite!");
            System.out.println("Bist du User oder Restaurant?");
            System.out.println("1. User");
            System.out.println("2. Restaurant");
            System.out.println("0. Beenden");
            System.out.print("Auswahl: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> showUserEntryMenu();
                case "2" -> showRestaurantEntryMenu();
                case "0" -> running = false;
                default -> System.out.println("Ungültige Eingabe.");
            }
        }

        System.out.println("BundleBite wurde beendet.");
    }

    private void showUserEntryMenu() {
        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("User-Bereich");
            System.out.println("1. User erstellen");
            System.out.println("2. Login mit User-ID");
            System.out.println("0. Zurück");
            System.out.print("Auswahl: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> createUser();
                case "2" -> loginUser();
                case "0" -> back = true;
                default -> System.out.println("Ungültige Eingabe.");
            }
        }
    }

    private void showUserMenu() {
        boolean logout = false;

        while (!logout) {
            System.out.println();
            System.out.println("User-Menü");
            System.out.println("1. Alle Restaurants anzeigen");
            System.out.println("2. Gerichte eines Restaurants anzeigen");
            System.out.println("3. GroupOrder erstellen");
            System.out.println("4. Alle GroupOrders anzeigen");
            System.out.println("5. GroupOrder beitreten");
            System.out.println("6. OrderEntry bearbeiten");
            System.out.println("0. Logout");
            System.out.print("Auswahl: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> showAllRestaurants();
                case "2" -> showDishesForRestaurant();
                case "3" -> createGroupOrder();
                case "4" -> showAllGroupOrders();
                case "5" -> joinGroupOrder();
                case "6" -> updateOrderEntry();
                case "0" -> {
                    loggedInUserId = null;
                    logout = true;
                }
                default -> System.out.println("Ungültige Eingabe.");
            }
        }
    }

    private void showRestaurantEntryMenu() {
        boolean back = false;

        while (!back) {
            System.out.println();
            System.out.println("Restaurant-Bereich");
            System.out.println("1. Restaurant erstellen");
            System.out.println("2. Login mit Restaurant-ID");
            System.out.println("0. Zurück");
            System.out.print("Auswahl: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> createRestaurant();
                case "2" -> loginRestaurant();
                case "0" -> back = true;
                default -> System.out.println("Ungültige Eingabe.");
            }
        }
    }

    private void showRestaurantMenu() {
        boolean logout = false;

        while (!logout) {
            System.out.println();
            System.out.println("Restaurant-Menü");
            System.out.println("1. Eigene Gerichte anzeigen");
            System.out.println("2. Gericht erstellen");
            System.out.println("3. Gericht bearbeiten");
            System.out.println("4. Gericht löschen");
            System.out.println("0. Logout");
            System.out.print("Auswahl: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> showOwnDishes();
                case "2" -> createDish();
                case "3" -> updateDish();
                case "4" -> deleteDish();
                case "0" -> {
                    loggedInRestaurantId = null;
                    logout = true;
                }
                default -> System.out.println("Ungültige Eingabe.");
            }
        }
    }

    private void createUser() {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("E-Mail: ");
            String email = scanner.nextLine();

            System.out.print("Adresse, Format 'Straße Hausnummer PLZ Stadt': ");
            String address = scanner.nextLine();

            CreateUserRequest request = new CreateUserRequest(name, email, address);

            UserResponse user = post("/users", request, UserResponse.class);

            loggedInUserId = user.id();

            System.out.println("User wurde erstellt.");
            System.out.println("Deine User-ID: " + user.id());

            showUserMenu();
        } catch (Exception exception) {
            System.out.println("User konnte nicht erstellt werden: " + exception.getMessage());
        }
    }

    private void loginUser() {
        try {
            System.out.print("User-ID: ");
            UUID userId = UUID.fromString(scanner.nextLine());

            UserResponse user = get("/users/" + userId, UserResponse.class);

            loggedInUserId = user.id();

            System.out.println("Login erfolgreich. Willkommen " + user.name() + "!");
            showUserMenu();
        } catch (Exception exception) {
            System.out.println("Login fehlgeschlagen: " + exception.getMessage());
        }
    }

    private void createRestaurant() {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Adresse, Format 'Straße Hausnummer PLZ Stadt': ");
            String address = scanner.nextLine();

            System.out.print("Mindestbestellwert: ");
            Double minOrderValue = Double.parseDouble(scanner.nextLine());

            CreateRestaurantRequest request = new CreateRestaurantRequest(name, address, minOrderValue);

            RestaurantResponse restaurant = post("/restaurants", request, RestaurantResponse.class);

            loggedInRestaurantId = restaurant.id();

            System.out.println("Restaurant wurde erstellt.");
            System.out.println("Deine Restaurant-ID: " + restaurant.id());

            showRestaurantMenu();
        } catch (Exception exception) {
            System.out.println("Restaurant konnte nicht erstellt werden: " + exception.getMessage());
        }
    }

    private void loginRestaurant() {
        try {
            System.out.print("Restaurant-ID: ");
            UUID restaurantId = UUID.fromString(scanner.nextLine());

            RestaurantResponse restaurant = get("/restaurants/" + restaurantId, RestaurantResponse.class);

            loggedInRestaurantId = restaurant.id();

            System.out.println("Login erfolgreich. Willkommen " + restaurant.name() + "!");
            showRestaurantMenu();
        } catch (Exception exception) {
            System.out.println("Login fehlgeschlagen: " + exception.getMessage());
        }
    }

    private void showAllRestaurants() {
        try {
            List<RestaurantResponse> restaurants = getList("/restaurants", new TypeReference<>() {
            });

            if (restaurants.isEmpty()) {
                System.out.println("Es gibt noch keine Restaurants.");
                return;
            }

            for (RestaurantResponse restaurant : restaurants) {
                System.out.println();
                System.out.println("ID: " + restaurant.id());
                System.out.println("Name: " + restaurant.name());
                System.out.println("Adresse: " + restaurant.address());
                System.out.println("Mindestbestellwert: " + restaurant.minOrderValue());
            }
        } catch (Exception exception) {
            System.out.println("Restaurants konnten nicht geladen werden: " + exception.getMessage());
        }
    }

    private void showDishesForRestaurant() {
        try {
            System.out.print("Restaurant-ID: ");
            UUID restaurantId = UUID.fromString(scanner.nextLine());

            showDishesForRestaurant(restaurantId);
        } catch (Exception exception) {
            System.out.println("Gerichte konnten nicht geladen werden: " + exception.getMessage());
        }
    }

    private void showOwnDishes() {
        if (loggedInRestaurantId == null) {
            System.out.println("Du bist nicht als Restaurant eingeloggt.");
            return;
        }

        showDishesForRestaurant(loggedInRestaurantId);
    }

    private void showDishesForRestaurant(UUID restaurantId) {
        try {
            List<DishResponse> dishes = getList("/restaurants/" + restaurantId + "/dishes", new TypeReference<>() {
            });

            if (dishes.isEmpty()) {
                System.out.println("Dieses Restaurant hat noch keine Gerichte.");
                return;
            }

            for (DishResponse dish : dishes) {
                System.out.println();
                System.out.println("ID: " + dish.id());
                System.out.println("Name: " + dish.name());
                System.out.println("Beschreibung: " + dish.description());
                System.out.println("Preis: " + dish.price());
                System.out.println("Zutaten: " + dish.ingredients());
            }
        } catch (Exception exception) {
            System.out.println("Gerichte konnten nicht geladen werden: " + exception.getMessage());
        }
    }

    private void createGroupOrder() {
        try {
            if (loggedInUserId == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return;
            }

            System.out.println("Wähle ein Restaurant aus:");
            showAllRestaurants();

            System.out.print("Restaurant-ID: ");
            UUID restaurantId = UUID.fromString(scanner.nextLine());

            System.out.print("Ablaufzeit in Minuten: ");
            int expiresAt = Integer.parseInt(scanner.nextLine());

            CreateGroupOrderRequest request = new CreateGroupOrderRequest(
                    restaurantId,
                    loggedInUserId,
                    expiresAt
            );

            GroupOrderResponse groupOrder = post("/group-orders", request, GroupOrderResponse.class);

            System.out.println("GroupOrder wurde erstellt.");
            System.out.println("GroupOrder-ID: " + groupOrder.id());
        } catch (Exception exception) {
            System.out.println("GroupOrder konnte nicht erstellt werden: " + exception.getMessage());
        }
    }

    private void showAllGroupOrders() {
        try {
            List<GroupOrderResponse> groupOrders = getList("/group-orders", new TypeReference<>() {
            });

            if (groupOrders.isEmpty()) {
                System.out.println("Es gibt aktuell keine GroupOrders.");
                return;
            }

            for (GroupOrderResponse groupOrder : groupOrders) {
                System.out.println();
                System.out.println("ID: " + groupOrder.id());
                System.out.println("Restaurant-ID: " + groupOrder.restaurantId());
                System.out.println("Creator-User-ID: " + groupOrder.creatorUserId());
                System.out.println("ExpiresAt: " + groupOrder.expiresAt());
            }
        } catch (Exception exception) {
            System.out.println("GroupOrders konnten nicht geladen werden: " + exception.getMessage());
        }
    }

    private void joinGroupOrder() {
        try {
            if (loggedInUserId == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return;
            }

            System.out.print("GroupOrder-ID: ");
            UUID groupOrderId = UUID.fromString(scanner.nextLine());

            GroupOrderResponse groupOrder = get("/group-orders/" + groupOrderId, GroupOrderResponse.class);

            System.out.println("Gerichte des Restaurants:");
            showDishesForRestaurant(groupOrder.restaurantId());

            System.out.print("Dish-ID: ");
            UUID dishId = UUID.fromString(scanner.nextLine());

            System.out.print("Menge: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            CreateOrderEntryRequest request = new CreateOrderEntryRequest(
                    loggedInUserId,
                    dishId,
                    quantity
            );

            OrderEntryResponse orderEntry = post(
                    "/group-orders/" + groupOrderId + "/order-entries",
                    request,
                    OrderEntryResponse.class
            );

            System.out.println("Du bist der GroupOrder beigetreten.");
            System.out.println("OrderEntry-ID: " + orderEntry.id());
        } catch (Exception exception) {
            System.out.println("Beitritt zur GroupOrder fehlgeschlagen: " + exception.getMessage());
        }
    }

    private void updateOrderEntry() {
        try {
            System.out.print("GroupOrder-ID: ");
            UUID groupOrderId = UUID.fromString(scanner.nextLine());

            System.out.print("OrderEntry-ID: ");
            UUID orderEntryId = UUID.fromString(scanner.nextLine());

            System.out.print("Neue Menge: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            UpdateOrderEntryRequest request = new UpdateOrderEntryRequest(quantity);

            put("/group-orders/" + groupOrderId + "/order-entries/" + orderEntryId, request);

            System.out.println("OrderEntry wurde aktualisiert.");
        } catch (Exception exception) {
            System.out.println("OrderEntry konnte nicht aktualisiert werden: " + exception.getMessage());
        }
    }

    private void createDish() {
        try {
            if (loggedInRestaurantId == null) {
                System.out.println("Du musst als Restaurant eingeloggt sein.");
                return;
            }

            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Beschreibung: ");
            String description = scanner.nextLine();

            System.out.print("Preis in Cent: ");
            long price = Long.parseLong(scanner.nextLine());

            System.out.print("Zutaten kommagetrennt: ");
            List<String> ingredients = List.of(scanner.nextLine().split(","));

            CreateDishRequest request = new CreateDishRequest(
                    name,
                    description,
                    price,
                    ingredients
            );

            DishResponse dish = post(
                    "/restaurants/" + loggedInRestaurantId + "/dishes",
                    request,
                    DishResponse.class
            );

            System.out.println("Gericht wurde erstellt.");
            System.out.println("Dish-ID: " + dish.id());
        } catch (Exception exception) {
            System.out.println("Gericht konnte nicht erstellt werden: " + exception.getMessage());
        }
    }

    private void updateDish() {
        try {
            if (loggedInRestaurantId == null) {
                System.out.println("Du musst als Restaurant eingeloggt sein.");
                return;
            }

            System.out.print("Dish-ID: ");
            UUID dishId = UUID.fromString(scanner.nextLine());

            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Beschreibung: ");
            String description = scanner.nextLine();

            System.out.print("Preis in Cent: ");
            long price = Long.parseLong(scanner.nextLine());

            System.out.print("Zutaten kommagetrennt: ");
            List<String> ingredients = List.of(scanner.nextLine().split(","));

            UpdateDishRequest request = new UpdateDishRequest(
                    loggedInRestaurantId,
                    name,
                    description,
                    price,
                    ingredients
            );

            put("/dishes/" + dishId, request);

            System.out.println("Gericht wurde aktualisiert.");
        } catch (Exception exception) {
            System.out.println("Gericht konnte nicht aktualisiert werden: " + exception.getMessage());
        }
    }

    private void deleteDish() {
        try {
            System.out.print("Dish-ID: ");
            UUID dishId = UUID.fromString(scanner.nextLine());

            delete("/dishes/" + dishId);

            System.out.println("Gericht wurde gelöscht.");
        } catch (Exception exception) {
            System.out.println("Gericht konnte nicht gelöscht werden: " + exception.getMessage());
        }
    }

    private <T> T get(String path, Class<T> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);

        return objectMapper.readValue(response.body(), responseType);
    }

    private <T> List<T> getList(String path, TypeReference<List<T>> responseType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);

        return objectMapper.readValue(response.body(), responseType);
    }

    private <T> T post(String path, Object body, Class<T> responseType) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);

        return objectMapper.readValue(response.body(), responseType);
    }

    private void put(String path, Object body) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
    }

    private void delete(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        validateResponse(response);
    }

    private void validateResponse(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    public record CreateUserRequest(
            String name,
            String email,
            String address
    ) {
    }

    public record CreateRestaurantRequest(
            String name,
            String address,
            Double minOrderValue
    ) {
    }

    public record CreateDishRequest(
            String name,
            String description,
            long price,
            List<String> ingredients
    ) {
    }

    public record UpdateDishRequest(
            UUID restaurantId,
            String name,
            String description,
            long price,
            List<String> ingredients
    ) {
    }

    public record CreateGroupOrderRequest(
            UUID restaurantId,
            UUID creatorUserId,
            int expiresAt
    ) {
    }

    public record CreateOrderEntryRequest(
            UUID userId,
            UUID dishId,
            int quantity
    ) {
    }

    public record UpdateOrderEntryRequest(
            int quantity
    ) {
    }

    public record UserResponse(
            UUID id,
            String name,
            String email,
            Object address
    ) {
    }

    public record RestaurantResponse(
            UUID id,
            String name,
            Object address,
            Double minOrderValue
    ) {
    }

    public record DishResponse(
            UUID id,
            UUID restaurantId,
            String name,
            String description,
            long price,
            List<String> ingredients
    ) {
    }

    public record GroupOrderResponse(
            UUID id,
            UUID restaurantId,
            UUID creatorUserId,
            int expiresAt
    ) {
    }

    public record OrderEntryResponse(
            UUID id,
            UUID userId,
            UUID dishId,
            int quantity
    ) {
    }
}