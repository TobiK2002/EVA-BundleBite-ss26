package toja.bundlebite.services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.User;
import Core.Models.exceptions.UserException;
import Core.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

class UserServiceTest {

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        testUser = userService.createUser(
                "Max Mustermann",
                "max.mustermann@gmx.de",
                "Beispielstraße 24 04109 Leipzig"
        );
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create valid Customer")
        void shouldCreateValidUser() {
            // Arrange
            String name = "Max Mustermann";
            String email = "max.mustermann@gmx.de";
            String adress = "Beispielstraße 24 04109 Leipzig";

            // Act
            User user = userService.createUser(name, email, adress);

            // Assert
            assertNotNull(user);
            assertNotNull(user.getId());
            assertEquals(name, user.getName());
            assertEquals(email, user.getEmail());
            assertEquals(adress, user.getAdress());
        }

        @Test
        @DisplayName("Should throw exception for invalid Name")
        void shouldThrowExceptionForInvalidName() {
            // Arrange
            String name = "Albert";
            String email = "max.mustermann@gmx.de";
            String adress = "Beispielstraße 24 04109 Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.createUser(name, email, adress)
            );
            assertEquals("Invalid name", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid email format")
        void shouldThrowExceptionForInvalidEmail() {
            // Arrange
            String name = "test user";
            String invalidEmail = "invalid-email";
            String adress = "Beispielstraße 24 04109 Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () ->
                            userService.createUser(
                                    name,
                                    invalidEmail,
                                    adress
                            )
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for email with multiple @ symbols")
        void shouldThrowExceptionForEmailWithMultipleAtSymbols() {
            // Arrange
            String username = "test user";
            String invalidEmail = "test@@example.com";
            String adress = "Beispielstraße 24 04109 Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () ->
                            userService.createUser(
                                    username,
                                    invalidEmail,
                                    adress
                            )
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid address")
        void shouldThrowExceptionForInvalidAddress() {
            // Arrange
            String name = "Max Mustermann";
            String email = "max.mustermann@gmx.de";
            String invalidAdress = "Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.createUser(name, email, invalidAdress)
            );
            assertEquals("Invalid adress", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for address without valid postal code")
        void shouldThrowExceptionForAddressWithoutValidPostalCode() {
            // Arrange
            String name = "Max Mustermann";
            String email = "max.mustermann@gmx.de";
            String invalidAdress = "Beispielstraße 24 4109 Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.createUser(name, email, invalidAdress)
            );
            assertEquals("Invalid adress", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by id")
        void shouldGetUserById() {
            // Act
            User foundUser = userService.getUserById(testUser.getId());

            // Assert
            assertNotNull(foundUser);
            assertEquals(testUser.getId(), foundUser.getId());
            assertEquals(testUser.getName(), foundUser.getName());
            assertEquals(testUser.getEmail(), foundUser.getEmail());
            assertEquals(testUser.getAdress(), foundUser.getAdress());
        }

        @Test
        @DisplayName("Should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // Arrange
            UUID unknownId = UUID.randomUUID();

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.getUserById(unknownId)
            );
            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should return a copy of the user")
        void shouldReturnCopyOfUser() {
            // Act
            User foundUser = userService.getUserById(testUser.getId());

            // Assert
            assertNotSame(testUser, foundUser);
            assertEquals(testUser.getId(), foundUser.getId());
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateExistingUser() {
            // Arrange
            User updatedUser = new User(
                    testUser.getId(),
                    "Erika Musterfrau",
                    "erika.musterfrau@gmx.de",
                    "Neue Straße 12 04109 Leipzig"
            );

            // Act
            userService.updateUser(updatedUser);

            // Assert
            User foundUser = userService.getUserById(testUser.getId());
            assertEquals(updatedUser.getId(), foundUser.getId());
            assertEquals(updatedUser.getName(), foundUser.getName());
            assertEquals(updatedUser.getEmail(), foundUser.getEmail());
            assertEquals(updatedUser.getAdress(), foundUser.getAdress());
        }

        @Test
        @DisplayName("Should throw exception when updating non existing user")
        void shouldThrowExceptionWhenUpdatingNonExistingUser() {
            // Arrange
            User unknownUser = new User(
                    UUID.randomUUID(),
                    "Erika Musterfrau",
                    "erika.musterfrau@gmx.de",
                    "Neue Straße 12 04109 Leipzig"
            );

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.updateUser(unknownUser)
            );
            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating user with invalid email")
        void shouldThrowExceptionWhenUpdatingUserWithInvalidEmail() {
            // Arrange
            User updatedUser = new User(
                    testUser.getId(),
                    "Erika Musterfrau",
                    "invalid-email",
                    "Neue Straße 12 04109 Leipzig"
            );

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.updateUser(updatedUser)
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating user with invalid name")
        void shouldThrowExceptionWhenUpdatingUserWithInvalidName() {
            // Arrange
            User updatedUser = new User(
                    testUser.getId(),
                    "Erika",
                    "erika.musterfrau@gmx.de",
                    "Neue Straße 12 04109 Leipzig"
            );

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.updateUser(updatedUser)
            );
            assertEquals("Invalid name", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating user with invalid address")
        void shouldThrowExceptionWhenUpdatingUserWithInvalidAddress() {
            // Arrange
            User updatedUser = new User(
                    testUser.getId(),
                    "Erika Musterfrau",
                    "erika.musterfrau@gmx.de",
                    "Leipzig"
            );

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.updateUser(updatedUser)
            );
            assertEquals("Invalid adress", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete existing user")
        void shouldDeleteExistingUser() {
            // Act
            userService.deleteUser(testUser.getId());

            // Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.getUserById(testUser.getId())
            );
            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when deleting non existing user")
        void shouldThrowExceptionWhenDeletingNonExistingUser() {
            // Arrange
            UUID unknownId = UUID.randomUUID();

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.deleteUser(unknownId)
            );
            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should not delete another user")
        void shouldNotDeleteAnotherUser() {
            // Arrange
            User anotherUser = userService.createUser(
                    "Erika Musterfrau",
                    "erika.musterfrau@gmx.de",
                    "Neue Straße 12 04109 Leipzig"
            );

            // Act
            userService.deleteUser(testUser.getId());

            // Assert
            User foundUser = userService.getUserById(anotherUser.getId());
            assertNotNull(foundUser);
            assertEquals(anotherUser.getId(), foundUser.getId());
        }
    }
}
