package rest.server;

import Core.Models.GroupOrder;
import Core.Models.OrderEntry;
import Core.Models.exceptions.GroupOrderException;
import Core.Services.GroupOrderService;
import org.springframework.core.annotation.Order;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
public class GroupOrderThread extends Thread{
    private final UUID groupOrderId;
    private final int expiresAtMinutes;
    private final GroupOrderService groupOrderService;
    private final NotificationServer notificationServer;
    private Double minOrderPrice;

    public GroupOrderThread(
            UUID groupOrderId,
            int expiresAtMinutes,
            GroupOrderService groupOrderService,
            NotificationServer notificationServer,
            Double minOrderPrice
    ) {
        this.groupOrderId = groupOrderId;
        this.expiresAtMinutes = expiresAtMinutes;
        this.groupOrderService = groupOrderService;
        this.notificationServer = notificationServer;
        this.minOrderPrice = minOrderPrice;
    }

    @Override
    public void run() {
        try {
            long endTime = System.currentTimeMillis()
                    + expiresAtMinutes * 60_000L;

            while (true) {
                long remainingMilliseconds =
                        endTime - System.currentTimeMillis();

                // Ablaufzeit erreicht
                if (remainingMilliseconds <= 0) {
                    break;
                }

                // Höchstens eine Minute warten
                long waitingTime = Math.min(
                        remainingMilliseconds,
                        60_000L
                );

                Thread.sleep(waitingTime);

                remainingMilliseconds =
                        endTime - System.currentTimeMillis();

                // Noch nicht abgelaufen: Erinnerung senden
                if (remainingMilliseconds > 0) {
                    long remainingMinutes =
                            (long) Math.ceil(
                                    remainingMilliseconds / 60_000.0
                            );

                    notifyAllMembers(
                            "Die GroupOrder " + groupOrderId
                                    + " läuft noch "
                                    + remainingMinutes + " Minute(n)."
                    );
                }
            }

            // GroupOrder und Beteiligte vor dem Löschen laden
            GroupOrder groupOrder = groupOrderService.getGroupOrderById(groupOrderId);

            //gesamtpreis der GroupOrder berechnen um zu schauen ob mindestbestellwert überschritten wurde
            double priceOfAllOrderEntries = 0;
            List<OrderEntry> allOrderEntries= groupOrderService.getAllOrderEntriesForGroupOrder(groupOrderId);
            for (OrderEntry entry : allOrderEntries) {
                priceOfAllOrderEntries += entry.getSumPrice();
            }


            Set<String> userEmails = new HashSet<>();
            userEmails.add(groupOrder.getCreatorUserEmail());

            for (OrderEntry entry :
                    groupOrderService.getAllOrderEntriesForGroupOrder(groupOrderId)) {

                userEmails.add(entry.getUserEmail());
            }

            groupOrderService.deleteGroupOrder(groupOrderId);


            if (priceOfAllOrderEntries >= minOrderPrice) {
                for (String email : userEmails) {

                    notificationServer.notifyUser(
                            email,
                            "Die GroupOrder " + groupOrderId + " wurde geschlossen," +
                                    " weil die Ablaufzeit erreicht wurde." +
                                    " Die Bestellung wurde erfolgreich aufgegeben."
                    );
                }
            } else {
                for (String email : userEmails) {

                    notificationServer.notifyUser(
                            email,
                            "Die GroupOrder " + groupOrderId + " wurde geschlossen," +
                                    " weil die Ablaufzeit erreicht wurde." +
                                    " Die Bestellung konnte nicht aufgegeben werden,da der Mindestbestellwert nicht erreicht wurde."
                    );
                }
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

        } catch (GroupOrderException exception) {
            // GroupOrder wurde schon manuell gelöscht.
        }
    }

    private void notifyAllMembers(String message) {
        GroupOrder groupOrder =
                groupOrderService.getGroupOrderById(groupOrderId);

        Set<String> userEmails = new HashSet<>();
        userEmails.add(groupOrder.getCreatorUserEmail());

        for (OrderEntry entry :
                groupOrderService.getAllOrderEntriesForGroupOrder(groupOrderId)) {

            userEmails.add(entry.getUserEmail());
        }

        for (String email : userEmails) {
            notificationServer.notifyUser(email, message);
        }
    }
}
