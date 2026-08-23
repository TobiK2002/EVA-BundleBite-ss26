package rest.client;


import rest.client.menus.RestaurantMenu;
import rest.client.menus.UserMenu;
import rest.client.ApiClient;
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

    private final Scanner scanner = new Scanner(System.in);
    private final UserMenu userMenu;
    private final RestaurantMenu restaurantMenu;
    private final ApiClient apiClient;


    private String loggedInUserEmail;
    private UUID loggedInRestaurantId;

    ConsoleClient() {
        this.apiClient= new ApiClient();
        this.userMenu= new UserMenu(scanner,apiClient);
        this.restaurantMenu= new RestaurantMenu(scanner,apiClient);

    }

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


    private void createUser() {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("E-Mail: ");
            String email = scanner.nextLine();

            System.out.print("Adresse, Format 'Straße Hausnummer PLZ Stadt': ");
            String address = scanner.nextLine();

            CreateUserRequest request = new CreateUserRequest(name, email, address);

            UserResponse user = apiClient.post("/users", request, UserResponse.class);

            NotificationClient notificationClient = new NotificationClient(user.email);
            notificationClient.connect();

            loggedInUserEmail = user.email;

            System.out.println("User wurde erstellt.");
            System.out.println("Deine User-E-Mail: " + user.email());

            userMenu.showUserMenu(user);
            notificationClient.close();

        } catch (Exception exception) {
            System.out.println("User konnte nicht erstellt werden: " + exception.getMessage());
        }
    }

    private void loginUser() {
        try {
            System.out.print("User-E-Mail: ");
            String userEmail = scanner.nextLine();

            UserResponse user = apiClient.get("/users/" + userEmail, UserResponse.class);

            NotificationClient notificationClient = new NotificationClient(user.email);
            notificationClient.connect();

            loggedInUserEmail = user.email;

            System.out.println("Login erfolgreich. Willkommen " + user.name() + "!");
            userMenu.showUserMenu(user);

            notificationClient.close();

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

            RestaurantResponse restaurant = apiClient.post("/restaurants", request, RestaurantResponse.class);

            loggedInRestaurantId = restaurant.id();

            System.out.println("Restaurant wurde erstellt.");
            System.out.println("Deine Restaurant-ID: " + restaurant.id());

            restaurantMenu.showRestaurantMenu(loggedInRestaurantId);

        } catch (Exception exception) {
            System.out.println("Restaurant konnte nicht erstellt werden: " + exception.getMessage());
        }
    }

    private void loginRestaurant() {
        try {
            System.out.print("Restaurant-ID: ");
            UUID restaurantId = UUID.fromString(scanner.nextLine());

            RestaurantResponse restaurant = apiClient.get("/restaurants/" + restaurantId, RestaurantResponse.class);

            loggedInRestaurantId = restaurant.id();

            System.out.println("Login erfolgreich. Willkommen " + restaurant.name() + "!");


            restaurantMenu.showRestaurantMenu(loggedInRestaurantId);

        } catch (Exception exception) {
            System.out.println("Login fehlgeschlagen: " + exception.getMessage());
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
            String creatorUserEmail,
            int expiresAt
    ) {
    }
    public record CreateGroupOrderEntryRequest(
            String userEmail,
            UUID dishId,
            int quantity
    ) {
    }

    public record CreateOrderEntryRequest(
            String userEmail,
            UUID dishId,
            int quantity
    ) {
    }

    public record UpdateOrderEntryRequest(
            int quantity
    ) {
    }

    public record UserResponse(
            String name,
            String email,
            AddressResponse address
    ) {
    }

    public record RestaurantResponse(
            UUID id,
            String name,
            AddressResponse address,
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
            String creatorUserEmail,
            int expiresAt
    ) {
    }

    public record OrderEntryResponse(
            UUID id,
            String userEmail,
            UUID dishId,
            int quantity,
            double sumPrice
    ) {
    }

    public record AddressResponse(
            String street,
            String houseNumber,
            String postalCode,
            String city
    ) {
    }
}