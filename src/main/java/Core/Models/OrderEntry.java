package Core.Models;

import java.util.UUID;

public class OrderEntry {
    UUID id;
    UUID userId;
    UUID dishId;
    UUID groupOrderId;
    long sumPrice;

    int quantity;
}
