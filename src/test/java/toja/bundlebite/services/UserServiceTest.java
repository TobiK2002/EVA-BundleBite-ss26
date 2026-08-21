package toja.bundlebite.services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.User;
import Core.Models.exceptions.AddressException;
import Core.Models.exceptions.UserException;
import Core.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            String name = "Marina Mustermann";
            String email = "marina.mustermann@gmx.de";
            String address = "Beispielstraße 24 04109 Leipzig";

            // Act
            User user = userService.createUser(name, email, address);

            // Assert
            assertNotNull(user);
            assertEquals(name, user.getName());
            assertEquals(email, user.getEmail());
            assertEquals(address, user.getAddress().toString());
            assertEquals("04109", user.getAddress().getPostalCode());
            assertEquals("Leipzig", user.getAddress().getCity());
        }

        @Test
        @DisplayName("Should throw exception for invalid Name")
        void shouldThrowExceptionForInvalidName() {
            // Arrange
            String name = "Albert";
            String email = "Albert.mustermann@gmx.de";
            String address = "Beispielstraße 24 04109 Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.createUser(name, email, address)
            );
            assertEquals("Invalid name", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid email format")
        void shouldThrowExceptionForInvalidEmail() {
            // Arrange
            String name = "test user";
            String invalidEmail = "invalid-email";
            String address = "Beispielstraße 24 04109 Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () ->
                            userService.createUser(
                                    name,
                                    invalidEmail,
                                    address
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
            String address = "Beispielstraße 24 04109 Leipzig";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () ->
                            userService.createUser(
                                    username,
                                    invalidEmail,
                                    address
                            )
            );
            assertEquals("Invalid email", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid address")
        void shouldThrowExceptionForInvalidAddress() {
            // Arrange
            String name = "Maike Mustermann";
            String email = "maike.mustermann@gmx.de";
            String invalidAddress = "Leipzig";

            // Act & Assert
            AddressException exception = assertThrows(
                    AddressException.class,
                    () -> userService.createUser(name, email, invalidAddress)
            );
            assertEquals("Invalid address", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for address without valid postal code")
        void shouldThrowExceptionForAddressWithoutValidPostalCode() {
            // Arrange
            String name = "Merten Mustermann";
            String email = "merten.mustermann@gmx.de";
            String invalidAddress = "Beispielstraße 24 4109 Leipzig";

            // Act & Assert
            AddressException exception = assertThrows(
                    AddressException.class,
                    () -> userService.createUser(name, email, invalidAddress)
            );
            assertEquals("Invalid postal code", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by email")
        void shouldGetUserByEmail() {
            // Act
            User foundUser = userService.getUserByEmail(testUser.getEmail());

            // Assert
            assertNotNull(foundUser);
            assertEquals(testUser.getName(), foundUser.getName());
            assertEquals(testUser.getEmail(), foundUser.getEmail());
            assertEquals(testUser.getAddress(), foundUser.getAddress());
        }

        @Test
        @DisplayName("Should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            // Arrange
            String unknownEmail = "unknown.user@gmx.de";

            // Act & Assert
            UserException exception = assertThrows(
                    UserException.class,
                    () -> userService.getUserByEmail(unknownEmail)
            );
            assertEquals("User does not exist", exception.getMessage());
        }

        @Test
        @DisplayName("Should return a copy of the user")
        void shouldReturnCopyOfUser() {
            // Act
            User foundUser = userService.getUserByEmail(testUser.getEmail());

            // Assert
            assertNotSame(testUser, foundUser);
            assertEquals(testUser.getEmail(), foundUser.getEmail());
        }


        @Nested
        @DisplayName("Update User Tests")
        class UpdateUserTests {

            @Test
            @DisplayName("Should update existing user")
            void shouldUpdateExistingUser() {
                // Arrange
                 userService.createUser(
                         "Erika Musterfrau",
                         "erika.musterfrau@gmx.de",
                         "Neue Straße 12 04109 Leipzig"
                 );
                 User updatedUser = new User(
                        "Erika Muster",
                        "erika.musterfrau@gmx.de",
                        "Neue Straße 12 04109 Leipzig"
                );


                // Act
                userService.updateUser(updatedUser);

                // Assert
                User foundUser = userService.getUserByEmail(updatedUser.getEmail());
                assertEquals(updatedUser.getName(), foundUser.getName());
                assertEquals(updatedUser.getEmail(), foundUser.getEmail());
                assertEquals(updatedUser.getAddress(), foundUser.getAddress());
            }

            @Test
            @DisplayName("Should throw exception when updating non existing user")
            void shouldThrowExceptionWhenUpdatingNonExistingUser() {
                // Arrange
                User unknownUser = new User(
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
            @DisplayName("Should throw exception when updating user with invalid name")
            void shouldThrowExceptionWhenUpdatingUserWithInvalidName() {
                // Arrange
                userService.createUser(
                        "Marta Musterfrau",
                        "marta.musterfrau@gmx.de",
                        "Neue Straße 12 04109 Leipzig"
                );
                User updatedUser = new User(
                        "Marta",
                        "marta.musterfrau@gmx.de",
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
                String invalidAddress = "Leipzig";

                // Act & Assert
                AddressException exception = assertThrows(
                        AddressException.class,
                        () -> {
                            User updatedUser = new User(
                                    "Erika Musterfrau",
                                    "erika.musterfrau@gmx.de",
                                    invalidAddress
                            );

                            userService.updateUser(updatedUser);
                        }
                );
                assertEquals("Invalid address", exception.getMessage());
            }
        }

        @Nested
        @DisplayName("Delete User Tests")
        class DeleteUserTests {

            @Test
            @DisplayName("Should delete existing user")
            void shouldDeleteExistingUser() {
                // Act
                userService.deleteUser(testUser.getEmail());

                // Assert
                UserException exception = assertThrows(
                        UserException.class,
                        () -> userService.getUserByEmail(testUser.getEmail())
                );
                assertEquals("User does not exist", exception.getMessage());
            }

            @Test
            @DisplayName("Should throw exception when deleting non existing user")
            void shouldThrowExceptionWhenDeletingNonExistingUser() {
                // Arrange
                String unknownEmail = "unknown.user@gmx.de";

                // Act & Assert
                UserException exception = assertThrows(
                        UserException.class,
                        () -> userService.deleteUser(unknownEmail)
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
                userService.deleteUser(testUser.getEmail());

                // Assert
                User foundUser = userService.getUserByEmail(anotherUser.getEmail());
                assertNotNull(foundUser);
                assertEquals(anotherUser.getEmail(), foundUser.getEmail());
            }
        }
    }
}
