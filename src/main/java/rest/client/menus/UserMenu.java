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
    private ConsoleClient.UserResponse loggedInUser;

    private static final String SERVER_HOST = "localhost";
    private static final int SOCKET_PORT = 8081;


    public UserMenu(Scanner scanner,ApiClient apiClient) {
        this.scanner = scanner;
        this.apiClient = apiClient;
    }

    public void showUserMenu(ConsoleClient.UserResponse user) {
        boolean logout = false;

        this.loggedInUser = user;

        while (!logout) {
            System.out.println();
            System.out.println("User-Menü");
            System.out.println("1. Alle Restaurants anzeigen");
            System.out.println("2. Gerichte eines Restaurants anzeigen");
            System.out.println("3. GroupOrder erstellen");
            System.out.println("4. Alle GroupOrders anzeigen");
            System.out.println("5. Meine GroupOrders anzeigen");
            System.out.println("6. GroupOrder beitreten");
            System.out.println("7. Meine OrderEntries einer GroupOrder anzeigen");
            System.out.println("8. OrderEntry bearbeiten");
            System.out.println("0. Logout");
            System.out.print("Auswahl: ");

            String input = scanner.nextLine();

            switch (input) {
                case "1" -> showAllRestaurants();
                case "2" -> showDishesForRestaurant();
                case "3" -> createGroupOrder();
                case "4" -> showAllGroupOrders();
                case "5" -> showMyGroupOrders();
                case "6" -> joinGroupOrder();
                case "7" -> showOrderEntriesByUserByGroupOrder();
                case "8" -> updateOrderEntry();
                case "0" -> {
                    this.loggedInUser = null;
                    logout = true;
                }
                default -> System.out.println("Ungültige Eingabe.");
            }
        }
    }


    private void showAllRestaurants() {
        try {

            List<ConsoleClient.RestaurantResponse> restaurants = apiClient.getList("/restaurants/city/" + loggedInUser.address().city(), new TypeReference<>() {
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


    private ConsoleClient.GroupOrderResponse createGroupOrder() {
        try {
            if (loggedInUser == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return null;
            }

            System.out.println("Wähle ein Restaurant aus:");
            showAllRestaurants();

            System.out.print("Restaurant-ID: ");
            UUID restaurantId = UUID.fromString(scanner.nextLine());

            System.out.print("Ablaufzeit in Minuten: ");
            int expiresAt = Integer.parseInt(scanner.nextLine());

            ConsoleClient.CreateGroupOrderRequest request = new ConsoleClient.CreateGroupOrderRequest(
                    restaurantId,
                    this.loggedInUser.email(),
                    expiresAt
            );

            ConsoleClient.GroupOrderResponse groupOrder = apiClient.post("/group-orders", request, ConsoleClient.GroupOrderResponse.class);

            System.out.println("GroupOrder wurde erstellt.");
            System.out.println("GroupOrder-ID: " + groupOrder.id());
            return groupOrder;
        } catch (Exception exception) {
            System.out.println("GroupOrder konnte nicht erstellt werden: " + exception.getMessage());
            return null;
        }
    }

// Zeigt alle GroupOrders mit selber PLZ des Users an
    private void showAllGroupOrders() {
        try {

            List<ConsoleClient.GroupOrderResponse> groupOrders = apiClient.getList("/group-orders/by-postal/"+ loggedInUser.address().postalCode(), new TypeReference<>() {
            });

            if (groupOrders.isEmpty()) {
                System.out.println("Es gibt aktuell keine GroupOrders in deiner Nähe.");
                return;
            }

            for (ConsoleClient.GroupOrderResponse groupOrder : groupOrders) {

                ConsoleClient.RestaurantResponse restaurant=new ConsoleClient.RestaurantResponse(null,null,null,null);
                try {
                    restaurant = apiClient.get("/restaurants/"+ groupOrder.restaurantId(),ConsoleClient.RestaurantResponse.class);
                } catch (Exception exception) {
                    System.out.println("Fehler beim Laden der Restaurants, um die GroupOrders in deiner Nähe anzuzeigen" + exception.getMessage());
                }

                System.out.println();
                System.out.println("GroupOrder-ID: " + groupOrder.id());
                System.out.println("Restaurant-ID: " + groupOrder.restaurantId());
                System.out.println("Restaurant-Name:" + restaurant.name());
                System.out.println("Restaurant-Adresse: " + restaurant.address());
                System.out.println("Restaurant-Mindestbestellwert: " + restaurant.minOrderValue());
                System.out.println("Creator-User-ID: " + groupOrder.creatorUserEmail());
                System.out.println("ExpiresAt: " + groupOrder.expiresAt());
            }
        } catch (Exception exception) {
            System.out.println("GroupOrders konnten nicht geladen werden: " + exception.getMessage());
        }
    }

    private void showMyGroupOrders() {
        try {
            if (loggedInUser == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return;
            }
            List<ConsoleClient.GroupOrderResponse> groupOrders= apiClient.getList("/group-orders/forUser/"+ this.loggedInUser.email(), new TypeReference<>(){});

            if (groupOrders.isEmpty()) {
                System.out.println("Du bist aktuell in keiner GroupOrder.");
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
            System.out.println("Deine GroupOrders konnten nicht geladen werden: " + exception.getMessage());
        }
    }

    private void  showAllGroupOrdersWithIndex(List<ConsoleClient.GroupOrderResponse> groupOrders) {
        for (ConsoleClient.GroupOrderResponse groupOrder : groupOrders) {

            //Default Restaurant Response, wenn API Call nicht klappt
            ConsoleClient.RestaurantResponse restaurant=new ConsoleClient.RestaurantResponse(null,null,null,null);
            try {
                restaurant = apiClient.get("/restaurants/"+ groupOrder.restaurantId(),ConsoleClient.RestaurantResponse.class);
            } catch (Exception exception) {
                System.out.println("Fehler beim Laden des Restaurants" + exception.getMessage());
            }

            System.out.println();
            System.out.println("Zahl für die Konsoleneingabe zum Auswählen: " + groupOrders.indexOf(groupOrder));
            System.out.println("GroupOrder-ID: " + groupOrder.id());
            System.out.println("Restaurant-ID: " + groupOrder.restaurantId());
            System.out.println("Restaurant-Name:" + restaurant.name());
            System.out.println("Restaurant-Adresse: " + restaurant.address());
            System.out.println("Restaurant-Mindestbestellwert: " + restaurant.minOrderValue());
            System.out.println("Creator-User-ID: " + groupOrder.creatorUserEmail());
            System.out.println("ExpiresAt: " + groupOrder.expiresAt());
        }
    }



    private void joinGroupOrder() {
        try {
            if (loggedInUser == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return;
            }
            UUID groupOrderId = null;

            //Schauen ob GroupOrders existieren, wenn ja anzeigen, wenn nein eine erstellen
            List<ConsoleClient.GroupOrderResponse> groupOrders = apiClient.getList("/group-orders/by-postal/" + loggedInUser.address().postalCode(), new TypeReference<>() {
            });

                if (groupOrders.isEmpty()) {
                    while (groupOrderId == null) {
                        System.out.println("Es gibt aktuell keine GroupOrders.\n Möchtest du eine erstellen? (y/n)");
                        String input = scanner.nextLine();
                        if (input.equalsIgnoreCase("y")) {

                            ConsoleClient.GroupOrderResponse newGroupOrder = createGroupOrder();
                            if (newGroupOrder == null) {
                                return;
                            }
                            groupOrderId = newGroupOrder.id();
                        }
                        else if (input.equalsIgnoreCase("n")) {
                            return;
                        }
                        else {
                            System.out.println("Ungültige Eingabe.");
                        }
                    }
                }

            //Wenn GroupOrders existieren
            if (groupOrderId == null) {
                showAllGroupOrdersWithIndex (groupOrders);
                System.out.print("GroupOrder-Zahl für Menüauswahl: ");
                groupOrderId=groupOrders.get(Integer.parseInt(scanner.nextLine())).id();
            }


            ConsoleClient.GroupOrderResponse groupOrder = apiClient.get("/group-orders/" + groupOrderId, ConsoleClient.GroupOrderResponse.class);

            System.out.println("Gerichte des Restaurants:");
            showDishesForRestaurant(groupOrder.restaurantId());

            System.out.print("Dish-ID: ");
            UUID dishId = UUID.fromString(scanner.nextLine());

            System.out.print("Menge: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            ConsoleClient.CreateOrderEntryRequest request = new ConsoleClient.CreateOrderEntryRequest(
                    this.loggedInUser.email(),
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

    private void showOrderEntriesByUserByGroupOrder() {
        UUID groupOrderId;
        try {
            if (loggedInUser == null) {
                System.out.println("Du musst als User eingeloggt sein.");
                return;
            }
            //Group-Orders anzeigen, in welche User sich befindet
            try {
                List<ConsoleClient.GroupOrderResponse> groupOrders = apiClient.getList("/group-orders/forUser/" + this.loggedInUser.email(), new TypeReference<>() {
                });

                if (groupOrders.isEmpty()) {
                    System.out.println("Du bist aktuell in keiner GroupOrder, bitte erstell erst eine!");
                    return;
                }

                System.out.println("Dies sind deine aktuellen GroupOrders, bitte wähle eine aus, um die Einträge zu sehen:\n ");
                showAllGroupOrdersWithIndex(groupOrders);
                System.out.println();

                System.out.print("GroupOrder-Zahl für Menüauswahl: ");
                groupOrderId=groupOrders.get(Integer.parseInt(scanner.nextLine())).id();

            } catch (Exception exception) {
                System.out.println("Deine GroupOrders konnten nicht geladen werden " + exception.getMessage());
                return;
            }
            //Für die ausgewählte GroupOrder, in welche User sich befindet, werden seine Einträge gelistet
            List<ConsoleClient.OrderEntryResponse> orderEntries = apiClient.getList("/group-orders/with-email/"+ groupOrderId + "/order-entries/" + this.loggedInUser.email(), new TypeReference<>() {
            });

            for (ConsoleClient.OrderEntryResponse orderEntry : orderEntries) {
                //Default Dish Response, wenn API Call nicht klappt
                ConsoleClient.DishResponse dish = new ConsoleClient.DishResponse(
                        orderEntry.dishId(),
                        null,
                        "ungültig",
                        "ungültig",
                        0,
                        List.of()
                );
                //Lade Gericht
                try {
                    dish = apiClient.get("/dishes/" + orderEntry.dishId(), ConsoleClient.DishResponse.class);
                } catch (Exception exception) {
                    System.out.println("Fehler beim Laden des Gerichtes");
                }
                System.out.println();
                System.out.println("ID: " + orderEntry.id());
                System.out.println("Gericht-ID: " + orderEntry.dishId());
                System.out.println("Name: " + dish.name());
                System.out.println("Preis: " + dish.price());
                System.out.println("Menge: " + orderEntry.quantity());
                System.out.println("Gesamtpreis des Eintrags: " + orderEntry.sumPrice());
            }

        } catch (Exception exception) {
            System.out.println("OrderEntries konnten nicht geladen werden: " + exception.getMessage());
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
