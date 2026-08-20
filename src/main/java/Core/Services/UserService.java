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

    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();

    public User createUser(String name, String email, String address) throws UserException {
        if (usersByEmail.containsKey(email)) {
            throw UserException.emailDoesAlreadyExist();
        }
        try {
            User user = new User( name, email, address);
            saveUser(user);
            return user;
        } catch (IllegalArgumentException exception) {
            throw UserException.invalidAddress();
        }
    }

    public User getUserByEmail(String email) throws UserException {
        User user = usersByEmail.get(email);
        if (user == null) {
            throw UserException.userDoesNotExist();
        }

        return clone(user);
    }

    public List<User> getAllUsers() throws UserException {
        List<User> allUsers = new ArrayList<>();
        for(String email : usersByEmail.keySet()){
            try {
                allUsers.add(clone(getUserByEmail(email)));
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

    public void deleteUser(String email) throws UserException {
        User user = usersByEmail.remove(email);
        if (user == null) {
            throw UserException.userDoesNotExist();
        }
    }

    public void deleteAllUsers(){
        usersByEmail.clear();
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
        getUserByEmail(updatedUser.getEmail());
    }

    private void saveUser(User user) throws UserException {
        validateUser(user);
        usersByEmail.put(user.getEmail(), clone(user));
    }

    private User clone(User user) {
        return new User(
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
