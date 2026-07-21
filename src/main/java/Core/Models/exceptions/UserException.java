package Core.Models.exceptions;

public class UserException extends RuntimeException{
    public static final String invalidEmail = "Invalid email";
    public static final String userDoesNotExist = "User does not exist";
    public static final String invalidName = "Invalid username";
    public static final String invalidAdress = "Invalid adress";


    public UserException(String message) {
        super(message);
    }

    public static UserException invalidEmail(){return new UserException(invalidEmail);}
    public static UserException userDoesNotExist(){return new UserException(userDoesNotExist);}
    public static UserException invalidName(){return new UserException(invalidName);}
    public static UserException invalidAdress(){return new UserException(invalidAdress);}
}
