package toja.bundlebite.services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.Dish;
import Core.Models.GroupOrder;
import Core.Models.OrderEntry;
import Core.Models.Restaurant;
import Core.Models.User;
import Core.Models.exceptions.DishException;
import Core.Models.exceptions.GroupOrderException;
import Core.Models.exceptions.OrderEntryException;
import Core.Models.exceptions.RestaurantException;
import Core.Models.exceptions.UserException;
import Core.Services.DishService;
import Core.Services.GroupOrderService;
import Core.Services.OrderEntryService;
import Core.Services.RestaurantService;
import Core.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class GroupOrderServiceTest {

    private GroupOrderService groupOrderService;
    private RestaurantService restaurantService;
    private OrderEntryService orderEntryService;
    private User testUser;
    private Restaurant testRestaurant;
    private Dish testDish;
    private GroupOrder testGroupOrder;

    @BeforeEach
    void setUp() {
        DishService dishService = new DishService();
        restaurantService = new RestaurantService(dishService);
        UserService userService = new UserService();
        orderEntryService = new OrderEntryService();
        groupOrderService = new GroupOrderService(
                restaurantService,
                userService,
                orderEntryService
        );

        testUser = userService.createUser(
                "Max Mustermann",
                "max.mustermann@gmx.de",
                "Beispielstrasse 24 04109 Leipzig"
        );

        testRestaurant = restaurantService.createRestaurant(
                "Pizza Roma",
                "Restaurantstrasse 5 04109 Leipzig",
                15.0
        );

        testDish = createTestDish();
        testGroupOrder = groupOrderService.createGroupOrder(
                testRestaurant.getId(),
                testUser.getEmail(),
                60
        );
    }

    @Test
    @DisplayName("Should create valid group order")
    void shouldCreateValidGroupOrder() {
        // Act
        GroupOrder groupOrder = groupOrderService.createGroupOrder(
                testRestaurant.getId(),
                testUser.getEmail(),
                120
        );

        // Assert
        assertNotNull(groupOrder);
        assertNotNull(groupOrder.getId());
        assertEquals(testRestaurant.getId(), groupOrder.getRestaurantId());
        assertEquals(testUser.getEmail(), groupOrder.getCreatorUserEmail());
        assertEquals(120, groupOrder.getExpiresAt());
        assertTrue(groupOrder.getAllOrderEntryIds().isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when creating group order for non existing restaurant")
    void shouldThrowExceptionWhenCreatingGroupOrderForNonExistingRestaurant() {
        // Act & Assert
        RestaurantException exception = assertThrows(
                RestaurantException.class,
                () -> groupOrderService.createGroupOrder(
                        UUID.randomUUID(),
                        testUser.getEmail(),
                        60
                )
        );
        assertEquals("Restaurant does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating group order for non existing user")
    void shouldThrowExceptionWhenCreatingGroupOrderForNonExistingUser() {
        // Act & Assert
        UserException exception = assertThrows(
                UserException.class,
                () -> groupOrderService.createGroupOrder(
                        testRestaurant.getId(),
                        UUID.randomUUID().toString(),
                        60
                )
        );
        assertEquals("User does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid expiration time")
    void shouldThrowExceptionForInvalidExpirationTime() {
        // Act & Assert
        GroupOrderException exception = assertThrows(
                GroupOrderException.class,
                () -> groupOrderService.createGroupOrder(
                        testRestaurant.getId(),
                        testUser.getEmail(),
                        0
                )
        );
        assertEquals("The expiration time is invalid", exception.getMessage());
    }

    @Test
    @DisplayName("Should get group order by id")
    void shouldGetGroupOrderById() {
        // Act
        GroupOrder foundGroupOrder = groupOrderService.getGroupOrderById(testGroupOrder.getId());

        // Assert
        assertNotNull(foundGroupOrder);
        assertEquals(testGroupOrder.getId(), foundGroupOrder.getId());
        assertEquals(testGroupOrder.getRestaurantId(), foundGroupOrder.getRestaurantId());
        assertEquals(testGroupOrder.getCreatorUserEmail(), foundGroupOrder.getCreatorUserEmail());
        assertEquals(testGroupOrder.getExpiresAt(), foundGroupOrder.getExpiresAt());
    }

    @Test
    @DisplayName("Should throw exception when group order does not exist")
    void shouldThrowExceptionWhenGroupOrderDoesNotExist() {
        // Act & Assert
        GroupOrderException exception = assertThrows(
                GroupOrderException.class,
                () -> groupOrderService.getGroupOrderById(UUID.randomUUID())
        );
        assertEquals("Group Order does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should return a copy of the group order")
    void shouldReturnCopyOfGroupOrder() {
        // Act
        GroupOrder foundGroupOrder = groupOrderService.getGroupOrderById(testGroupOrder.getId());

        // Assert
        assertNotSame(testGroupOrder, foundGroupOrder);
        assertEquals(testGroupOrder.getId(), foundGroupOrder.getId());
    }

    @Test
    @DisplayName("Should get all group orders")
    void shouldGetAllGroupOrders() {
        // Arrange
        GroupOrder anotherGroupOrder = groupOrderService.createGroupOrder(
                testRestaurant.getId(),
                testUser.getEmail(),
                120
        );

        // Act
        List<GroupOrder> groupOrders = groupOrderService.getAllGroupOrders();

        // Assert
        assertEquals(2, groupOrders.size());
        assertTrue(
                groupOrders.stream()
                        .anyMatch(groupOrder ->
                                groupOrder.getId().equals(testGroupOrder.getId())
                        )
        );
        assertTrue(
                groupOrders.stream()
                        .anyMatch(groupOrder ->
                                groupOrder.getId().equals(anotherGroupOrder.getId())
                        )
        );
    }

    @Test
    @DisplayName("Should return empty list when no group orders exist")
    void shouldReturnEmptyListWhenNoGroupOrdersExist() {
        // Arrange
        groupOrderService.deleteAllGroupOrders();

        // Act
        List<GroupOrder> groupOrders = groupOrderService.getAllGroupOrders();

        // Assert
        assertNotNull(groupOrders);
        assertTrue(groupOrders.isEmpty());
    }

    @Test
    @DisplayName("Should return copies of all group orders")
    void shouldReturnCopiesOfAllGroupOrders() {
        // Act
        List<GroupOrder> groupOrders = groupOrderService.getAllGroupOrders();

        // Assert
        assertEquals(1, groupOrders.size());
        assertNotSame(testGroupOrder, groupOrders.get(0));
        assertEquals(testGroupOrder.getId(), groupOrders.get(0).getId());
    }

    @Test
    @DisplayName("Should delete existing group order")
    void shouldDeleteExistingGroupOrder() {
        // Act
        groupOrderService.deleteGroupOrder(testGroupOrder.getId());

        // Assert
        GroupOrderException exception = assertThrows(
                GroupOrderException.class,
                () -> groupOrderService.getGroupOrderById(testGroupOrder.getId())
        );
        assertEquals("Group Order does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when deleting non existing group order")
    void shouldThrowExceptionWhenDeletingNonExistingGroupOrder() {
        // Act & Assert
        GroupOrderException exception = assertThrows(
                GroupOrderException.class,
                () -> groupOrderService.deleteGroupOrder(UUID.randomUUID())
        );
        assertEquals("Group Order does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should not delete another group order")
    void shouldNotDeleteAnotherGroupOrder() {
        // Arrange
        GroupOrder anotherGroupOrder = groupOrderService.createGroupOrder(
                testRestaurant.getId(),
                testUser.getEmail(),
                120
        );

        // Act
        groupOrderService.deleteGroupOrder(testGroupOrder.getId());

        // Assert
        GroupOrder foundGroupOrder = groupOrderService.getGroupOrderById(anotherGroupOrder.getId());
        assertNotNull(foundGroupOrder);
        assertEquals(anotherGroupOrder.getId(), foundGroupOrder.getId());
    }

    @Test
    @DisplayName("Should delete all group orders")
    void shouldDeleteAllGroupOrders() {
        // Arrange
        groupOrderService.createGroupOrder(
                testRestaurant.getId(),
                testUser.getEmail(),
                120
        );

        // Act
        groupOrderService.deleteAllGroupOrders();

        // Assert
        assertTrue(groupOrderService.getAllGroupOrders().isEmpty());
    }

    @Test
    @DisplayName("Should not throw exception when deleting all group orders from empty service")
    void shouldNotThrowExceptionWhenDeletingAllGroupOrdersFromEmptyService() {
        // Arrange
        groupOrderService.deleteAllGroupOrders();

        // Act & Assert
        assertDoesNotThrow(() -> groupOrderService.deleteAllGroupOrders());
        assertTrue(groupOrderService.getAllGroupOrders().isEmpty());
    }

    @Test
    @DisplayName("Should create order entry for group order")
    void shouldCreateOrderEntryForGroupOrder() {
        // Act
        OrderEntry orderEntry = groupOrderService.createOrderEntryForGroupOrder(
                testGroupOrder.getId(),
                testUser.getEmail(),
                testDish.getId(),
                2
        );

        // Assert
        assertNotNull(orderEntry);
        assertNotNull(orderEntry.getId());
        assertEquals(testUser.getEmail(), orderEntry.getUserEmail());
        assertEquals(testDish.getId(), orderEntry.getDishId());
        assertEquals(2, orderEntry.getQuantity());
        assertEquals(testDish.getName(), orderEntry.getSnapshotDishName());
        assertEquals(testDish.getPrice(), orderEntry.getSnapshotDishPrice());
        assertEquals(testDish.getPrice() * 2, orderEntry.getSumPrice());

        GroupOrder groupOrder = groupOrderService.getGroupOrderById(testGroupOrder.getId());
        assertTrue(groupOrder.getAllOrderEntryIds().contains(orderEntry.getId()));
    }

    @Test
    @DisplayName("Should throw exception when creating order entry for non existing group order")
    void shouldThrowExceptionWhenCreatingOrderEntryForNonExistingGroupOrder() {
        // Act & Assert
        GroupOrderException exception = assertThrows(
                GroupOrderException.class,
                () -> groupOrderService.createOrderEntryForGroupOrder(
                        UUID.randomUUID(),
                        testUser.getEmail(),
                        testDish.getId(),
                        2
                )
        );
        assertEquals("Group Order does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating order entry with non existing user")
    void shouldThrowExceptionWhenCreatingOrderEntryWithNonExistingUser() {
        // Act & Assert
        UserException exception = assertThrows(
                UserException.class,
                () -> groupOrderService.createOrderEntryForGroupOrder(
                        testGroupOrder.getId(),
                        UUID.randomUUID().toString(),
                        testDish.getId(),
                        2
                )
        );
        assertEquals("User does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating order entry with non existing dish")
    void shouldThrowExceptionWhenCreatingOrderEntryWithNonExistingDish() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> groupOrderService.createOrderEntryForGroupOrder(
                        testGroupOrder.getId(),
                        testUser.getEmail(),
                        UUID.randomUUID(),
                        2
                )
        );
        assertEquals("Referenced Dish does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating order entry with dish from another restaurant")
    void shouldThrowExceptionWhenCreatingOrderEntryWithDishFromAnotherRestaurant() {
        // Arrange
        Restaurant anotherRestaurant = restaurantService.createRestaurant(
                "Burger Haus",
                "Neue Strasse 12 04109 Leipzig",
                20.0
        );
        Dish anotherDish = restaurantService.createDishForRestaurant(
                anotherRestaurant.getId(),
                "Burger",
                "Burger mit Kaese",
                9.99,
                new ArrayList<>(List.of("Brot", "Patty", "Kaese"))
        );

        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> groupOrderService.createOrderEntryForGroupOrder(
                        testGroupOrder.getId(),
                        testUser.getEmail(),
                        anotherDish.getId(),
                        2
                )
        );
        assertEquals("Referenced Dish does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating order entry with negative quantity")
    void shouldThrowExceptionWhenCreatingOrderEntryWithNegativeQuantity() {
        // Act & Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> groupOrderService.createOrderEntryForGroupOrder(
                        testGroupOrder.getId(),
                        testUser.getEmail(),
                        testDish.getId(),
                        -1
                )
        );
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should get order entry by id")
    void shouldGetOrderEntryById() {
        // Arrange
        OrderEntry orderEntry = createTestOrderEntry();

        // Act
        OrderEntry foundOrderEntry = groupOrderService.getOrderEntry(orderEntry.getId());

        // Assert
        assertNotNull(foundOrderEntry);
        assertNotSame(orderEntry, foundOrderEntry);
        assertEquals(orderEntry.getId(), foundOrderEntry.getId());
        assertEquals(orderEntry.getUserEmail(), foundOrderEntry.getUserEmail());
        assertEquals(orderEntry.getDishId(), foundOrderEntry.getDishId());
    }

    @Test
    @DisplayName("Should get all order entries for group order")
    void shouldGetAllOrderEntriesForGroupOrder() {
        // Arrange
        OrderEntry pizza = createTestOrderEntry();
        OrderEntry pasta = groupOrderService.createOrderEntryForGroupOrder(
                testGroupOrder.getId(),
                testUser.getEmail(),
                restaurantService.createDishForRestaurant(
                        testRestaurant.getId(),
                        "Pasta Napoli",
                        "Pasta mit Tomatensauce",
                        7.99,
                        new ArrayList<>(List.of("Pasta", "Tomaten"))
                ).getId(),
                1
        );

        // Act
        List<OrderEntry> orderEntries = groupOrderService.getAllOrderEntriesForGroupOrder(
                testGroupOrder.getId()
        );

        // Assert
        assertEquals(2, orderEntries.size());
        assertTrue(orderEntries.stream().anyMatch(orderEntry -> orderEntry.getId().equals(pizza.getId())));
        assertTrue(orderEntries.stream().anyMatch(orderEntry -> orderEntry.getId().equals(pasta.getId())));
    }

    @Test
    @DisplayName("Should update order entry that belongs to group order")
    void shouldUpdateOrderEntryThatBelongsToGroupOrder() {
        // Arrange
        OrderEntry orderEntry = createTestOrderEntry();

        // Act
        groupOrderService.updateOrderEntry(testGroupOrder.getId(), orderEntry.getId(), 5);

        // Assert
        OrderEntry foundOrderEntry = groupOrderService.getOrderEntry(orderEntry.getId());
        assertEquals(5, foundOrderEntry.getQuantity());
        assertEquals(testDish.getPrice() * 5, foundOrderEntry.getSumPrice());
        assertEquals(testDish.getName(), foundOrderEntry.getSnapshotDishName());
        assertEquals(testDish.getPrice(), foundOrderEntry.getSnapshotDishPrice());
    }

    @Test
    @DisplayName("Should throw exception when updating order entry that is not assigned to group order")
    void shouldThrowExceptionWhenUpdatingOrderEntryThatIsNotAssignedToGroupOrder() {
        // Arrange
        OrderEntry unknownOrderEntry = orderEntryService.createOrderEntry(
                testUser.getEmail(),
                testDish.getId(),
                testDish.getName(),
                testDish.getPrice(),
                1
        );

        // Act & Assert
        GroupOrderException exception = assertThrows(
                GroupOrderException.class,
                () -> groupOrderService.updateOrderEntry(
                        testGroupOrder.getId(),
                        unknownOrderEntry.getId(),
                        3
                )
        );
        assertEquals("This Order Entry is not included in this Group-Order", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when updating order entry with negative quantity")
    void shouldThrowExceptionWhenUpdatingOrderEntryWithNegativeQuantity() {
        // Arrange
        OrderEntry orderEntry = createTestOrderEntry();

        // Act & Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> groupOrderService.updateOrderEntry(
                        testGroupOrder.getId(),
                        orderEntry.getId(),
                        -1
                )
        );
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete order entry and remove it from group order")
    void shouldDeleteOrderEntryAndRemoveItFromGroupOrder() {
        // Arrange
        OrderEntry orderEntry = createTestOrderEntry();

        // Act
        groupOrderService.deleteOrderEntry(testGroupOrder.getId(), orderEntry.getId());

        // Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> groupOrderService.getOrderEntry(orderEntry.getId())
        );
        assertEquals("Referenced OrderEntry does not exist", exception.getMessage());

        GroupOrder groupOrder = groupOrderService.getGroupOrderById(testGroupOrder.getId());
        assertFalse(groupOrder.getAllOrderEntryIds().contains(orderEntry.getId()));
    }

    @Test
    @DisplayName("Should throw exception when deleting order entry that is not assigned to group order")
    void shouldThrowExceptionWhenDeletingOrderEntryThatIsNotAssignedToGroupOrder() {
        // Arrange
        OrderEntry unknownOrderEntry = orderEntryService.createOrderEntry(
                testUser.getEmail(),
                testDish.getId(),
                testDish.getName(),
                testDish.getPrice(),
                1
        );

        // Act & Assert
        GroupOrderException exception = assertThrows(
                GroupOrderException.class,
                () -> groupOrderService.deleteOrderEntry(
                        testGroupOrder.getId(),
                        unknownOrderEntry.getId()
                )
        );
        assertEquals("This Order Entry is not included in this Group-Order", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete order entries when deleting group order")
    void shouldDeleteOrderEntriesWhenDeletingGroupOrder() {
        // Arrange
        OrderEntry orderEntry = createTestOrderEntry();

        // Act
        groupOrderService.deleteGroupOrder(testGroupOrder.getId());

        // Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> groupOrderService.getOrderEntry(orderEntry.getId())
        );
        assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete order entries when deleting all group orders")
    void shouldDeleteOrderEntriesWhenDeletingAllGroupOrders() {
        // Arrange
        OrderEntry orderEntry = createTestOrderEntry();

        // Act
        groupOrderService.deleteAllGroupOrders();

        // Assert
        assertTrue(groupOrderService.getAllGroupOrders().isEmpty());
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> groupOrderService.getOrderEntry(orderEntry.getId())
        );
        assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
    }

    private Dish createTestDish() {
        return restaurantService.createDishForRestaurant(
                testRestaurant.getId(),
                "Pizza Margherita",
                "Klassische Pizza mit Tomaten und Kaese",
                8.99,
                new ArrayList<>(List.of("Teig", "Tomaten", "Kaese"))
        );
    }

    private OrderEntry createTestOrderEntry() {
        return groupOrderService.createOrderEntryForGroupOrder(
                testGroupOrder.getId(),
                testUser.getEmail(),
                testDish.getId(),
                2
        );
    }
}
