package Core.Models;

import java.util.UUID;
import java.util.ArrayList;

public class Dish {
    private final UUID id;
    private final UUID restaurantId;
    String name;
    String description;
    double price;
    ArrayList<String> ingredients;

    public Dish(UUID id, UUID restaurantId, String name, String description, double price, ArrayList<String> ingredients) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        if (!ingredients.isEmpty()) {
            this.ingredients.addAll(ingredients);
        }

    }

    public UUID getId() {
        return id;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }
    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Boolean addIngredient(String ingredient) {
        if (ingredients.contains(ingredient)) {
            return true;
        } else {
            ingredients.add(ingredient);
            return false;
        }
    }

    public Boolean deleteIngredient(String ingredient) {
        if (!ingredients.contains(ingredient)) {
            return false;
        } else {
            ingredients.remove(ingredient);
            return true;
        }
    }
}
