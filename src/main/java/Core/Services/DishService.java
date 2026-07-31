package Core.Services;
import Core.Models.Dish;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import Core.Models.exceptions.DishException;


public class DishService {
    private final Map<UUID, Dish> dishesById = new ConcurrentHashMap<>();

    void saveDish(Dish newDish) throws DishException{
        validateDish(newDish);
        dishesById.put(newDish.getId(), cloneDish(newDish));
    }

    void validateDish(Dish newDish) throws DishException {

        if (newDish.getName() == null || newDish.getName().isBlank()) {
            throw DishException.invalidName();
        }
        if (newDish.getDescription() == null ||newDish.getDescription().isBlank()) {
            throw DishException.invalidDescription();
        }
        if (newDish.getPrice() < 0) {
            throw DishException.invalidPrice();
        }
        if (newDish.getIngredients().isEmpty()) {
            throw DishException.invalidIngredients();
        }
    }

    Dish cloneDish(Dish newDish) {
        return new Dish(
            newDish.getId(),
            newDish.getRestaurantId(),
            newDish.getName(),
            newDish.getDescription(),
            newDish.getPrice(),
            newDish.getIngredients()
        );
    }

    public Dish createDish(
            UUID restaurantId,
            String name,
            String description,
            double price,
            ArrayList<String> ingredients
    ) {
        UUID id = UUID.randomUUID();
        Dish newDish = new Dish(id, restaurantId, name, description, price, ingredients);
        saveDish(newDish);
        return newDish;
    }

    public void updateDish(
            Dish updatedDish
    ) {
        validateDish(updatedDish);
        saveDish(updatedDish);
    }

    public void deleteDish(
            UUID dishId
    ) throws DishException {
        if (dishesById.get(dishId) == null) {
            throw DishException.dishDoesNotExist();
        }
        dishesById.remove(dishId);
    }

    public Dish getDishByID (UUID id) throws DishException{
        if (id == null) {
            throw DishException.dishDoesNotExist();
        }

        Dish requestedDish = dishesById.get(id);
        return cloneDish(requestedDish);
    }

    public List<Dish> getAllDishes() {
        List<Dish> allDishes = new ArrayList<>();
        for (UUID dishId : dishesById.keySet()) {
            try {
                allDishes.add(getDishByID(dishId));
            } catch (DishException dishException) {
                System.out.print("Fehler beim Ziehen eines Gerichtes");
            }
        }

        return allDishes;
    }


}
