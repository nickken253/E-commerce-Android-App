# Roffu - Ứng dụng Thương mại Điện tử

Roffu là một ứng dụng thương mại điện tử hiện đại được phát triển bằng Kotlin và Jetpack Compose, cung cấp trải nghiệm mua sắm trực tuyến mượt mà và thân thiện với người dùng.

## Tính năng chính

### 1. Xác thực và Quản lý Tài khoản
- Đăng nhập/Đăng ký tài khoản
- Đăng nhập bằng Google
- Quản lý thông tin cá nhân
- Quên mật khẩu và đặt lại mật khẩu

### 2. Trang chủ và Tìm kiếm
- Hiển thị sản phẩm nổi bật
- Tìm kiếm sản phẩm theo tên, danh mục, thương hiệu
- Lọc sản phẩm theo giá, thương hiệu, danh mục
- Sắp xếp sản phẩm theo nhiều tiêu chí

### 3. Chi tiết Sản phẩm
- Hiển thị thông tin chi tiết sản phẩm
- Xem hình ảnh sản phẩm
- So sánh sản phẩm với các sản phẩm cùng danh mục
- Thêm vào giỏ hàng
- Chọn số lượng, màu sắc, kích thước

### 4. Giỏ hàng và Thanh toán
- Quản lý giỏ hàng
- Cập nhật số lượng sản phẩm
- Xóa sản phẩm khỏi giỏ hàng
- Thanh toán đơn hàng

### 5. Quản lý Đơn hàng
- Xem danh sách đơn hàng
- Theo dõi trạng thái đơn hàng
- Chi tiết vận chuyển
- Đánh giá sản phẩm
- Mua lại sản phẩm

### 6. Tính năng Bổ sung
- Hệ thống đề xuất sản phẩm
- Thông báo đơn hàng
- Lịch sử xem sản phẩm
- Yêu thích sản phẩm

## Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose
- **Kiến trúc**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Networking**: Retrofit2
- **Image Loading**: Coil
- **State Management**: StateFlow
- **Navigation**: Navigation Compose
- **Local Storage**: DataStore Preferences
- **Coroutines**: Xử lý bất đồng bộ

## Cấu trúc dự án

```
app/
├── api/                    # API interfaces và models
├── components/            # UI components tái sử dụng
├── models/               # Data models
├── repositories/         # Data repositories
├── screens/             # Màn hình ứng dụng
│   ├── auth/           # Màn hình xác thực
│   ├── cart/           # Màn hình giỏ hàng
│   ├── home/           # Màn hình chính
│   ├── order/          # Màn hình đơn hàng
│   └── productdetails/ # Màn hình chi tiết sản phẩm
├── sealed/             # Sealed classes
├── ui/                 # UI themes và styles
└── utils/              # Utility classes
```

## Cài đặt và Chạy

1. Clone repository:
```bash
git clone https://github.com/your-username/roffu.git
```

2. Mở project trong Android Studio

3. Cấu hình API:
- Tạo file `local.properties`
- Thêm API key: `API_KEY=your_api_key`

4. Build và chạy ứng dụng

## Yêu cầu hệ thống

- Android Studio Arctic Fox trở lên
- Android SDK 21+
- Kotlin 1.5.0+
- JDK 11+

## Đóng góp

Mọi đóng góp đều được hoan nghênh! Vui lòng tạo issue hoặc pull request để đóng góp.

## Giấy phép

Dự án này được cấp phép theo giấy phép MIT - xem file [LICENSE](LICENSE) để biết thêm chi tiết.
