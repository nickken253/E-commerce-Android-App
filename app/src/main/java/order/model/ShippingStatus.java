package order.model;

public class ShippingStatus {
    public String status; // Ví dụ: "Đã lấy hàng", "Đang giao", "Đã giao"
    public String time;   // "2025-04-18 10:00"
    public String note;   // Ghi chú thêm nếu có

    public ShippingStatus() {}

    public ShippingStatus(String status, String time, String note) {
        this.status = status;
        this.time = time;
        this.note = note;
    }
}
