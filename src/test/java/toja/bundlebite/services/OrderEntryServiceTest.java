package toja.bundlebite.services;

import static org.junit.jupiter.api.Assertions.*;

import Core.Models.OrderEntry;
import Core.Models.exceptions.OrderEntryException;
import Core.Services.OrderEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class OrderEntryServiceTest {

    private OrderEntryService orderEntryService;
    private UUID testUserId;
    private UUID testDishId;
    private OrderEntry testOrderEntry;

    @BeforeEach
    void setUp() {
        orderEntryService = new OrderEntryService();
        testUserId = UUID.randomUUID();
        testDishId = UUID.randomUUID();
        testOrderEntry = createTestOrderEntry();
    }

    @Test
    @DisplayName("Should create valid order entry")
    void shouldCreateValidOrderEntry() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        String snapshotDishName = "Pizza Margherita";
        double snapshotDishPrice = 899;
        int quantity = 3;

        // Act
        OrderEntry orderEntry = orderEntryService.createOrderEntry(
                userId,
                dishId,
                snapshotDishName,
                snapshotDishPrice,
                quantity
        );

        // Assert
        assertNotNull(orderEntry);
        assertNotNull(orderEntry.getId());
        assertEquals(userId, orderEntry.getUserId());
        assertEquals(dishId, orderEntry.getDishId());
        assertEquals(snapshotDishName, orderEntry.getSnapshotDishName());
        assertEquals(snapshotDishPrice, orderEntry.getSnapshotDishPrice());
        assertEquals(quantity, orderEntry.getQuantity());
        assertEquals(snapshotDishPrice * quantity, orderEntry.getSumPrice());
    }

    @Test
    @DisplayName("Should get order entry by id")
    void shouldGetOrderEntryById() {
        // Act
        OrderEntry foundOrderEntry = orderEntryService.getOrderEntryById(testOrderEntry.getId());

        // Assert
        assertNotNull(foundOrderEntry);
        assertEquals(testOrderEntry.getId(), foundOrderEntry.getId());
        assertEquals(testOrderEntry.getUserId(), foundOrderEntry.getUserId());
        assertEquals(testOrderEntry.getDishId(), foundOrderEntry.getDishId());
        assertEquals(testOrderEntry.getQuantity(), foundOrderEntry.getQuantity());
        assertEquals(testOrderEntry.getSumPrice(), foundOrderEntry.getSumPrice());
        assertEquals(testOrderEntry.getSnapshotDishName(), foundOrderEntry.getSnapshotDishName());
        assertEquals(testOrderEntry.getSnapshotDishPrice(), foundOrderEntry.getSnapshotDishPrice());
    }

    @Test
    @DisplayName("Should throw exception when order entry does not exist")
    void shouldThrowExceptionWhenOrderEntryDoesNotExist() {
        // Arrange
        UUID unknownId = UUID.randomUUID();

        // Act & Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> orderEntryService.getOrderEntryById(unknownId)
        );
        assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should return a copy of the order entry")
    void shouldReturnCopyOfOrderEntry() {
        // Act
        OrderEntry foundOrderEntry = orderEntryService.getOrderEntryById(testOrderEntry.getId());

        // Assert
        assertNotSame(testOrderEntry, foundOrderEntry);
        assertEquals(testOrderEntry.getId(), foundOrderEntry.getId());
    }

    @Test
    @DisplayName("Should get all order entries")
    void shouldGetAllOrderEntries() {
        // Arrange
        OrderEntry anotherOrderEntry = orderEntryService.createOrderEntry(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pasta Napoli",
                799,
                1
        );

        // Act
        List<OrderEntry> orderEntries = orderEntryService.getAllOrderEntries();

        // Assert
        assertEquals(2, orderEntries.size());
        assertTrue(
                orderEntries.stream()
                        .anyMatch(orderEntry ->
                                orderEntry.getId().equals(testOrderEntry.getId())
                        )
        );
        assertTrue(
                orderEntries.stream()
                        .anyMatch(orderEntry ->
                                orderEntry.getId().equals(anotherOrderEntry.getId())
                        )
        );
    }

    @Test
    @DisplayName("Should return empty list when no order entries exist")
    void shouldReturnEmptyListWhenNoOrderEntriesExist() {
        // Arrange
        orderEntryService.deleteAllOrderEntries();

        // Act
        List<OrderEntry> orderEntries = orderEntryService.getAllOrderEntries();

        // Assert
        assertNotNull(orderEntries);
        assertTrue(orderEntries.isEmpty());
    }

    @Test
    @DisplayName("Should return copies of all order entries")
    void shouldReturnCopiesOfAllOrderEntries() {
        // Act
        List<OrderEntry> orderEntries = orderEntryService.getAllOrderEntries();

        // Assert
        assertEquals(1, orderEntries.size());
        assertNotSame(testOrderEntry, orderEntries.get(0));
        assertEquals(testOrderEntry.getId(), orderEntries.get(0).getId());
    }

    @Test
    @DisplayName("Should update existing order entry")
    void shouldUpdateExistingOrderEntry() {
        // Arrange
        OrderEntry updatedOrderEntry = new OrderEntry(
                testOrderEntry.getId(),
                testOrderEntry.getUserId(),
                testOrderEntry.getDishId(),
                5,
                4495,
                "Pizza Margherita",
                899
        );

        // Act
        orderEntryService.updateOrderEntry(updatedOrderEntry);

        // Assert
        OrderEntry foundOrderEntry = orderEntryService.getOrderEntryById(testOrderEntry.getId());
        assertEquals(updatedOrderEntry.getId(), foundOrderEntry.getId());
        assertEquals(updatedOrderEntry.getUserId(), foundOrderEntry.getUserId());
        assertEquals(updatedOrderEntry.getDishId(), foundOrderEntry.getDishId());
        assertEquals(updatedOrderEntry.getQuantity(), foundOrderEntry.getQuantity());
        assertEquals(updatedOrderEntry.getSumPrice(), foundOrderEntry.getSumPrice());
        assertEquals(updatedOrderEntry.getSnapshotDishName(), foundOrderEntry.getSnapshotDishName());
        assertEquals(updatedOrderEntry.getSnapshotDishPrice(), foundOrderEntry.getSnapshotDishPrice());
    }

    @Test
    @DisplayName("Should throw exception when updating non existing order entry")
    void shouldThrowExceptionWhenUpdatingNonExistingOrderEntry() {
        // Arrange
        OrderEntry unknownOrderEntry = new OrderEntry(
                UUID.randomUUID(),
                testUserId,
                testDishId,
                1,
                899,
                "Pizza Margherita",
                899
        );

        // Act & Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> orderEntryService.updateOrderEntry(unknownOrderEntry)
        );
        assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete existing order entry")
    void shouldDeleteExistingOrderEntry() {
        // Act
        orderEntryService.deleteOrderEntry(testOrderEntry.getId());

        // Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> orderEntryService.getOrderEntryById(testOrderEntry.getId())
        );
        assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when deleting non existing order entry")
    void shouldThrowExceptionWhenDeletingNonExistingOrderEntry() {
        // Arrange
        UUID unknownId = UUID.randomUUID();

        // Act & Assert
        OrderEntryException exception = assertThrows(
                OrderEntryException.class,
                () -> orderEntryService.deleteOrderEntry(unknownId)
        );
        assertEquals("Referenced OrderEntry does not exist", exception.getMessage());
    }

    @Test
    @DisplayName("Should not delete another order entry")
    void shouldNotDeleteAnotherOrderEntry() {
        // Arrange
        OrderEntry anotherOrderEntry = orderEntryService.createOrderEntry(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pasta Napoli",
                799,
                1
        );

        // Act
        orderEntryService.deleteOrderEntry(testOrderEntry.getId());

        // Assert
        OrderEntry foundOrderEntry = orderEntryService.getOrderEntryById(anotherOrderEntry.getId());
        assertNotNull(foundOrderEntry);
        assertEquals(anotherOrderEntry.getId(), foundOrderEntry.getId());
    }

    @Test
    @DisplayName("Should delete all order entries")
    void shouldDeleteAllOrderEntries() {
        // Arrange
        orderEntryService.createOrderEntry(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Pasta Napoli",
                799,
                1
        );

        // Act
        orderEntryService.deleteAllOrderEntries();

        // Assert
        assertTrue(orderEntryService.getAllOrderEntries().isEmpty());
    }

    @Test
    @DisplayName("Should not throw exception when deleting all order entries from empty service")
    void shouldNotThrowExceptionWhenDeletingAllOrderEntriesFromEmptyService() {
        // Arrange
        orderEntryService.deleteAllOrderEntries();

        // Act & Assert
        assertDoesNotThrow(() -> orderEntryService.deleteAllOrderEntries());
        assertTrue(orderEntryService.getAllOrderEntries().isEmpty());
    }

    private OrderEntry createTestOrderEntry() {
        return orderEntryService.createOrderEntry(
                testUserId,
                testDishId,
                "Pizza Margherita",
                899,
                2
        );
    }
}
