package rest.client.menus;

import rest.client.ApiClient;
import rest.client.ConsoleClient;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class RestaurantMenu {

    private final Scanner scanner;
    private final ApiClient apiClient;
    private UUID loggedInRestaurantId;

    public RestaurantMenu(Scanner scanner,ApiClient apiClient) {
        this.scanner = scanner;
        this.apiClient = apiClient;
    }


    public void showRestaurantMenu(UUID passedLoggedInRestaurantId) {
        boolean logout = false;
        this.loggedInRestaurantId=passedLoggedInRestaurantId;

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
                    this.loggedInRestaurantId = null;
                    logout = true;
                }
                default -> System.out.println("Ungültige Eingabe.");
            }
        }
    }

    private void showOwnDishes() {
        if (this.loggedInRestaurantId == null) {
            System.out.println("Du bist nicht als Restaurant eingeloggt.");
            return;
        }

        showDishesForRestaurant(this.loggedInRestaurantId);

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

            System.out.print("Preis in Euro.Cent: ");
            double price = Double.parseDouble(scanner.nextLine());

            System.out.print("Zutaten kommagetrennt: ");
            List<String> ingredients = List.of(scanner.nextLine().split(","));

            ConsoleClient.CreateDishRequest request = new ConsoleClient.CreateDishRequest(
                    name,
                    description,
                    price,
                    ingredients
            );

            ConsoleClient.DishResponse dish = apiClient.post(
                    "/restaurants/" + loggedInRestaurantId + "/dishes",
                    request,
                    ConsoleClient.DishResponse.class
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

            System.out.print("Preis in Euro.Cent: ");
            double price = Double.parseDouble(scanner.nextLine());

            System.out.print("Zutaten kommagetrennt: ");
            List<String> ingredients = List.of(scanner.nextLine().split(","));

            ConsoleClient.UpdateDishRequest request = new ConsoleClient.UpdateDishRequest(
                    loggedInRestaurantId,
                    name,
                    description,
                    price,
                    ingredients
            );

            apiClient.put("/dishes/" + dishId, request);

            System.out.println("Gericht wurde aktualisiert.");
        } catch (Exception exception) {
            System.out.println("Gericht konnte nicht aktualisiert werden: " + exception.getMessage());
        }
    }

    private void deleteDish() {
        try {

            System.out.print("Dish-ID: ");
            UUID dishId = UUID.fromString(scanner.nextLine());
            //Schauen ob Gericht in einer Bestellung vorhanden ist
            List<ConsoleClient.GroupOrderResponse> groupOrders = apiClient.getList("/group-orders", new TypeReference<>() {});
            if (groupOrders.isEmpty()) {
                apiClient.delete("/dishes/" + dishId);
                System.out.println("Gericht wurde gelöscht.");
            }
            else {
                for (ConsoleClient.GroupOrderResponse groupOrder : groupOrders) {
                    List<ConsoleClient.OrderEntryResponse> orderEntries = apiClient.getList("/order-entries/by-group-order/" + groupOrder.id(), new TypeReference<>() {
                    });
                    if (orderEntries.stream().anyMatch(orderEntry -> orderEntry.dishId().equals(dishId))) {
                        System.out.println("Gericht kann nicht Gelöscht werden, da es in einer Bestellung vorhanden ist.");
                        return;
                    }
                }
                apiClient.delete("/dishes/" + dishId);
                System.out.println("Gericht wurde gelöscht.");
            }
        } catch (Exception exception) {
            System.out.println("Gericht konnte nicht gelöscht werden: " + exception.getMessage());
        }
    }
}
