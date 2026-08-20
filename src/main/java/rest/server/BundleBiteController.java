package rest.server;

import Core.Models.Dish;
import Core.Models.GroupOrder;
import Core.Models.OrderEntry;
import Core.Models.Restaurant;
import Core.Models.User;
import Core.Services.DishService;
import Core.Services.GroupOrderService;
import Core.Services.OrderEntryService;
import Core.Services.RestaurantService;
import Core.Services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BundleBiteController {

    private final DishService dishService = new DishService();
    private final RestaurantService restaurantService = new RestaurantService(dishService);

    private final UserService userService = new UserService();
    private final OrderEntryService orderEntryService = new OrderEntryService();

    private final GroupOrderService groupOrderService =
            new GroupOrderService(restaurantService, userService, orderEntryService);

    // -------------------------
    // Users
    // -------------------------

    @PostMapping("/users")
    public User createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(
                request.name(),
                request.email(),
                request.address()
        );
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public User getUserById(@PathVariable UUID userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/users/{userId}")
    public void updateUser(
            @PathVariable UUID userId,
            @RequestBody UpdateUserRequest request
    ) {
        User user = new User(
                userId,
                request.name(),
                request.email(),
                request.address()
        );

        userService.updateUser(user);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
    }

    @DeleteMapping("/users")
    public void deleteAllUsers() {
        userService.deleteAllUsers();
    }


    // -------------------------
    // Restaurants
    // -------------------------

    @PostMapping("/restaurants")
    public Restaurant createRestaurant(@RequestBody CreateRestaurantRequest request) {
        return restaurantService.createRestaurant(
                request.name(),
                request.address(),
                request.minOrderValue()
        );
    }

    @GetMapping("/restaurants")
    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @GetMapping("/restaurants/{restaurantId}")
    public Restaurant getRestaurantById(@PathVariable UUID restaurantId) {
        return restaurantService.getRestaurantById(restaurantId);
    }

    @PutMapping("/restaurants/{restaurantId}")
    public void updateRestaurant(
            @PathVariable UUID restaurantId,
            @RequestBody UpdateRestaurantRequest request
    ) {
        Restaurant restaurant = new Restaurant(
                restaurantId,
                request.name(),
                request.address(),
                request.minOrderValue()
        );

        restaurantService.updateRestaurant(restaurant);
    }

    @DeleteMapping("/restaurants/{restaurantId}")
    public void deleteRestaurant(@PathVariable UUID restaurantId) {
        restaurantService.deleteRestaurant(restaurantId);
    }

    // -------------------------
    // Dishes über RestaurantService
    // -------------------------

    @PostMapping("/restaurants/{restaurantId}/dishes")
    public Dish createDishForRestaurant(
            @PathVariable UUID restaurantId,
            @RequestBody CreateDishRequest request
    ) {
        return restaurantService.createDishForRestaurant(
                restaurantId,
                request.name(),
                request.description(),
                request.price(),
                request.ingredients()
        );
    }

    @GetMapping("/restaurants/{restaurantId}/dishes")
    public List<Dish> getAllDishesForRestaurant(@PathVariable UUID restaurantId) {
        return restaurantService.getALlDishesForRestaurant(restaurantId);
    }

    @GetMapping("/dishes/{dishId}")
    public Dish getDish(@PathVariable UUID dishId) {
        return restaurantService.getDish(dishId);
    }

    @PutMapping("/dishes/{dishId}")
    public void updateDish(
            @PathVariable UUID dishId,
            @RequestBody UpdateDishRequest request
    ) {
        Dish dish = new Dish(
                dishId,
                request.restaurantId(),
                request.name(),
                request.description(),
                request.price(),
                request.ingredients()
        );

        restaurantService.updateDish(dish);
    }

    @DeleteMapping("/dishes/{dishId}")
    public void deleteDish(@PathVariable UUID dishId) {
        restaurantService.deleteDish(dishId);
    }

    // -------------------------
    // GroupOrders
    // -------------------------

    @PostMapping("/group-orders")
    public GroupOrder createGroupOrder(@RequestBody CreateGroupOrderRequest request) {
        return groupOrderService.createGroupOrder(
                request.restaurantId(),
                request.creatorUserId(),
                request.expiresAt()
        );
    }

    @GetMapping("/group-orders")
    public List<GroupOrder> getAllGroupOrders() {
        return groupOrderService.getAllGroupOrders();
    }

    @GetMapping("/group-orders/{groupOrderId}")
    public GroupOrder getGroupOrderById(@PathVariable UUID groupOrderId) {
        return groupOrderService.getGroupOrderById(groupOrderId);
    }

    @DeleteMapping("/group-orders/{groupOrderId}")
    public void deleteGroupOrder(@PathVariable UUID groupOrderId) {
        groupOrderService.deleteGroupOrder(groupOrderId);
    }

    // -------------------------
    // OrderEntries über GroupOrderService
    // -------------------------

    @PostMapping("/group-orders/{groupOrderId}/order-entries")
    public OrderEntry createOrderEntryForGroupOrder(
            @PathVariable UUID groupOrderId,
            @RequestBody CreateOrderEntryRequest request
    ) {
        return groupOrderService.createOrderEntryForGroupOrder(
                groupOrderId,
                request.userId(),
                request.dishId(),
                request.quantity()
        );
    }

    @GetMapping("/group-orders/{groupOrderId}/order-entries")
    public List<OrderEntry> getAllOrderEntriesForGroupOrder(@PathVariable UUID groupOrderId) {
        return groupOrderService.getAllOrderEntriesForGroupOrder(groupOrderId);
    }

    @GetMapping("/order-entries/{orderEntryId}")
    public OrderEntry getOrderEntry(@PathVariable UUID orderEntryId) {
        return groupOrderService.getOrderEntry(orderEntryId);
    }

    @PutMapping("/group-orders/{groupOrderId}/order-entries/{orderEntryId}")
    public void updateOrderEntry(
            @PathVariable UUID groupOrderId,
            @PathVariable UUID orderEntryId,
            @RequestBody UpdateOrderEntryRequest request
    ) {
        groupOrderService.updateOrderEntry(
                groupOrderId,
                orderEntryId,
                request.quantity()
        );
    }

    @DeleteMapping("/group-orders/{groupOrderId}/order-entries/{orderEntryId}")
    public void deleteOrderEntry(
            @PathVariable UUID groupOrderId,
            @PathVariable UUID orderEntryId
    ) {
        groupOrderService.deleteOrderEntry(groupOrderId, orderEntryId);
    }

    // -------------------------
    // Request-Klassen
    // -------------------------

    public record CreateUserRequest(
            String name,
            String email,
            String address
    ) {
    }

    public record UpdateUserRequest(
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

    public record UpdateRestaurantRequest(
            String name,
            String address,
            Double minOrderValue
    ) {
    }

    public record CreateDishRequest(
            String name,
            String description,
            long price,
            ArrayList<String> ingredients
    ) {
    }

    public record UpdateDishRequest(
            UUID restaurantId,
            String name,
            String description,
            long price,
            ArrayList<String> ingredients
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
}