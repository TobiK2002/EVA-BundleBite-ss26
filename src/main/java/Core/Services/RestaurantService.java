package Core.Services;


import Core.Models.Address;
import Core.Models.Restaurant;
import Core.Models.Dish;
import Core.Models.exceptions.DishException;
import Core.Models.exceptions.RestaurantException;
import Core.Services.DishService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class RestaurantService {

    private final Map<UUID, Restaurant> restaurantsById = new ConcurrentHashMap<>();
    private final DishService dishService;

    public RestaurantService(DishService dishService) {
        this.dishService = dishService;
    }

    public Restaurant createRestaurant(String name, String address, Double minOrderValue) {
        UUID id = UUID.randomUUID();
        Restaurant restaurant = new Restaurant(id, name, address, minOrderValue);
        saveRestaurant(restaurant);
        return restaurant;
    }

    public Restaurant getRestaurantById(UUID id) throws RestaurantException {
        Restaurant restaurant = restaurantsById.get(id);
        if (restaurant == null) {
            throw RestaurantException.restaurantDoesNotExist();
        }

        return clone(restaurant);
    }

    public void updateRestaurant(Restaurant updatedRestaurant) throws RestaurantException {
        Restaurant original = restaurantsById.get(updatedRestaurant.getId());

        if (original == null) {
            throw RestaurantException.restaurantDoesNotExist();
        }

        //DishListe wird vom Original/Alten Restaurant übernommen. Die Dishes sollen nicht durch UpdateRestaurant modifiziert
        //werden sondern durch spezielle add/drop dish funktionen auf dem spezifischen Restaurant Objekt
        updatedRestaurant.dropAllDishes();;
        for (UUID id : original.getAllDishIds()) {
            updatedRestaurant.addDish(id);
        }

        saveRestaurant(updatedRestaurant);
    }

    public List<Restaurant> getAllRestaurants() throws RestaurantException {
        List<Restaurant> allRestaurants = new ArrayList<>();
        for (UUID restaurantId : restaurantsById.keySet()) {
            try {
                allRestaurants.add(getRestaurantById(restaurantId));
            } catch (RestaurantException restaurantException) {
                System.out.println("Fehler beim Ziehen eines Restaurants");
            }
        }
        return allRestaurants;
    }

    public void deleteRestaurant(UUID id) throws RestaurantException {
        Restaurant restaurant = restaurantsById.remove(id);
        if (restaurant == null) {
            throw RestaurantException.restaurantDoesNotExist();
        }
    }

    public void deleteAllRestaurants() {
        restaurantsById.clear();
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (restaurant.getName() == null || restaurant.getName().isBlank()) {
            throw RestaurantException.invalidName();
        }

        if (restaurant.getAddress() == null) {
            throw RestaurantException.invalidAddress();
        }

        if (restaurant.getMinOrderValue() == null || restaurant.getMinOrderValue() < 0) {
            throw RestaurantException.invalidMinOrderValue();
        }
    }


    private void saveRestaurant(Restaurant restaurant) throws RestaurantException {
        validateRestaurant(restaurant);
        restaurantsById.put(restaurant.getId(), clone(restaurant));
    }

    private Restaurant clone(Restaurant restaurant) {
        return new Restaurant(
                restaurant.getId(),
                restaurant.getName(),
                new Address(
                        restaurant.getAddress().getStreet(),
                        restaurant.getAddress().getHouseNumber(),
                        restaurant.getAddress().getPostalCode(),
                        restaurant.getAddress().getCity()
                ),
                restaurant.getMinOrderValue(),
                restaurant.getAllDishIds()

        );
    }

    public Dish createDishForRestaurant(
            UUID restaurantId,
            String name,
            String description,
            double price,
            ArrayList<String> ingredients
    ) throws RestaurantException {
        if (restaurantsById.containsKey(restaurantId)) {
            Dish newDish = dishService.createDish(
                    restaurantId,
                    name,
                    description,
                    price,
                    ingredients
            );

            Restaurant referenzedRestaurant = this.getRestaurantById(restaurantId);
            referenzedRestaurant.addDish(newDish.getId());
            updateRestaurant(referenzedRestaurant);

            return newDish;
        } else {
            throw RestaurantException.restaurantDoesNotExist();
        }
    }

    public void updateDish(Dish updatedDish) throws DishException {
        Restaurant restaurant = restaurantsById.get(updatedDish.getRestaurantId());

        if (!restaurant.getAllDishIds().contains(updatedDish.getId())) {
            throw DishException.dishDoesNotExist();
        }

        dishService.updateDish(updatedDish);
    }

    public void deleteDish(UUID dishId) throws DishException {
        Dish referencedDish = dishService.getDishByID(dishId);

        Restaurant referencedRestaurant = restaurantsById.get(referencedDish.getRestaurantId());
        referencedRestaurant.dropDish(dishId);
        dishService.deleteDish(dishId);
    }

    public Dish getDish(UUID dishId) {
        return dishService.getDishByID(dishId);
    }

    public List<Dish> getALlDishesForRestaurant(UUID restaurantId) {
        Restaurant referencedRestaurant = getRestaurantById(restaurantId);
        List<Dish> result = new ArrayList<>();

        for (UUID id : referencedRestaurant.getAllDishIds()) {
            result.add(dishService.getDishByID(id));
        }
        return result;
    }

}
