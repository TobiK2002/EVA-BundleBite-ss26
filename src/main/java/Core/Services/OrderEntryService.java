package Core.Services;

import Core.Models.Address;
import Core.Models.Dish;
import Core.Models.OrderEntry;
import Core.Models.Restaurant;
import Core.Models.exceptions.OrderEntryException;
import Core.Models.exceptions.RestaurantException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderEntryService {
    private final Map<UUID, OrderEntry> orderEntriesById = new ConcurrentHashMap<>();
    private final DishService dishService;
    private final UserService userService;
    private final RestaurantService restaurantService;


    public OrderEntryService(DishService dishService, UserService userService, RestaurantService restaurantService) {
        this.dishService = dishService;
        this.userService = userService;
        this.restaurantService = restaurantService;
    }

    public OrderEntry createOrderEntry(UUID userId, UUID dishId, int quantity) {
        UUID id = UUID.randomUUID();
        Dish referencedDish = dishService.getDishByID(dishId);
        String snapshotDishName = referencedDish.getName();
        long snapshotDishPrice = referencedDish.getPrice();
        long totalPrice = quantity * referencedDish.getPrice();

        OrderEntry entry = new OrderEntry(id, userId, dishId, quantity, totalPrice, snapshotDishName, snapshotDishPrice);
        saveOrderEntry(entry);
        return entry;
    }

    public OrderEntry getOrderEntryById(UUID id){
        OrderEntry entry = orderEntriesById.get(id);
        if (entry == null) {
            throw OrderEntryException.orderEntryDoesNotExist();
        }
        return clone(entry);
    }

    public List<OrderEntry> getAllOrderEntries(){
        List<OrderEntry> allEntries = new ArrayList<>();
        for(UUID entryId : orderEntriesById.keySet()){
            try {
                allEntries.add(getOrderEntryById(entryId));
            } catch (OrderEntryException orderEntryException){
                System.out.println("Fehler beim Ziehen eines OrderEntries");
            }
        }
        return allEntries;
    }

    public void updateOrderEntry(OrderEntry updatedOrderEntry) throws OrderEntryException {
        OrderEntry original = orderEntriesById.get(updatedOrderEntry.getId());
        if (original == null) {
            throw OrderEntryException.orderEntryDoesNotExist();
        }
        saveOrderEntry(updatedOrderEntry);
    }

    public void deleteOrderEntry(UUID id) throws OrderEntryException {
        OrderEntry entry = orderEntriesById.remove(id);
        if (entry == null) {
            throw OrderEntryException.orderEntryDoesNotExist();
        }
    }

    public void deleteAllOrderEntries() {
        orderEntriesById.clear();
    }
    private void saveOrderEntry(OrderEntry orderEntry) throws OrderEntryException {
        validateOrderEntry(orderEntry);
        orderEntriesById.put(orderEntry.getId(), clone(orderEntry));
    }

    private void validateOrderEntry(OrderEntry orderEntry) throws OrderEntryException {
        if (orderEntry.getQuantity() < 0) {
            throw OrderEntryException.quantityMustBePositive();
        }
        if (orderEntry.getSumPrice() < 0) {
            throw OrderEntryException.priceMustBePositive();
        }

    }

    private OrderEntry clone(OrderEntry orderEntry) {
        return new OrderEntry(
                orderEntry.getId(),
                orderEntry.getUserId(),
                orderEntry.getDishId(),
                orderEntry.getQuantity(),
                orderEntry.getSumPrice(),
                orderEntry.getSnapshotDishName(),
                orderEntry.getSnapshotDishPrice()
        );
    }
}