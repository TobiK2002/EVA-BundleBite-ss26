package Core.Services;

import Core.Models.OrderEntry;
import Core.Models.exceptions.OrderEntryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderEntryService {
    private final Map<UUID, OrderEntry> orderEntriesById = new ConcurrentHashMap<>();


    public OrderEntry createOrderEntry(String userEmail, UUID dishId,String snapshotDishName, double snapshotDishPrice, int quantity) {
        UUID id = UUID.randomUUID();

        double totalPrice = quantity * snapshotDishPrice;

        OrderEntry entry = new OrderEntry(id, userEmail, dishId, quantity, totalPrice, snapshotDishName, snapshotDishPrice);
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
        orderEntriesById.put(orderEntry.getId(), clone(orderEntry));
    }

    private OrderEntry clone(OrderEntry orderEntry) {
        return new OrderEntry(
                orderEntry.getId(),
                orderEntry.getUserEmail(),
                orderEntry.getDishId(),
                orderEntry.getQuantity(),
                orderEntry.getSumPrice(),
                orderEntry.getSnapshotDishName(),
                orderEntry.getSnapshotDishPrice()
        );
    }
}
