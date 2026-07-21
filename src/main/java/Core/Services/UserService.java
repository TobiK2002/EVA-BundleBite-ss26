package Core.Services;

import Core.Models.User;
import Core.Models.exceptions.UserException;

import java.time.LocalDate;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final Map<UUID, User> UsersById = new ConcurrentHashMap<>();

    public User createUser(String username, String email){
        UUID id= UUID.randomUUID();
        User user = new User(id, username, email);
        saveUser(user);
        return user;
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
                user.getUsername(),
                user.getEmail()
        );
        return userClone;
    }
}
