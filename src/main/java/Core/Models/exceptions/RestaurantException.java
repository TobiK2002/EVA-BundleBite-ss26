package Core.Models.exceptions;

public class RestaurantException extends RuntimeException {

    public static final String invalidName = "Invalid restaurant name";
    public static final String invalidAddress = "Invalid restaurant address";
    public static final String invalidMinOrderValue = "Invalid minimum order value";
    public static final String restaurantDoesNotExist = "Restaurant does not exist";
    public RestaurantException(String message) {
        super(message);
    }
    
    
    
    
    
        
    public static RestaurantException invalidName() {
        return new RestaurantException(invalidName);
    }

    public static RestaurantException invalidAddress() {
        return new RestaurantException(invalidAddress);
    }

    public static RestaurantException invalidMinOrderValue() {
        return new RestaurantException(invalidMinOrderValue);
    }

    public static RestaurantException restaurantDoesNotExist() {
        return new RestaurantException(restaurantDoesNotExist);
    }
}
