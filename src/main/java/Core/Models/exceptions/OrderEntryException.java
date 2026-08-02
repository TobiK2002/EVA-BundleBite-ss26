package Core.Models.exceptions;

import Core.Models.OrderEntry;

public class OrderEntryException extends RuntimeException {
    public static final String orderEntryDoesNotExist = "Referenced OrderEntry does not exist";
    public static final String quantityMustBePositive = "Quantity must be positive";
    public static final String priceMustBePositive = "Price must be positive";

    public OrderEntryException(String message) {
        super(message);
    }

    public static OrderEntryException orderEntryDoesNotExist() {
        return new OrderEntryException(orderEntryDoesNotExist);

    }
    public static OrderEntryException quantityMustBePositive() {
        return new OrderEntryException(quantityMustBePositive);
    }
    public static OrderEntryException priceMustBePositive() {
        return new OrderEntryException(priceMustBePositive);
    }
}
