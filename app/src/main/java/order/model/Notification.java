package order.model;

public class Notification {
    public String title;
    public String description;
    public String time;
    public String type; // "order", "promo", "system", ...
    public Notification(String title, String description, String time, String type) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.type = type;
    }
}
