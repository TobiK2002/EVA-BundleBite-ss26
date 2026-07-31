package Core.Models.exceptions;

import Core.Models.Dish;

public class DishException extends RuntimeException {
    public static final String invalidName = "Invalid dish name";
    public static final String invalidDescription = "Invalid dish description";
    public static final String invalidPrice = "Invalid dish price";
    public static final String invalidIngredients = "Dish requires at least one ingredient";
    public static final String dishDoesNotExist = "Referenced Dish does not exist";
    public static final String restaurantDoesNotExist = "Referenced Restaurant does not exist";
    public static final String dishAlreadyExists = "Referenced Dish is already in the List";

    public DishException(String message) {
        super(message);
    }

    public static DishException invalidName() {return new DishException(invalidName);}
    public static DishException invalidDescription() {return new DishException(invalidDescription);}
    public static DishException invalidPrice() {return new DishException(invalidPrice);}
    public static DishException invalidIngredients() {return new DishException(invalidIngredients);}
    public static DishException dishDoesNotExist() {return new DishException(dishDoesNotExist);}
    public static DishException restaurantDoesNotExist() {return new DishException(restaurantDoesNotExist);}
    public static DishException dishAlreadyExists() {return new DishException(dishAlreadyExists);}
}
