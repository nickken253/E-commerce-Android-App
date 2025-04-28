package order.model;
import java.util.List;

public class Order {
    public int orderId;
    public String orderDate;
    public double totalAmount;
    public String status;
    public List<OrderItem> items;
    public String shopName;
    public List<ShippingStatus> shippingStatusHistory;
}