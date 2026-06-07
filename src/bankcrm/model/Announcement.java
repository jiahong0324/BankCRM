package bankcrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Color;

public class Announcement {

    public enum AnnType {
        INFO, WARNING, MAINTENANCE, URGENT
    }

    private String id;
    private String postedById;
    private String postedByName;
    private String title;
    private String body;
    private AnnType type;
    private boolean active;
    private LocalDateTime postedAt;
    
    public Announcement() {
        this.id = "";
        this.postedById = "";
        this.postedByName = "";
        this.title = "";
        this.body = "";
        this.type = AnnType.INFO;
        this.active = true;
        this.postedAt = LocalDateTime.now();
    }

    public Announcement(String id, String postedById, String postedByName,
                        String title, String body, AnnType type) {
        this.id = id;
        this.postedById = postedById;
        this.postedByName = postedByName;
        this.title = title;
        this.body = body;
        this.type = type;
        this.active = true;
        this.postedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getPostedById() {
        return postedById;
    }

    public String getPostedByName() {
        return postedByName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public AnnType getType() {
        return type;
    }

    public void setType(AnnType type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public String getFormattedDate() {
        return postedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Color typeColor() {
        if (type == AnnType.URGENT) {
            return new Color(198, 40, 40);
        } else if (type == AnnType.WARNING) {
            return new Color(230, 119, 0);
        } else if (type == AnnType.MAINTENANCE) {
            return new Color(13, 71, 161);
        } else {
            return new Color(46, 125, 50);
        }
    }
}