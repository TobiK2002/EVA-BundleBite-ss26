package toja.bundlebite.services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.Restaurant;
import Core.Models.exceptions.AddressException;
import Core.Models.exceptions.RestaurantException;
import Core.Services.DishService;
import Core.Services.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class RestaurantServiceTest {
    private RestaurantService restaurantService;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        DishService dishService = new DishService();
        restaurantService = new RestaurantService(dishService);
        testRestaurant = restaurantService.createRestaurant(
                "Pizza Roma",
                "Beispielstraße 24 04109 Leipzig",
                15.0
        );
    }

    @Nested
    @DisplayName("Create Restaurant Tests")
    class CreateRestaurantTests {
        @Test
        @DisplayName("Should create valid restaurant")
        void shouldCreateValidRestaurant() {
            // Arrange
            String name = "Burger Haus";
            String address = "Neue Straße 12 04109 Leipzig";
            Double minOrderValue = 20.0;

            // Act
            Restaurant restaurant = restaurantService.createRestaurant(
                    name,
                    address,
                    minOrderValue
            );

            // Assert
            assertNotNull(restaurant);
            assertNotNull(restaurant.getId());
            assertEquals(name, restaurant.getName());
            assertEquals(address, restaurant.getAddress().toString());
            assertEquals("04109", restaurant.getAddress().getPostalCode());
            assertEquals("Leipzig", restaurant.getAddress().getCity());
            assertEquals(minOrderValue, restaurant.getMinOrderValue());
        }

        @Test
        @DisplayName("Should throw exception for invalid name")
        void shouldThrowExceptionForInvalidName() {
            // Arrange
            String invalidName = "";
            String address = "Beispielstraße 24 04109 Leipzig";
            Double minOrderValue = 15.0;

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.createRestaurant(
                            invalidName,
                            address,
                            minOrderValue
                    )
            );
            assertEquals("Invalid restaurant name", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null name")
        void shouldThrowExceptionForNullName() {
            // Arrange
            String name = null;
            String address = "Beispielstraße 24 04109 Leipzig";
            Double minOrderValue = 15.0;

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.createRestaurant(
                            name,
                            address,
                            minOrderValue
                    )
            );
            assertEquals("Invalid restaurant name", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid address")
        void shouldThrowExceptionForInvalidAddress() {
            // Arrange
            String name = "Pizza Roma";
            String invalidAddress = "Leipzig";
            Double minOrderValue = 15.0;

            // Act & Assert
            AddressException exception = assertThrows(
                    AddressException.class,
                    () -> restaurantService.createRestaurant(
                            name,
                            invalidAddress,
                            minOrderValue
                    )
            );
            assertEquals("Invalid address", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for address without valid postal code")
        void shouldThrowExceptionForAddressWithoutValidPostalCode() {
            // Arrange
            String name = "Pizza Roma";
            String invalidAddress = "Beispielstraße 24 4109 Leipzig";
            Double minOrderValue = 15.0;

            // Act & Assert
            AddressException exception = assertThrows(
                    AddressException.class,
                    () -> restaurantService.createRestaurant(
                            name,
                            invalidAddress,
                            minOrderValue
                    )
            );
            assertEquals("Invalid postal code", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null minimum order value")
        void shouldThrowExceptionForNullMinOrderValue() {
            // Arrange
            String name = "Pizza Roma";
            String address = "Beispielstraße 24 04109 Leipzig";
            Double minOrderValue = null;

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.createRestaurant(
                            name,
                            address,
                            minOrderValue
                    )
            );
            assertEquals("Invalid minimum order value", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for negative minimum order value")
        void shouldThrowExceptionForNegativeMinOrderValue() {
            // Arrange
            String name = "Pizza Roma";
            String address = "Beispielstraße 24 04109 Leipzig";
            Double minOrderValue = -1.0;

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.createRestaurant(
                            name,
                            address,
                            minOrderValue
                    )
            );
            assertEquals("Invalid minimum order value", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get Restaurant Tests")
    class GetRestaurantTests {
        @Test
        @DisplayName("Should get restaurant by id")
        void shouldGetRestaurantById() {
            // Act
            Restaurant foundRestaurant = restaurantService.getRestaurantById(
                    testRestaurant.getId()
            );

            // Assert
            assertNotNull(foundRestaurant);
            assertEquals(testRestaurant.getId(), foundRestaurant.getId());
            assertEquals(testRestaurant.getName(), foundRestaurant.getName());
            assertEquals(testRestaurant.getAddress(), foundRestaurant.getAddress());
            assertEquals(
                    testRestaurant.getMinOrderValue(),
                    foundRestaurant.getMinOrderValue()
            );
        }

        @Test
        @DisplayName("Should throw exception when restaurant does not exist")
        void shouldThrowExceptionWhenRestaurantDoesNotExist() {
            // Arrange
            UUID unknownId = UUID.randomUUID();

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.getRestaurantById(unknownId)
            );
            assertEquals("Restaurant does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should return a copy of the restaurant")
        void shouldReturnCopyOfRestaurant() {
            // Act
            Restaurant foundRestaurant = restaurantService.getRestaurantById(
                    testRestaurant.getId()
            );

            // Assert
            assertNotSame(testRestaurant, foundRestaurant);
            assertEquals(testRestaurant.getId(), foundRestaurant.getId());
        }
    }

    @Nested
    @DisplayName("Get All Restaurant Tests")
    class GetAllRestaurantTests {
        @Test
        @DisplayName("Should get all restaurants")
        void shouldGetAllRestaurants() {
            // Arrange
            Restaurant anotherRestaurant = restaurantService.createRestaurant(
                    "Burger Haus",
                    "Neue Straße 12 04109 Leipzig",
                    20.0
            );

            // Act
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();

            // Assert
            assertEquals(2, restaurants.size());
            assertTrue(
                    restaurants.stream()
                            .anyMatch(restaurant ->
                                    restaurant.getId().equals(testRestaurant.getId())
                            )
            );
            assertTrue(
                    restaurants.stream()
                            .anyMatch(restaurant ->
                                    restaurant.getId().equals(anotherRestaurant.getId())
                            )
            );
        }

        @Test
        @DisplayName("Should return empty list when no restaurants exist")
        void shouldReturnEmptyListWhenNoRestaurantsExist() {
            // Arrange
            restaurantService.deleteAllRestaurants();

            // Act
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();

            // Assert
            assertNotNull(restaurants);
            assertTrue(restaurants.isEmpty());
        }

        @Test
        @DisplayName("Should return copies of all restaurants")
        void shouldReturnCopiesOfAllRestaurants() {
            // Act
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();

            // Assert
            assertEquals(1, restaurants.size());
            assertNotSame(testRestaurant, restaurants.get(0));
            assertEquals(testRestaurant.getId(), restaurants.get(0).getId());
        }
    }

    @Nested
    @DisplayName("Update Restaurant Tests")
    class UpdateRestaurantTests {
        @Test
        @DisplayName("Should update existing restaurant")
        void shouldUpdateExistingRestaurant() {
            // Arrange
            Restaurant updatedRestaurant = new Restaurant(
                    testRestaurant.getId(),
                    "Pasta Milano",
                    "Neue Straße 12 04109 Leipzig",
                    25.0
            );

            // Act
            restaurantService.updateRestaurant(updatedRestaurant);

            // Assert
            Restaurant foundRestaurant = restaurantService.getRestaurantById(
                    testRestaurant.getId()
            );
            assertEquals(updatedRestaurant.getId(), foundRestaurant.getId());
            assertEquals(updatedRestaurant.getName(), foundRestaurant.getName());
            assertEquals(updatedRestaurant.getAddress(), foundRestaurant.getAddress());
            assertEquals(
                    updatedRestaurant.getMinOrderValue(),
                    foundRestaurant.getMinOrderValue()
            );
        }

        @Test
        @DisplayName("Should throw exception when updating non existing restaurant")
        void shouldThrowExceptionWhenUpdatingNonExistingRestaurant() {
            // Arrange
            Restaurant unknownRestaurant = new Restaurant(
                    UUID.randomUUID(),
                    "Pasta Milano",
                    "Neue Straße 12 04109 Leipzig",
                    25.0
            );

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.updateRestaurant(unknownRestaurant)
            );
            assertEquals("Restaurant does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating restaurant with invalid name")
        void shouldThrowExceptionWhenUpdatingRestaurantWithInvalidName() {
            // Arrange
            Restaurant updatedRestaurant = new Restaurant(
                    testRestaurant.getId(),
                    "",
                    "Neue Straße 12 04109 Leipzig",
                    25.0
            );

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.updateRestaurant(updatedRestaurant)
            );
            assertEquals("Invalid restaurant name", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating restaurant with invalid address")
        void shouldThrowExceptionWhenUpdatingRestaurantWithInvalidAddress() {
            // Arrange
            String invalidAddress = "Leipzig";

            // Act & Assert
            AddressException exception = assertThrows(
                    AddressException.class,
                    () -> {
                        Restaurant updatedRestaurant = new Restaurant(
                                testRestaurant.getId(),
                                "Pasta Milano",
                                invalidAddress,
                                25.0
                        );

                        restaurantService.updateRestaurant(updatedRestaurant);
                    }
            );
            assertEquals("Invalid address", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating restaurant with negative minimum order value")
        void shouldThrowExceptionWhenUpdatingRestaurantWithNegativeMinOrderValue() {
            // Arrange
            Restaurant updatedRestaurant = new Restaurant(
                    testRestaurant.getId(),
                    "Pasta Milano",
                    "Neue Straße 12 04109 Leipzig",
                    -1.0
            );

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.updateRestaurant(updatedRestaurant)
            );
            assertEquals("Invalid minimum order value", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete Restaurant Tests")
    class DeleteRestaurantTests {
        @Test
        @DisplayName("Should delete existing restaurant")
        void shouldDeleteExistingRestaurant() {
            // Act
            restaurantService.deleteRestaurant(testRestaurant.getId());

            // Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.getRestaurantById(testRestaurant.getId())
            );
            assertEquals("Restaurant does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when deleting non existing restaurant")
        void shouldThrowExceptionWhenDeletingNonExistingRestaurant() {
            // Arrange
            UUID unknownId = UUID.randomUUID();

            // Act & Assert
            RestaurantException exception = assertThrows(
                    RestaurantException.class,
                    () -> restaurantService.deleteRestaurant(unknownId)
            );
            assertEquals("Restaurant does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should not delete another restaurant")
        void shouldNotDeleteAnotherRestaurant() {
            // Arrange
            Restaurant anotherRestaurant = restaurantService.createRestaurant(
                    "Burger Haus",
                    "Neue Straße 12 04109 Leipzig",
                    20.0
            );

            // Act
            restaurantService.deleteRestaurant(testRestaurant.getId());

            // Assert
            Restaurant foundRestaurant = restaurantService.getRestaurantById(
                    anotherRestaurant.getId()
            );
            assertNotNull(foundRestaurant);
            assertEquals(anotherRestaurant.getId(), foundRestaurant.getId());
        }
    }

    @Nested
    @DisplayName("Delete All Restaurant Tests")
    class DeleteAllRestaurantTests {
        @Test
        @DisplayName("Should delete all restaurants")
        void shouldDeleteAllRestaurants() {
            // Arrange
            restaurantService.createRestaurant(
                    "Burger Haus",
                    "Neue Straße 12 04109 Leipzig",
                    20.0
            );

            // Act
            restaurantService.deleteAllRestaurants();

            // Assert
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();
            assertTrue(restaurants.isEmpty());
        }

        @Test
        @DisplayName("Should not throw exception when deleting all restaurants from empty service")
        void shouldNotThrowExceptionWhenDeletingAllRestaurantsFromEmptyService() {
            // Arrange
            restaurantService.deleteAllRestaurants();

            // Act & Assert
            assertDoesNotThrow(() -> restaurantService.deleteAllRestaurants());
            assertTrue(restaurantService.getAllRestaurants().isEmpty());
        }
    }
}
