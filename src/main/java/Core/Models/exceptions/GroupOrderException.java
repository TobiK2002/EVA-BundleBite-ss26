package Core.Models.exceptions;

import Core.Models.GroupOrder;

public class GroupOrderException extends RuntimeException {
    public static final String GroupOrderDoesNotExist = "Group Order does not exist";
    public static final String InvalidExpirationime = "The expiration time is invalid";
    public static final String OrderEntryNotFound = "This Order Entry is not included in this Group-Order";

    public GroupOrderException(String message) {
        super(message);
    }

    public static GroupOrderException GroupOrderDoesNotExist() {return new GroupOrderException(GroupOrderDoesNotExist);}
    public static GroupOrderException InvalidExpirationTime() {return new GroupOrderException(InvalidExpirationime);}
    public static GroupOrderException OrderEntryNotFound() {return new GroupOrderException(OrderEntryNotFound);}
}
