package Core.Services;

import Core.Models.Address;
import Core.Models.Restaurant;
import Core.Models.exceptions.RestaurantException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RestaurantService {

    private final Map<UUID, Restaurant> restaurantsById = new ConcurrentHashMap<>();

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
        validateUpdatedRestaurant(updatedRestaurant);
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

    private void validateUpdatedRestaurant(Restaurant updatedRestaurant) {
        getRestaurantById(updatedRestaurant.getId());
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
                restaurant.getMinOrderValue()
        );
    }
}
