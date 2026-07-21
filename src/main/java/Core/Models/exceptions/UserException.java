package Core.Models.exceptions;

public class UserException extends RuntimeException{
    public static final String invalidEmail = "Invalid email";
    public static final String userDoesNotExist = "User does not exist";


    public UserException(String message) {
        super(message);
    }

    public static UserException invalidEmail(){return new UserException(invalidEmail);}
    public static UserException userDoesNotExist(){return new UserException(userDoesNotExist);}
}
