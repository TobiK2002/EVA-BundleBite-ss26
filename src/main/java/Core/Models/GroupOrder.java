package Core.Models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupOrder {
    UUID id;
    UUID restaurantId;
    String creatorUserEmail;
    int expiresAt;
    List<UUID> entries = new ArrayList<>();


    public GroupOrder(UUID id, UUID restaurantId, String creatorUserEmail, int expiresAt) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.creatorUserEmail = creatorUserEmail;
        this.expiresAt = expiresAt;
    }

    public GroupOrder(UUID id, UUID restaurantId, String creatorUserEmail, int expiresAt, List<UUID> OrderEntries) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.creatorUserEmail = creatorUserEmail;
        this.expiresAt = expiresAt;
        this.entries.addAll(OrderEntries);
    }
    public UUID getId() {
        return this.id;
    }
    public String getCreatorUserEmail() {
        return this.creatorUserEmail;
    }
    public UUID getRestaurantId() {
        return this.restaurantId;
    }
    public int getExpiresAt() {
        return this.expiresAt;
    }
    public List<UUID> getAllOrderEntryIds()  {
        return this.entries;
    }


    public void addOrderEntry(UUID id) {
        this.entries.add(id);
    }
    public void dropOrderEntry(UUID id) {
        this.entries.remove(id);
    }
    public void dropAllOrderEntries() {
        this.entries.clear();
    }
}
