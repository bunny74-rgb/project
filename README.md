# 📱 Ứng Dụng Quản Lý Danh Bạ (Contact Management App)

Ứng dụng Android hiện đại được xây dựng hoàn toàn bằng **Kotlin** và **Jetpack Compose**, cho phép người dùng quản lý danh bạ cá nhân lưu trữ nội bộ (Room Database) cũng như tương tác, đồng bộ dữ liệu với danh bạ hệ thống trên thiết bị Android.

---

## ✨ Tính Năng Chính

- 📋 **Quản lý danh bạ nội bộ**:
  - **Thêm mới**: Tạo liên hệ mới kèm một hoặc nhiều số điện thoại (phân tách nhanh bằng dấu phẩy).
  - **Danh sách & Chi tiết**: Hiển thị danh sách liên hệ trực quan và xem chi tiết đầy đủ từng liên hệ.
  - **Chỉnh sửa**: Cập nhật tên và danh sách số điện thoại linh hoạt.
  - **Xóa liên hệ**: Xóa liên hệ an toàn khỏi cơ sở dữ liệu ứng dụng.

- 📲 **Tương tác với Danh bạ hệ thống**:
  - **Đọc danh bạ thiết bị**: Đọc các liên hệ sẵn có từ điện thoại (yêu cầu cấp quyền `READ_CONTACTS`).
  - **Xuất sang hệ thống**: Sao chép liên hệ từ ứng dụng sang danh bạ gốc của điện thoại (yêu cầu cấp quyền `WRITE_CONTACTS`).

- 🎨 **Giao diện & Trải nghiệm người dùng**:
  - Thiết kế theo chuẩn **Material Design 3**.
  - Hỗ trợ hiển thị **Edge-to-Edge** tràn viền hiện đại.
  - Luồng chuyển màn hình mượt mà với **Navigation Compose**.

---

## 🛠 Công Nghệ Sử Dụng (Tech Stack)

| Thành phần | Công nghệ / Thư viện |
| :--- | :--- |
| **Ngôn ngữ** | Kotlin |
| **Giao diện (UI)** | Jetpack Compose, Material3 |
| **Kiến trúc** | MVVM (Model - View - ViewModel) + Repository Pattern |
| **Cơ sở dữ liệu** | Room Database (KSP) |
| **Bất đồng bộ** | Kotlin Coroutines, LiveData, Compose State |
| **Điều hướng** | Jetpack Navigation Compose |
| **Android System APIs** | `ContentResolver`, `ContactsContract`, Activity Result API (Runtime Permissions) |

---

## 📂 Cấu Trúc Dự Án

```
com.example.myapplication/
├── data/                      # Tầng dữ liệu Room DB
│   ├── ContactEntity.kt       # Thực thể lưu thông tin liên hệ
│   ├── PhoneEntity.kt         # Thực thể lưu số điện thoại (Quan hệ 1-N)
│   ├── ContactWithPhones.kt   # Class quan hệ giữa Contact và các Phone
│   ├── ContactDao.kt          # Data Access Object cho Room
│   └── ContactDatabase.kt     # Khởi tạo Room Database
├── repository/                # Tầng quản lý dữ liệu
│   ├── ContactRepository.kt   # Xử lý dữ liệu Room
│   ├── SystemContactReader.kt # Đọc danh bạ thiết bị qua ContentResolver
│   └── SystemContactWriter.kt # Ghi danh bạ thiết bị qua ContentResolver
├── viewmodel/                 # Tầng ViewModel
│   └── ContactViewModel.kt    # Quản lý trạng thái UI và logic nghiệp vụ
├── component/                 # Các Composable UI screens & components
│   ├── ContactScreen.kt       # Màn hình chính (Danh sách + Nhập liệu)
│   ├── ContactDetailScreen.kt # Màn hình chi tiết liên hệ
│   ├── ContactEditScreen.kt   # Màn hình chỉnh sửa liên hệ
│   ├── ContactList.kt         # Component danh sách liên hệ
│   ├── ContactItem.kt         # Component hiển thị từng thẻ liên hệ
│   └── ContactDialog.kt       # Dialog thông báo / xác nhận
├── navigation/                # Điều hướng ứng dụng
│   └── AppNavigation.kt       # Cấu hình NavHost & NavGraph
├── model/                     # Data Transfer Objects
│   └── SystemContact.kt       # Model chứa thông tin danh bạ hệ thống
└── ui/theme/                  # Cấu hình giao diện (Color, Theme, Type)
```

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 1. Yêu cầu môi trường
- **Android Studio**: Ladybug (2024.2.1) trở lên.
- **JDK**: Java 11+.
- **Android SDK**: `minSdk = 24`, `targetSdk = 37`.

### 2. Các bước khởi chạy
1. Mở dự án trong Android Studio.
2. Đợi Gradle thực hiện **Sync Project with Gradle Files**.
3. Chọn thiết bị ảo (Emulator) hoặc thiết bị thật (chế độ Developer Mode & USB Debugging).
4. Nhấn nút **Run** (`Shift + F10`) để biên dịch và chạy ứng dụng.

---

## 🔒 Quyền Truy Cập (Permissions)

Ứng dụng sử dụng các quyền sau (khai báo trong `AndroidManifest.xml` và xin quyền linh hoạt khi chạy):
- `android.permission.READ_CONTACTS`: Dùng để đọc danh bạ thiết bị.
- `android.permission.WRITE_CONTACTS`: Dùng để thêm liên hệ mới vào danh bạ thiết bị.

---

## 📝 Giấy Phép (License)

Dự án được phát triển phục vụ mục đích học tập và tham khảo.
