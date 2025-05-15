# Roffu - Ứng dụng Thương mại Điện tử

Roffu là một ứng dụng thương mại điện tử hiện đại được phát triển bằng Kotlin và Jetpack Compose, cung cấp trải nghiệm mua sắm trực tuyến mượt mà và thân thiện với người dùng.

## Tính năng chính

### 1. Quản lý tài khoản
- Đăng ký và đăng nhập với email/Google
- Quản lý thông tin cá nhân
- Lưu trữ địa chỉ giao hàng
- Xem lịch sử đơn hàng
- Quản lý danh sách yêu thích

### 2. Trang chủ và Tìm kiếm
- Hiển thị sản phẩm nổi bật và mới nhất
- Tìm kiếm sản phẩm với gợi ý thông minh
- Lọc sản phẩm theo:
  + Danh mục
  + Thương hiệu
  + Khoảng giá
  + Sắp xếp theo tên/giá
- Lưu lịch sử tìm kiếm
- Gợi ý sản phẩm dựa trên lịch sử tìm kiếm

### 3. Chi tiết sản phẩm
- Hiển thị hình ảnh sản phẩm với gallery
- Thông tin chi tiết sản phẩm
- So sánh sản phẩm cùng danh mục
- Đánh giá và bình luận
- Thêm vào giỏ hàng
- Thêm vào danh sách yêu thích

### 4. Giỏ hàng và Thanh toán
- Quản lý giỏ hàng
- Áp dụng mã giảm giá
- Tính phí vận chuyển
- Thanh toán an toàn
- Xác nhận đơn hàng qua email

### 5. Quản lý đơn hàng
- Theo dõi trạng thái đơn hàng
- Xem chi tiết đơn hàng
- Hủy đơn hàng
- Đánh giá sản phẩm sau khi mua

### 6. Tính năng bổ sung
- Thông báo đẩy
- Chế độ tối/sáng
- Đa ngôn ngữ
- Hỗ trợ nhiều loại tiền tệ
- Tích hợp bản đồ

## Công nghệ sử dụng

### Frontend
- Kotlin
- Jetpack Compose
- Material Design 3
- Coil (xử lý hình ảnh)
- Accompanist (các thư viện bổ trợ)

### Architecture & Dependency Injection
- MVVM Architecture
- Hilt
- Kotlin Coroutines
- Flow
- StateFlow

### Networking
- Retrofit2
- OkHttp3
- Gson

### Local Storage
- DataStore
- Room Database
- SharedPreferences

### Testing
- JUnit
- Mockito
- Espresso
- Compose Testing

## Cấu trúc thư mục

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/mustfaibra/roffu/
│   │   │   ├── api/           # API interfaces và models
│   │   │   ├── di/            # Dependency injection
│   │   │   ├── models/        # Data models
│   │   │   ├── screens/       # UI screens
│   │   │   ├── ui/            # UI components
│   │   │   ├── utils/         # Utility classes
│   │   │   └── MainActivity.kt
│   │   └── res/               # Resources
│   └── test/                  # Unit tests
└── build.gradle
```

## Cài đặt và Chạy

1. Clone repository:
```bash
git clone https://github.com/your-username/roffu.git
```

2. Mở project trong Android Studio

3. Cấu hình API key:
- Tạo file `local.properties`
- Thêm API key: `API_KEY=your_api_key`

4. Build và chạy ứng dụng

## Yêu cầu hệ thống

- Android 6.0 (API level 23) trở lên
- Android Studio Arctic Fox trở lên
- Kotlin 1.8.0 trở lên
- JDK 11 trở lên

## Đóng góp

Mọi đóng góp đều được hoan nghênh! Vui lòng:

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit thay đổi (`git commit -m 'Add some AmazingFeature'`)
4. Push lên branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## Giấy phép

Dự án này được cấp phép theo MIT License - xem file [LICENSE](LICENSE) để biết thêm chi tiết.

## Liên hệ

- Email: your.email@example.com
- Website: https://your-website.com
- LinkedIn: [Your Name](https://linkedin.com/in/your-profile)
