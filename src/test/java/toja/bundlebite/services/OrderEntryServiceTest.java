package toja.bundlebite.services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.Dish;
import Core.Models.OrderEntry;
import Core.Models.Restaurant;
import Core.Models.User;
import Core.Models.exceptions.DishException;
import Core.Models.exceptions.OrderEntryException;
import Core.Services.DishService;
import Core.Services.OrderEntryService;
import Core.Services.RestaurantService;
import Core.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class OrderEntryServiceTest {

    private DishService dishService;
    private UserService userService;
    private RestaurantService restaurantService;
    private OrderEntryService orderEntryService;

    private User testUser;
    private Restaurant testRestaurant;
    private Dish testDish;
    private OrderEntry testOrderEntry;

    @BeforeEach
    void setUp() {
        dishService = new DishService();
        userService = new UserService();
        restaurantService = new RestaurantService(dishService);
        orderEntryService = new OrderEntryService(dishService, userService, restaurantService);

        testUser = userService.createUser(
                "Max Mustermann",
                "max.mustermann@gmx.de",
                "Beispielstraße 24 04109 Leipzig"
        );

        testRestaurant = restaurantService.createRestaurant(
                "Pizzeria Napoli",
                "Restaurantstraße 5 04109 Leipzig",
                10.0
        );

        ArrayList<String> ingredients = new ArrayList<>();
        ingredients.add("Tomate");
        ingredients.add("Mozzarella");

        testDish = restaurantService.createDishForRestaurant(
                testRestaurant.getId(),
                "Pizza Margherita",
                "Klassische Pizza mit Tomate und Mozzarella",
                800,
                ingredients
        );

        testOrderEntry = orderEntryService.createOrderEntry(testUser.getId(), testDish.getId(), 2);
    }

    @Nested
    @DisplayName("Create OrderEntry Tests")
    class CreateOrderEntryTests {

        @Test
        @DisplayName("Should create valid OrderEntry")
        void shouldCreateValidOrderEntry() {
            // Arrange
            int quantity = 3;

            // Act
            OrderEntry entry = orderEntryService.createOrderEntry(testUser.getId(), testDish.getId(), quantity);

            // Assert
            assertNotNull(entry);
            assertNotNull(entry.getId());
            assertEquals(testUser.getId(), entry.getUserId());
            assertEquals(testDish.getId(), entry.getDishId());
            assertEquals(quantity, entry.getQuantity());
            assertEquals(testDish.getName(), entry.getSnapshotDishName());
            assertEquals(testDish.getPrice(), entry.getSnapshotDishPrice());
            assertEquals(testDish.getPrice() * quantity, entry.getSumPrice());
        }

        @Test
        @DisplayName("Should throw exception when dish does not exist")
        void shouldThrowExceptionWhenDishDoesNotExist() {
            // Arrange
            UUID unknownDishId = UUID.randomUUID();

            // Act & Assert
            DishException exception = assertThrows(
                    DishException.class,
                    () -> orderEntryService.createOrderEntry(testUser.getId(), unknownDishId, 1)
            );
            assertEquals("Referenced Dish does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void shouldThrowExceptionForNegativeQuantity() {
            // Act & Assert
            OrderEntryException exception = assertThrows(
                    OrderEntryException.class,
                    () -> orderEntryService.createOrderEntry(testUser.getId(), testDish.getId(), -1)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should allow quantity of zero")
        void shouldAllowQuantityOfZero() {
            // Act
            OrderEntry entry = orderEntryService.createOrderEntry(testUser.getId(), testDish.getId(), 0);

            // Assert
            assertNotNull(entry);
            assertEquals(0, entry.getQuantity());
            assertEquals(0, entry.getSumPrice());
        }
    }

    @Nested
    @DisplayName("Get OrderEntry Tests")
    class GetOrderEntryTests {

        @Test
        @DisplayName("Should get order entry by id")
        void shouldGetOrderEntryById() {
            // Act
            OrderEntry foundEntry = orderEntryService.getOrderEntryById(testOrderEntry.getId());

            // Assert
            assertNotNull(foundEntry);
            assertEquals(testOrderEntry.getId(), foundEntry.getId());
            assertEquals(testOrderEntry.getUserId(), foundEntry.getUserId());
            assertEquals(testOrderEntry.getDishId(), foundEntry.getDishId());
            assertEquals(testOrderEntry.getQuantity(), foundEntry.getQuantity());
            assertEquals(testOrderEntry.getSumPrice(), foundEntry.getSumPrice());
        }

        @Test
        @DisplayName("Should throw exception when order entry does not exist")
        void shouldThrowExceptionWhenOrderEntryDoesNotExist() {
            // Arrange
            UUID unknownId = UUID.randomUUID();

            // Act & Assert
            OrderEntryException exception = assertThrows(
                    OrderEntryException.class,
                    () -> orderEntryService.getOrderEntryById(unknownId)
            );
            assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should return a copy of the order entry")
        void shouldReturnCopyOfOrderEntry() {
            // Act
            OrderEntry foundEntry = orderEntryService.getOrderEntryById(testOrderEntry.getId());

            // Assert
            assertNotSame(testOrderEntry, foundEntry);
            assertEquals(testOrderEntry.getId(), foundEntry.getId());
        }

        @Test
        @DisplayName("Should get all order entries")
        void shouldGetAllOrderEntries() {
            // Arrange
            orderEntryService.createOrderEntry(testUser.getId(), testDish.getId(), 1);

            // Act
            List<OrderEntry> allEntries = orderEntryService.getAllOrderEntries();

            // Assert
            assertEquals(2, allEntries.size());
        }
    }

    @Nested
    @DisplayName("Update OrderEntry Tests")
    class UpdateOrderEntryTests {

        @Test
        @DisplayName("Should update existing order entry")
        void shouldUpdateExistingOrderEntry() {
            // Arrange
            OrderEntry updatedEntry = new OrderEntry(
                    testOrderEntry.getId(),
                    testOrderEntry.getUserId(),
                    testOrderEntry.getDishId(),
                    5,
                    5 * testDish.getPrice(),
                    testDish.getName(),
                    testDish.getPrice()
            );

            // Act
            orderEntryService.updateOrderEntry(updatedEntry);

            // Assert
            OrderEntry foundEntry = orderEntryService.getOrderEntryById(testOrderEntry.getId());
            assertEquals(updatedEntry.getQuantity(), foundEntry.getQuantity());
            assertEquals(updatedEntry.getSumPrice(), foundEntry.getSumPrice());
        }

        @Test
        @DisplayName("Should throw exception when updating non existing order entry")
        void shouldThrowExceptionWhenUpdatingNonExistingOrderEntry() {
            // Arrange
            OrderEntry unknownEntry = new OrderEntry(
                    UUID.randomUUID(),
                    testUser.getId(),
                    testDish.getId(),
                    1,
                    testDish.getPrice(),
                    testDish.getName(),
                    testDish.getPrice()
            );

            // Act & Assert
            OrderEntryException exception = assertThrows(
                    OrderEntryException.class,
                    () -> orderEntryService.updateOrderEntry(unknownEntry)
            );
            assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating order entry with negative quantity")
        void shouldThrowExceptionWhenUpdatingOrderEntryWithNegativeQuantity() {
            // Arrange
            OrderEntry invalidEntry = new OrderEntry(
                    testOrderEntry.getId(),
                    testOrderEntry.getUserId(),
                    testOrderEntry.getDishId(),
                    -2,
                    testDish.getPrice(),
                    testDish.getName(),
                    testDish.getPrice()
            );

            // Act & Assert
            OrderEntryException exception = assertThrows(
                    OrderEntryException.class,
                    () -> orderEntryService.updateOrderEntry(invalidEntry)
            );
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating order entry with negative sum price")
        void shouldThrowExceptionWhenUpdatingOrderEntryWithNegativeSumPrice() {
            // Arrange
            OrderEntry invalidEntry = new OrderEntry(
                    testOrderEntry.getId(),
                    testOrderEntry.getUserId(),
                    testOrderEntry.getDishId(),
                    2,
                    -100,
                    testDish.getName(),
                    testDish.getPrice()
            );

            // Act & Assert
            OrderEntryException exception = assertThrows(
                    OrderEntryException.class,
                    () -> orderEntryService.updateOrderEntry(invalidEntry)
            );
            assertEquals("Price must be positive", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete OrderEntry Tests")
    class DeleteOrderEntryTests {

        @Test
        @DisplayName("Should delete existing order entry")
        void shouldDeleteExistingOrderEntry() {
            // Act
            orderEntryService.deleteOrderEntry(testOrderEntry.getId());

            // Assert
            OrderEntryException exception = assertThrows(
                    OrderEntryException.class,
                    () -> orderEntryService.getOrderEntryById(testOrderEntry.getId())
            );
            assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when deleting non existing order entry")
        void shouldThrowExceptionWhenDeletingNonExistingOrderEntry() {
            // Arrange
            UUID unknownId = UUID.randomUUID();

            // Act & Assert
            OrderEntryException exception = assertThrows(
                    OrderEntryException.class,
                    () -> orderEntryService.deleteOrderEntry(unknownId)
            );
            assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should not delete another order entry")
        void shouldNotDeleteAnotherOrderEntry() {
            // Arrange
            OrderEntry anotherEntry = orderEntryService.createOrderEntry(testUser.getId(), testDish.getId(), 1);

            // Act
            orderEntryService.deleteOrderEntry(testOrderEntry.getId());

            // Assert
            OrderEntry foundEntry = orderEntryService.getOrderEntryById(anotherEntry.getId());
            assertNotNull(foundEntry);
            assertEquals(anotherEntry.getId(), foundEntry.getId());
        }

        @Test
        @DisplayName("Should delete all order entries")
        void shouldDeleteAllOrderEntries() {
            // Arrange
            orderEntryService.createOrderEntry(testUser.getId(), testDish.getId(), 1);

            // Act
            orderEntryService.deleteAllOrderEntries();

            // Assert
            List<OrderEntry> allEntries = orderEntryService.getAllOrderEntries();
            assertTrue(allEntries.isEmpty());
        }
    }
}
