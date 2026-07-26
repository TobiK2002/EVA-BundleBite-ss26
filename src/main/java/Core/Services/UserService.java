package Core.Services;

import Core.Models.Address;
import Core.Models.User;
import Core.Models.exceptions.UserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final Map<UUID, User> usersById = new ConcurrentHashMap<>();

    public User createUser(String name, String email, String address) {
        UUID id = UUID.randomUUID();

        try {
            User user = new User(id, name, email, address);
            saveUser(user);
            return user;
        } catch (IllegalArgumentException exception) {
            throw UserException.invalidAddress();
        }
    }

    public User getUserById(UUID id) throws UserException {
        User user = usersById.get(id);
        if (user == null) {
            throw UserException.userDoesNotExist();
        }

        return clone(user);
    }
    public List<User> getAllUsers() throws UserException {
        List<User> allUsers = new ArrayList<>();
        for(UUID userId : usersById.keySet()){
            try {
                allUsers.add(getUserById(userId));
            } catch (UserException userException){
                System.out.println("Fehler beim Ziehen eines Users");
            }
        }
        return allUsers;
    }

    public void updateUser(User updatedUser) throws UserException {
        validateUpdatedUser(updatedUser);
        saveUser(updatedUser);
    }

    public void deleteUser(UUID id) throws UserException {
        User user = usersById.remove(id);
        if (user == null) {
            throw UserException.userDoesNotExist();
        }
    }

    public void deleteAllUsers(){
        usersById.clear();
    }

    private void validateUser(User user) {
        if (
                user.getEmail() == null ||
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
        ) {
            throw UserException.invalidName();
        }

        if (user.getAddress() == null) {
            throw UserException.invalidAddress();
        }
    }

    private void validateUpdatedUser(User updatedUser) {
        getUserById(updatedUser.getId());
    }

    private void saveUser(User user) throws UserException {
        validateUser(user);
        usersById.put(user.getId(), clone(user));
    }

    private User clone(User user) {
        return new User(
                user.getId(),
                user.getName(),
                user.getEmail(),
                new Address(
                        user.getAddress().getStreet(),
                        user.getAddress().getHouseNumber(),
                        user.getAddress().getPostalCode(),
                        user.getAddress().getCity()
                )
        );
    }
}
