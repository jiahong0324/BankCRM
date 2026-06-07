package bankcrm.service;

import bankcrm.model.Announcement;
import bankcrm.model.Customer;
import bankcrm.model.Notification;
import bankcrm.model.User;

import java.util.List;

public class AnnouncementService {

    private static final AnnouncementService instance = new AnnouncementService();

    private AnnouncementService() {}

    public static AnnouncementService getInstance() {
        return instance;
    }

    private final DataStore ds = DataStore.getInstance();

    public List<Announcement> getAll() {
        return ds.getAnnouncements();
    }

    public List<Announcement> getActive() {
        return ds.getActiveAnnouncements();
    }

    public String post(String posterId, String posterName,
                       String title, String body, String typeStr) {
        if (title == null || title.isBlank()) {
            return "Title is required.";
        }
        if (body == null || body.isBlank()) {
            return "Message body is required.";
        }

        Announcement.AnnType type;
        try {
            type = Announcement.AnnType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return "Invalid announcement type.";
        }

        Announcement ann = new Announcement(ds.nextAnnId(), posterId, posterName,
                                            title.trim(), body.trim(), type);
        ds.addAnnouncement(ann);

        for (User u : ds.getUsers()) {
            if (u instanceof Customer && u.isActive()) {
                ds.addNotification(new Notification(
                    ds.nextNotifId(), u.getUserId(),
                    "[Announcement] " + title, ann.getId()));
            }
        }

        return null;
    }

    public boolean deactivate(String id) {
        Announcement a = null;
        for (Announcement ann : ds.getAnnouncements()) {
            if (ann.getId().equals(id)) {
                a = ann;
                break;
            }
        }
        if (a == null) {
            return false;
        }
        a.setActive(false);
        return true;
    }

    public boolean remove(String id) {
        return ds.removeAnnouncement(id);
    }
}