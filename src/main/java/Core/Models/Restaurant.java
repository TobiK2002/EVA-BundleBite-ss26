package Core.Models;


import Core.Models.exceptions.DishException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Restaurant {
    private final UUID id;
    private String name;
    private Address address;
    private double minOrderValue;
    private final List<UUID> dishes = new ArrayList<>();


    public Restaurant(UUID id, String name, Address address, double minOrderValue) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.minOrderValue = minOrderValue;

    }

    public Restaurant(UUID id, String name, String address, double minOrderValue) {
        this.id = id;
        this.name = name;
        this.address = Address.fromString(address);
        this.minOrderValue = minOrderValue;
    }
    public Restaurant(UUID id, String name, Address address, double minOrderValue, List<UUID> dishes) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.minOrderValue = minOrderValue;
        this.dishes.addAll(dishes);
    }


    public UUID getId() {return id;
    }
    public String getName() {
        return name;
    }
    public Address getAddress() {
        return address;
    }


    public double getMinOrderValue() {
        return minOrderValue;
    }

    public List<UUID> getAllDishIds() {
        return dishes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = Address.fromString(address);
    }
    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }
    public void addDish(UUID dishId) {
        dishes.add(dishId);
    }
    public void dropDish(UUID dishId) {
        dishes.remove(dishId);
    }
    public void dropAllDishes() {
        dishes.clear();
    }

}


