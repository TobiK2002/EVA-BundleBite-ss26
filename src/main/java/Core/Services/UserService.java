package Core.Services;

import Core.Models.User;
import Core.Models.exceptions.UserException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final Map<UUID, User> UsersById = new ConcurrentHashMap<>();

    public User createUser(String name, String email, String adress){
        UUID id= UUID.randomUUID();
        User user = new User(id, name, email, adress);
        saveUser(user);
        return user;
    }

    public User getUserById(UUID id) throws UserException {
        User user = UsersById.get(id);
        if (user == null) {
            throw UserException.userDoesNotExist();

        }
        return clone(user);
    }

    public void updateUser(User updatedUser) throws UserException {
        validateUpdatedUser(updatedUser);
        saveUser(updatedUser);
    }
    public void deleteUser(UUID id) throws UserException {
        User user = UsersById.remove(id);
        if (user == null) {
            throw UserException.userDoesNotExist();
        }
    }

    private void validateUser(User user){
        if (
                !user.getEmail().contains("@") ||
                        user.getEmail().indexOf("@") !=
                                user.getEmail().lastIndexOf("@") ||
                        !(user.getEmail().lastIndexOf(".") >
                                user.getEmail().indexOf("@"))
        ) {
            throw UserException.invalidEmail();
        }
        if (
                user.getName() == null || user.getName().trim().split(" ").length < 2
        )
            throw UserException.invalidName();
        if (
                !isValidAddress(user.getAdress())
                //Straße, Hausnummer, PLZ, Ort
        )
            throw UserException.invalidAdress();

    }

    private void validateUpdatedUser(User updatedUser) {
        getUserById(updatedUser.getId());
    }

    private void saveUser(User user) throws UserException {
        validateUser(user);
        UsersById.put(user.getId(), clone(user));
    }

    private User clone(User user) {
        User userClone = new User(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAdress()
        );
        return userClone;
    }

    private boolean isValidAddress(String address) {
        if (address == null) return false;

        String[] parts = address.trim().split(" ");

        // Erwartet: Straße | Hausnummer | PLZ | Ort
        if (parts.length < 4) return false;

        // Straße kann aus mehreren Wörtern bestehen → wir fassen alles vor der Hausnummer zusammen
        // Beispiel: "Neue Muster Straße 12 04109 Leipzig"
        int lastStreetIndex = parts.length - 3;

        String street = String.join(" ", Arrays.copyOfRange(parts, 0, lastStreetIndex));
        String houseNumber = parts[lastStreetIndex];
        String plz = parts[lastStreetIndex + 1];
        String city = parts[lastStreetIndex + 2];

        // Hausnummer: Zahl + optional Buchstabe
        if (!houseNumber.matches("\\d+[A-Za-z]?")) return false;

        // PLZ: genau 5 Ziffern
        if (!plz.matches("\\d{5}")) return false;

        // Ort: nur Buchstaben (optional Bindestrich)
        if (!city.matches("[A-Za-zÄÖÜäöüß\\-]+")) return false;

        return true;
    }

}
