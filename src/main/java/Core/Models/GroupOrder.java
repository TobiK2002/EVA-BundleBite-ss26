package Core.Models;

import java.util.List;
import java.util.UUID;

public class GroupOrder {
    UUID id;
    UUID restaurantId;
    UUID creatorUserId;
    long expiresAt;
    List<OrderEntry> entries;
}
