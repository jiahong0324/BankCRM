package bankcrm.model;

public class CannedResponse {

    private String id;
    private String title;
    private String body;
    private String category;
    private boolean active;
    
    public CannedResponse() {
        this.id = "";
        this.title = "";
        this.body = "";
        this.category = "";
        this.active = true;
    }

    public CannedResponse(String id, String title, String body, String category) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.category = category;
        this.active = true;
    }

    public String getId() {
        return id;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return title;
    }
}