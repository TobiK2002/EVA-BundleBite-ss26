package toja.bundlebite.services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.Dish;
import Core.Models.exceptions.DishException;
import Core.Services.DishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class DishServiceTest {
    private DishService dishService;
    private UUID restaurantId;
    private Dish testDish;

    @BeforeEach
    void setUp() {
        dishService = new DishService();
        restaurantId = UUID.randomUUID();
        testDish = createTestDish();
    }

    @Test
    @DisplayName("Should create valid dish")
    void shouldCreateValidDish() {
        // Arrange
        UUID newRestaurantId = UUID.randomUUID();
        ArrayList<String> ingredients = new ArrayList<>(List.of("Pasta", "Tomaten"));

        // Act
        Dish dish = dishService.createDish(
                newRestaurantId,
                "Pasta Napoli",
                "Pasta mit Tomatensauce",
                7.99,
                ingredients
        );

        // Assert
        assertNotNull(dish);
        assertNotNull(dish.getId());
        assertEquals(newRestaurantId, dish.getRestaurantId());
        assertEquals("Pasta Napoli", dish.getName());
        assertEquals("Pasta mit Tomatensauce", dish.getDescription());
        assertEquals(7.99, dish.getPrice());
        assertEquals(ingredients, dish.getIngredients());
    }

    @Test
    @DisplayName("Should throw exception for invalid dish name")
    void shouldThrowExceptionForInvalidDishName() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.createDish(
                        restaurantId,
                        "",
                        "Klassische Pizza",
                        8.99,
                        new ArrayList<>(List.of("Teig", "Tomaten"))
                )
        );
        assertEquals("Invalid dish name", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid dish description")
    void shouldThrowExceptionForInvalidDishDescription() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.createDish(
                        restaurantId,
                        "Pizza Margherita",
                        "",
                        8.99,
                        new ArrayList<>(List.of("Teig", "Tomaten"))
                )
        );
        assertEquals("Invalid dish description", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid dish price")
    void shouldThrowExceptionForInvalidDishPrice() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.createDish(
                        restaurantId,
                        "Pizza Margherita",
                        "Klassische Pizza",
                        -1.0,
                        new ArrayList<>(List.of("Teig", "Tomaten"))
                )
        );
        assertEquals("Invalid dish price", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for empty ingredients")
    void shouldThrowExceptionForEmptyIngredients() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.createDish(
                        restaurantId,
                        "Pizza Margherita",
                        "Klassische Pizza",
                        8.99,
                        new ArrayList<>()
                )
        );
        assertEquals("Dish requires at least one ingredient", exception.getMessage());
    }

    @Test
    @DisplayName("Should get dish by id")
    void shouldGetDishById() {
        // Act
        Dish foundDish = dishService.getDishByID(testDish.getId());

        // Assert
        assertNotNull(foundDish);
        assertNotSame(testDish, foundDish);
        assertEquals(testDish.getId(), foundDish.getId());
        assertEquals(testDish.getRestaurantId(), foundDish.getRestaurantId());
        assertEquals(testDish.getName(), foundDish.getName());
        assertEquals(testDish.getDescription(), foundDish.getDescription());
        assertEquals(testDish.getPrice(), foundDish.getPrice());
        assertEquals(testDish.getIngredients(), foundDish.getIngredients());
    }

    @Test
    @DisplayName("Should throw exception when getting non existing dish")
    void shouldThrowExceptionWhenGettingNonExistingDish() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.getDishByID(UUID.randomUUID())
        );
        assertEquals("Referenced Dish does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when getting dish with null id")
    void shouldThrowExceptionWhenGettingDishWithNullId() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.getDishByID(null)
        );
        assertEquals("Referenced Dish does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should get all dishes")
    void shouldGetAllDishes() {
        // Arrange
        Dish pasta = dishService.createDish(
                restaurantId,
                "Pasta Napoli",
                "Pasta mit Tomatensauce",
                7.99,
                new ArrayList<>(List.of("Pasta", "Tomaten"))
        );

        // Act
        List<Dish> dishes = dishService.getAllDishes();

        // Assert
        assertEquals(2, dishes.size());
        assertTrue(dishes.stream().anyMatch(dish -> dish.getId().equals(testDish.getId())));
        assertTrue(dishes.stream().anyMatch(dish -> dish.getId().equals(pasta.getId())));
    }

    @Test
    @DisplayName("Should return copies of all dishes")
    void shouldReturnCopiesOfAllDishes() {
        // Act
        List<Dish> dishes = dishService.getAllDishes();

        // Assert
        assertEquals(1, dishes.size());
        assertNotSame(testDish, dishes.get(0));
        assertEquals(testDish.getId(), dishes.get(0).getId());
    }

    @Test
    @DisplayName("Should update existing dish")
    void shouldUpdateExistingDish() {
        // Arrange
        Dish updatedDish = new Dish(
                testDish.getId(),
                testDish.getRestaurantId(),
                "Pizza Funghi",
                "Pizza mit Champignons",
                9.99,
                new ArrayList<>(List.of("Teig", "Tomaten", "Champignons"))
        );

        // Act
        dishService.updateDish(updatedDish);

        // Assert
        Dish foundDish = dishService.getDishByID(testDish.getId());
        assertEquals(updatedDish.getId(), foundDish.getId());
        assertEquals(updatedDish.getRestaurantId(), foundDish.getRestaurantId());
        assertEquals(updatedDish.getName(), foundDish.getName());
        assertEquals(updatedDish.getDescription(), foundDish.getDescription());
        assertEquals(updatedDish.getPrice(), foundDish.getPrice());
        assertEquals(updatedDish.getIngredients(), foundDish.getIngredients());
    }

    @Test
    @DisplayName("Should throw exception when updating dish with invalid data")
    void shouldThrowExceptionWhenUpdatingDishWithInvalidData() {
        // Arrange
        Dish updatedDish = new Dish(
                testDish.getId(),
                testDish.getRestaurantId(),
                "",
                "Pizza mit Champignons",
                9.99,
                new ArrayList<>(List.of("Teig", "Tomaten", "Champignons"))
        );

        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.updateDish(updatedDish)
        );
        assertEquals("Invalid dish name", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete existing dish")
    void shouldDeleteExistingDish() {
        // Act
        dishService.deleteDish(testDish.getId());

        // Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.getDishByID(testDish.getId())
        );
        assertEquals("Referenced Dish does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when deleting non existing dish")
    void shouldThrowExceptionWhenDeletingNonExistingDish() {
        // Act & Assert
        DishException exception = assertThrows(
                DishException.class,
                () -> dishService.deleteDish(UUID.randomUUID())
        );
        assertEquals("Referenced Dish does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete all dishes")
    void shouldDeleteAllDishes() {
        // Arrange
        dishService.createDish(
                restaurantId,
                "Pasta Napoli",
                "Pasta mit Tomatensauce",
                7.99,
                new ArrayList<>(List.of("Pasta", "Tomaten"))
        );

        // Act
        dishService.deleteAllDishes();

        // Assert
        assertTrue(dishService.getAllDishes().isEmpty());
    }

    @Test
    @DisplayName("Should not throw exception when deleting all dishes from empty service")
    void shouldNotThrowExceptionWhenDeletingAllDishesFromEmptyService() {
        // Arrange
        dishService.deleteAllDishes();

        // Act & Assert
        assertDoesNotThrow(() -> dishService.deleteAllDishes());
        assertTrue(dishService.getAllDishes().isEmpty());
    }

    private Dish createTestDish() {
        return dishService.createDish(
                restaurantId,
                "Pizza Margherita",
                "Klassische Pizza mit Tomaten und Kaese",
                8.99,
                new ArrayList<>(List.of("Teig", "Tomaten", "Kaese"))
        );
    }
}
