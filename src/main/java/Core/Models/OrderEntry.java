package Core.Models;

import java.util.UUID;

public class OrderEntry {
    private final UUID id;
    private final String userEmail;
    private final UUID dishId;
    //Snaphot of the price and name of the dish
    private String snapshotDishName;
    private double snapshotDishPrice;

    double sumPrice;
    int quantity;

    public OrderEntry(UUID id, String userEmail, UUID dishId, int quantity, double sumPrice, String snapshotDishName, double snapshotDishPrice) {
        this.id = id;
        this.userEmail = userEmail;
        this.dishId = dishId;
        this.snapshotDishName = snapshotDishName;
        this.snapshotDishPrice = snapshotDishPrice;
        this.sumPrice = sumPrice;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public UUID getDishId() {
        return dishId;
    }
    public double getSumPrice() {
        return sumPrice;
    }
    public int getQuantity() {
        return quantity;
    }
    public String getSnapshotDishName() {
        return snapshotDishName;
    }
    public double getSnapshotDishPrice() {
        return snapshotDishPrice;
    }
    public void setSnapshotDishName(String snapshotDishName) {
        this.snapshotDishName = snapshotDishName;
    }
    public void setSnapshotDishPrice(long snapshotDishPrice) {
        this.snapshotDishPrice = snapshotDishPrice;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
