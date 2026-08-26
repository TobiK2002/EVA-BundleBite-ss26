package Core.Services;

import Core.Models.Dish;
import Core.Models.GroupOrder;
import Core.Models.OrderEntry;
import Core.Models.User;
import Core.Models.exceptions.DishException;
import Core.Models.exceptions.GroupOrderException;
import Core.Models.exceptions.OrderEntryException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupOrderService {
    private final Map<UUID, GroupOrder> groupOrdersById = new ConcurrentHashMap<>();
    private final RestaurantService restaurantService;
    private final UserService userService;
    private final OrderEntryService orderEntryService;

    public GroupOrderService(
            RestaurantService restaurantService,
            UserService userService,
            OrderEntryService orderEntryService
    ) {
        this.restaurantService = restaurantService;
        this.userService = userService;
        this.orderEntryService = orderEntryService;
    }

    public GroupOrder createGroupOrder(UUID restaurantId, String creatorUserEmail, int expiresAt) {
        UUID id = UUID.randomUUID();
        GroupOrder groupOrder = new GroupOrder(id, restaurantId, creatorUserEmail, expiresAt);
        saveGroupOrder(groupOrder);
        return groupOrder;
    }

    public GroupOrder getGroupOrderById(UUID id) {
        GroupOrder groupOrder = groupOrdersById.get(id);
        if (groupOrder == null) {
            throw GroupOrderException.GroupOrderDoesNotExist();
        }

        return cloneGroupOrder(groupOrder);
    }

    public List<GroupOrder> getAllGroupOrders() {
        List<GroupOrder> allGroupOrders = new ArrayList<>();
        for (UUID groupOrderId : groupOrdersById.keySet()) {
            try {
                allGroupOrders.add(getGroupOrderById(groupOrderId));
            } catch (GroupOrderException groupOrderException) {
                System.out.println("Fehler beim Ziehen einer GroupOrder");
            }
        }
        return allGroupOrders;
    }

    public List<GroupOrder> getAllGroupOrdersWithSamePostalCode(String postal) {
        List<GroupOrder> allGroupOrders = getAllGroupOrders();
        List<GroupOrder> allGroupOrdersWithSamePostal = new ArrayList<>();

        for (GroupOrder groupOrder : allGroupOrders) {
            User owner = userService.getUserByEmail(groupOrder.getCreatorUserEmail());
            if (Objects.equals(owner.getAddress().getPostalCode(), postal)) {
                allGroupOrdersWithSamePostal.add(groupOrder);
            }
        }
        return allGroupOrdersWithSamePostal;
    }

    public void deleteGroupOrder(UUID id) {
        GroupOrder groupOrder = groupOrdersById.remove(id);
        if (groupOrder == null) {
            throw GroupOrderException.GroupOrderDoesNotExist();
        }

        for (UUID orderEntryId : groupOrder.getAllOrderEntryIds()) {
            orderEntryService.deleteOrderEntry(orderEntryId);
        }
    }

    public void deleteAllGroupOrders() {
        groupOrdersById.clear();
        orderEntryService.deleteAllOrderEntries();
    }

    public OrderEntry createOrderEntryForGroupOrder(UUID groupOrderId, String userEmail, UUID dishId, int quantity) {
        GroupOrder groupOrder = getGroupOrderById(groupOrderId);
        validateOrderEntryData(groupOrder, userEmail, dishId, quantity);

        Dish dish = restaurantService.getDish(dishId);
        OrderEntry orderEntry = orderEntryService.createOrderEntry(
                userEmail,
                dishId,
                dish.getName(),
                dish.getPrice(),
                quantity
        );

        groupOrder.addOrderEntry(orderEntry.getId());
        saveGroupOrder(groupOrder);
        return orderEntry;
    }

    public OrderEntry getOrderEntry(UUID orderEntryId) {
        return orderEntryService.getOrderEntryById(orderEntryId);
    }

    public List<OrderEntry> getAllOrderEntriesForGroupOrder(UUID groupOrderId) {
        GroupOrder groupOrder = getGroupOrderById(groupOrderId);
        List<OrderEntry> orderEntries = new ArrayList<>();

        for (UUID orderEntryId : groupOrder.getAllOrderEntryIds()) {
            orderEntries.add(orderEntryService.getOrderEntryById(orderEntryId));
        }

        return orderEntries;
    }

    public List<GroupOrder> getAllGroupOrdersForUser(String userEmail) {
        //Validierung
        userService.getUserByEmail(userEmail);

        List<GroupOrder> groupOrdersForUser = new ArrayList<>();

        for (GroupOrder groupOrder : getAllGroupOrders()) {
            if (Objects.equals(groupOrder.getCreatorUserEmail(), userEmail)) {
                groupOrdersForUser.add(groupOrder);
                continue;
            }
            // Wenn der Angefragt User Ersteller ist, ist eh schon Teil der GroupOrder
            //Anderfalls werden die Order Entries der jeweiligen Grouporder nach dem User durchsucht

            for (OrderEntry orderEntry : getAllOrderEntriesForGroupOrder(groupOrder.getId())) {
                if (Objects.equals(orderEntry.getUserEmail(), userEmail)) {
                    groupOrdersForUser.add(groupOrder);
                    break;
                }
            }
        }

        return groupOrdersForUser;
    }

    public List<OrderEntry> getAllOrderEntriesByGroupOrderByUser(String userEmail, UUID groupOrderId) {
        List<OrderEntry> allOrderEntries = getAllOrderEntriesForGroupOrder(groupOrderId);
        List<OrderEntry> orderEntriesByGroupOrderByUser = new ArrayList<>();
        for (OrderEntry entry : allOrderEntries) {
            if (Objects.equals(entry.getUserEmail(), userEmail)) {
                orderEntriesByGroupOrderByUser.add(entry);
            }
        }
        return orderEntriesByGroupOrderByUser;
    }

    public void updateOrderEntry(UUID groupOrderId, UUID orderEntryId, int quantity) {
        GroupOrder groupOrder = getGroupOrderById(groupOrderId);
        if (!groupOrder.getAllOrderEntryIds().contains(orderEntryId)) {
            throw GroupOrderException.OrderEntryNotFound();
        }

        OrderEntry existingOrderEntry = orderEntryService.getOrderEntryById(orderEntryId);
        validateOrderEntryData(
                groupOrder,
                existingOrderEntry.getUserEmail(),
                existingOrderEntry.getDishId(),
                quantity
        );

        Dish dish = restaurantService.getDish(existingOrderEntry.getDishId());
        OrderEntry updatedOrderEntry = new OrderEntry(
                orderEntryId,
                existingOrderEntry.getUserEmail(),
                existingOrderEntry.getDishId(),
                quantity,
                quantity * dish.getPrice(),
                dish.getName(),
                dish.getPrice()
        );

        orderEntryService.updateOrderEntry(updatedOrderEntry);
    }

    public void deleteOrderEntry(UUID groupOrderId, UUID orderEntryId) {
        GroupOrder groupOrder = getGroupOrderById(groupOrderId);
        if (!groupOrder.getAllOrderEntryIds().contains(orderEntryId)) {
            throw GroupOrderException.OrderEntryNotFound();
        }
        groupOrder.dropOrderEntry(orderEntryId);
        orderEntryService.deleteOrderEntry(orderEntryId);
        saveGroupOrder(groupOrder);
    }

    private void validateGroupOrder(GroupOrder groupOrder) {
        userService.getUserByEmail(groupOrder.getCreatorUserEmail());
        restaurantService.getRestaurantById(groupOrder.getRestaurantId());

        if (groupOrder.getExpiresAt() <= 0) {
            throw GroupOrderException.InvalidExpirationTime();
        }
    }

    private void validateOrderEntryData(GroupOrder groupOrder, String userEmail, UUID dishId, int quantity) {
        if (quantity < 0) {
            throw OrderEntryException.quantityMustBePositive();
        }

        userService.getUserByEmail(userEmail);

        Dish dish = restaurantService.getDish(dishId);
        if (!dish.getRestaurantId().equals(groupOrder.getRestaurantId())) {
            throw DishException.dishDoesNotExist();
        }
    }

    private void saveGroupOrder(GroupOrder groupOrder) {
        validateGroupOrder(groupOrder);
        groupOrdersById.put(groupOrder.getId(), cloneGroupOrder(groupOrder));
    }



    private GroupOrder cloneGroupOrder(GroupOrder groupOrder) {
        return new GroupOrder(
                groupOrder.getId(),
                groupOrder.getRestaurantId(),
                groupOrder.getCreatorUserEmail(),
                groupOrder.getExpiresAt(),
                groupOrder.getAllOrderEntryIds()
        );
    }
}
//Test