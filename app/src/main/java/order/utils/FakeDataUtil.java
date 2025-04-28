package order.utils;
import order.model.*;
import java.util.*;

public class FakeDataUtil {
    public static List<Order> getFakeOrders() {
        List<Order> orders = new ArrayList<>();
        String[] shopNames = {"giadungtuyetnhi", "Shop ABC", "Điện máy Xanh", "Thời trang Mèo", "Shopee Mall", "Sách 24h"};
        String[] products = {"Nước rửa tay", "Áo phông nam", "Tai nghe Bluetooth", "Giày sneaker", "Sách luyện thi", "Bình giữ nhiệt", "Balo laptop", "Đèn bàn LED", "Sữa rửa mặt", "Bút bi"};
        String[] variants = {"Hương Dâu", "Size XL", "Màu Đen", "Loại 500ml", "Combo 2 cuốn", "Màu Xanh", "Phiên bản 2025", "Hộp 10 cây"};
        String[] statusArr = {"pending", "processing", "shipping", "completed", "cancelled"};
        Random rand = new Random();
        int orderId = 1;
        for (int i = 0; i < 40; i++) { // Fake 40 đơn hàng
            Order order = new Order();
            order.orderId = orderId++;
            order.shopName = shopNames[rand.nextInt(shopNames.length)];
            order.orderDate = "2025-04-" + (rand.nextInt(28) + 1);
            order.status = statusArr[rand.nextInt(statusArr.length)];
            order.items = new ArrayList<>();
            int itemCount = 1 + rand.nextInt(3);
            double total = 0;
            for (int j = 0; j < itemCount; j++) {
                OrderItem item = new OrderItem();
                item.productName = products[rand.nextInt(products.length)];
                item.variant = variants[rand.nextInt(variants.length)];
                item.price = 50000 + rand.nextInt(900000);
                item.quantity = 1 + rand.nextInt(3);
                total += item.price * item.quantity;
                order.items.add(item);
            }
            order.totalAmount = total;
            // Fake shippingStatusHistory
            order.shippingStatusHistory = new ArrayList<>();
            order.shippingStatusHistory.add(new ShippingStatus("Đặt hàng thành công", order.orderDate, ""));
            if (!order.status.equals("pending"))
                order.shippingStatusHistory.add(new ShippingStatus("Đang xử lý", order.orderDate, ""));
            if (order.status.equals("shipping") || order.status.equals("completed"))
                order.shippingStatusHistory.add(new ShippingStatus("Đang giao hàng", order.orderDate, ""));
            if (order.status.equals("completed"))
                order.shippingStatusHistory.add(new ShippingStatus("Giao thành công", order.orderDate, ""));
            if (order.status.equals("cancelled"))
                order.shippingStatusHistory.add(new ShippingStatus("Đã hủy đơn", order.orderDate, "Khách yêu cầu hủy hoặc shop hết hàng"));
            orders.add(order);
        }
        return orders;
    }

    private static ShippingStatus createStatus(String status, String time, String note) {
        ShippingStatus s = new ShippingStatus();
        s.status = status;
        s.time = time;
        s.note = note;
        return s;
    }

    public static Order getOrderById(int orderId) {
        for (Order order : getFakeOrders()) {
            if (order.orderId == orderId) {
                return order;
            }
        }
        return null;
    }

    // Fake notification data cho notification dialog
    public static String[] getFakeNotifications() {
        return new String[] {
                "Đơn hàng #12345 đã được giao thành công!",
                "Voucher giảm 10% cho đơn từ 200K, dùng ngay hôm nay!",
                "Shop giadungtuyetnhi vừa cập nhật trạng thái đơn hàng của bạn.",
                "Đơn #12346 đang được vận chuyển.",
                "Bạn có 1 đánh giá mới từ Shop ABC."
        };
    }

    public static List<Notification> getFakeNotificationList() {
        List<Notification> list = new ArrayList<>();
        list.add(new Notification("Custom Action", "Action Description", "1 phút trước", "system"));
        list.add(new Notification("Task assigned to you", "Aye yo, do this ting bruv", "2 giờ trước", "task"));
        list.add(new Notification("Notification triggered", "Michael is in the warehouse", "6 giờ trước", "order"));
        list.add(new Notification("Notification triggered", "Kevin spilled chili", "6 giờ trước", "order"));
        return list;
    }
}