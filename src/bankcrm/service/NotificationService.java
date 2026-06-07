package bankcrm.service;

import bankcrm.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private static final NotificationService instance = new NotificationService();

    private NotificationService() {}

    public static NotificationService getInstance() {
        return instance;
    }

    private final DataStore ds = DataStore.getInstance();

    public void notify(String userId, String message, String ticketId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        ds.addNotification(new Notification(ds.nextNotifId(), userId, message, ticketId));
    }

    public List<Notification> getAll(String userId) {
        return ds.getNotificationsForUser(userId);
    }

    public List<Notification> getUnread(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : ds.getNotificationsForUser(userId)) {
            if (!n.isRead()) {
                result.add(n);
            }
        }
        return result;
    }

    public int unreadCount(String userId) {
        return ds.unreadCount(userId);
    }

    public void markAllRead(String userId) {
        List<Notification> notifications = ds.getNotificationsForUser(userId);
        for (Notification n : notifications) {
            n.setRead(true);
        }
    }
}