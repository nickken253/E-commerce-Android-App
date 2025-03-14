![](task.jfif)

# Luồng Payment:
- Màn hình checkout: bao gồm:
    - Thông tin giỏ hàng: từng sản phẩm, số lượng từng cái, giá từng cái
    - địa chỉ giao hàng: không có mapview, cho chọn thành phố, quận huyện, xã, địa chỉ cụ thể, số điện thoại người nhận
    - chọn phương thức thanh toán: COD hoặc thanh toán bằng 
- Màn hình chọn địa chỉ giao hàng
- Màn hình chọn phương thức thanh toán:
  - API thẻ sẽ do mình cung cấp, do mình tạo và check luôn
  - Cho phép user tạo thẻ mới ở chỗ này
  - Thêm tiền vào các thẻ có hai cách: từ tk này chuyển qua tk kia hoặc mình sẽ vào admin của backend
- Màn hình success/failed

# Lá thư cho người đến sau
- Phần payment thầy HA bảo code API VnPay khó hơn code backend. nên là ai làm thì làm đi nhé.
- Phần checklist thầy bắt phải có thêm trường giá tiền + filter sắp xếp các thứ