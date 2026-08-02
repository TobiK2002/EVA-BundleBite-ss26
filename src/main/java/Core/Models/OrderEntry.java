package Core.Models;

import java.util.UUID;

public class OrderEntry {
    private final UUID id;
    private final UUID userId;
    private final UUID dishId;
    //Snaphot of the price and name of the dish
    private String snapshotDishName;
    private long snapshotDishPrice;

    long sumPrice;
    int quantity;

    public OrderEntry(UUID id, UUID userId, UUID dishId, int quantity, long sumPrice, String snapshotDishName, long snapshotDishPrice) {
        this.id = id;
        this.userId = userId;
        this.dishId = dishId;
        this.snapshotDishName = snapshotDishName;
        this.snapshotDishPrice = snapshotDishPrice;
        this.sumPrice = sumPrice;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }
    public UUID getUserId() {
        return userId;
    }
    public UUID getDishId() {
        return dishId;
    }
    public long getSumPrice() {
        return sumPrice;
    }
    public int getQuantity() {
        return quantity;
    }
    public String getSnapshotDishName() {
        return snapshotDishName;
    }
    public long getSnapshotDishPrice() {
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
