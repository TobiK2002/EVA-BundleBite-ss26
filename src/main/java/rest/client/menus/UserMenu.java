package rest.client.menus;

import rest.client.ApiClient;
import rest.client.ConsoleClient;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class UserMenu {

    private final Scanner scanner;
    private final ApiClient apiClient;
    private String loggedInUserEmail;


    public UserMenu(Scanner scanner,ApiClient apiClient) {
        this.scanner = scanner;
        this.apiClient = apiClient;
    }

    public void showUserMenu(String loggedInUserEmail) {
        boolean logout = false;
        this.loggedInUserEmail=loggedInUserEmail;

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
                    this.loggedInUserEmail = null;
                    logout = true;
                }
                default -> System.out.println("Ungültige Eingabe.");
            }
        }
    }


    private void showAllRestaurants() {
        try {
            List<ConsoleClient.RestaurantResponse> restaurants = apiClient.getList("/restaurants", new TypeReference<>() {
            });

            if (restaurants.isEmpty()) {
                System.out.println("Es gibt noch keine Restaurants.");
                return;
            }

            for (ConsoleClient.RestaurantResponse restaurant : restaurants) {
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

    private void showDishesForRestaurant(UUID restaurantId) {
        try {
            List<ConsoleClient.DishResponse> dishes = apiClient.getList("/restaurants/" + restaurantId + "/dishes", new TypeReference<>() {
            });

            if (dishes.isEmpty()) {
                System.out.println("Dieses Restaurant hat noch keine Gerichte.");
                return;
            }

            for (ConsoleClient.DishResponse dish : dishes) {
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
            if (loggedInUserEmail == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return;
            }

            System.out.println("Wähle ein Restaurant aus:");
            showAllRestaurants();

            System.out.print("Restaurant-ID: ");
            UUID restaurantId = UUID.fromString(scanner.nextLine());

            System.out.print("Ablaufzeit in Minuten: ");
            int expiresAt = Integer.parseInt(scanner.nextLine());

            ConsoleClient.CreateGroupOrderRequest request = new ConsoleClient.CreateGroupOrderRequest(
                    restaurantId,
                    this.loggedInUserEmail,
                    expiresAt
            );

            ConsoleClient.GroupOrderResponse groupOrder = apiClient.post("/group-orders", request, ConsoleClient.GroupOrderResponse.class);

            System.out.println("GroupOrder wurde erstellt.");
            System.out.println("GroupOrder-ID: " + groupOrder.id());
        } catch (Exception exception) {
            System.out.println("GroupOrder konnte nicht erstellt werden: " + exception.getMessage());
        }
    }

    private void showAllGroupOrders() {
        try {
            List<ConsoleClient.GroupOrderResponse> groupOrders = apiClient.getList("/group-orders", new TypeReference<>() {
            });

            if (groupOrders.isEmpty()) {
                System.out.println("Es gibt aktuell keine GroupOrders.");
                return;
            }

            for (ConsoleClient.GroupOrderResponse groupOrder : groupOrders) {
                System.out.println();
                System.out.println("ID: " + groupOrder.id());
                System.out.println("Restaurant-ID: " + groupOrder.restaurantId());
                System.out.println("Creator-User-ID: " + groupOrder.creatorUserEmail());
                System.out.println("ExpiresAt: " + groupOrder.expiresAt());
            }
        } catch (Exception exception) {
            System.out.println("GroupOrders konnten nicht geladen werden: " + exception.getMessage());
        }
    }

    private void joinGroupOrder() {
        try {
            if (loggedInUserEmail == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return;
            }

            System.out.print("GroupOrder-ID: ");
            UUID groupOrderId = UUID.fromString(scanner.nextLine());

            ConsoleClient.GroupOrderResponse groupOrder = apiClient.get("/group-orders/" + groupOrderId, ConsoleClient.GroupOrderResponse.class);

            System.out.println("Gerichte des Restaurants:");
            showDishesForRestaurant(groupOrder.restaurantId());

            System.out.print("Dish-ID: ");
            UUID dishId = UUID.fromString(scanner.nextLine());

            System.out.print("Menge: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            ConsoleClient.CreateOrderEntryRequest request = new ConsoleClient.CreateOrderEntryRequest(
                    this.loggedInUserEmail,
                    dishId,
                    quantity
            );

            ConsoleClient.OrderEntryResponse orderEntry = apiClient.post(
                    "/group-orders/" + groupOrderId + "/order-entries",
                    request,
                    ConsoleClient.OrderEntryResponse.class
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

            ConsoleClient.UpdateOrderEntryRequest request = new ConsoleClient.UpdateOrderEntryRequest(quantity);

            apiClient.put("/group-orders/" + groupOrderId + "/order-entries/" + orderEntryId, request);

            System.out.println("OrderEntry wurde aktualisiert.");
        } catch (Exception exception) {
            System.out.println("OrderEntry konnte nicht aktualisiert werden: " + exception.getMessage());
        }
    }
}
