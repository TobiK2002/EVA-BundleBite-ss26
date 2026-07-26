package Core.Models.exceptions;

public class AddressException extends RuntimeException {


    public static final String invalidAddress = "Invalid address";
    public static final String invalidStreet = "Invalid street";
    public static final String invalidHouseNumber = "Invalid house number";
    public static final String invalidPostalCode = "Invalid postal code";
    public static final String invalidCity = "Invalid city";


    public AddressException(String message) {
        super(message);
    }

    public static AddressException invalidAddress() {
        return new AddressException(invalidAddress);
    }

    public static AddressException invalidStreet() {
        return new AddressException(invalidStreet);
    }

    public static AddressException invalidHouseNumber() {
        return new AddressException(invalidHouseNumber);
    }

    public static AddressException invalidPostalCode() {
        return new AddressException(invalidPostalCode);
    }

    public static AddressException invalidCity() {
        return new AddressException(invalidCity);
    }
}
