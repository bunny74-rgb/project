# 📱 Ứng Dụng Quản Lý Danh Bạ & Đồng Bộ Hai Chiều (Contact Sync App)

Ứng dụng Android hiện đại được xây dựng hoàn toàn bằng **Kotlin** và **Jetpack Compose**, cho phép người dùng quản lý danh bạ lưu trữ nội bộ (Room Database) và **đồng bộ hai chiều tự động** với Danh bạ hệ thống của hệ điều hành Android thông qua `ContentProvider` và `ContentObserver`.

---

## ✨ Tính Năng Chính

- 📋 **Quản lý danh bạ nội bộ (Room DB)**:
  - **Hỗ trợ nhiều số điện thoại**: Mỗi liên hệ có thể lưu một hoặc nhiều số điện thoại.
  - **Thêm / Sửa / Xóa**: Thao tác nhất quán dữ liệu giữa ứng dụng và danh bạ hệ thống.
  - **Giao diện hiện đại**: Xây dựng bằng Jetpack Compose & Material Design 3, hỗ trợ hiển thị tràn viền (Edge-to-Edge).

- 🔄 **Đồng bộ hai chiều với Danh bạ hệ thống (System Contacts)**:
  - **Lưu liên kết ID bền vững**: Bảng `contact_sync` liên kết `localId` (UUID) với `rawContactId` (Long) của hệ thống.
  - **Chiều App → Hệ thống**: Khi thêm, sửa hoặc xóa liên hệ trong ứng dụng, danh bạ hệ thống sẽ được tạo mới, cập nhật hoặc xóa tương ứng.
  - **Chiều Hệ thống → App**: Nhập các liên hệ từ hệ thống vào ứng dụng, tự động cập nhật hoặc dọn dẹp khi danh bạ hệ thống có thay đổi.
  - **Đồng bộ tự động khi ứng dụng đang mở**: Dùng `ContentObserver` lắng nghe sự biến động của danh bạ hệ thống để kích hoạt đồng bộ tự động khi ứng dụng đang hoạt động (foreground) và tự kiểm tra lại khi quay lại ứng dụng.

---

## 🛠 Công Nghệ Sử Dụng & Phiên Bản (Tech Stack)

Thông tin phiên bản được trích xuất trực tiếp từ cấu hình Gradle của dự án (`gradle/libs.versions.toml` & `app/build.gradle.kts`):

| Thành phần | Công nghệ / Thư viện | Phiên bản |
| :--- | :--- | :--- |
| **Ngôn ngữ** | Kotlin | `2.2.10` |
| **Android Build Tool** | Android Gradle Plugin (AGP) | `9.3.0` |
| **Android SDK** | `minSdk = 24`, `targetSdk = 37`, `compileSdk = 37` | Android 15 (SDK 37) |
| **UI Toolkit** | Jetpack Compose BOM | `2026.02.01` (Material3) |
| **Điều hướng** | Navigation Compose | `2.9.8` |
| **Cơ sở dữ liệu** | Room Database (dùng KSP `2.3.10`) | `2.8.4` (Database Version 2) |
| **Kiến trúc & Bất đồng bộ** | MVVM, Coroutines, LiveData, StateFlow | Lifecycle `2.11.0` |
| **Hệ thống Android** | `ContentResolver`, `ContentObserver`, `ContactsContract` | Android Framework |

---

## 📂 Cấu Trúc Dự Án & Trách Nhiệm Các Lớp

```
com.example.myapplication/
├── MainActivity.kt                      # Activity duy nhất (Single Activity), quản lý vòng đời đồng bộ khi Mở/Dừng app.
├── component/                           # Tầng Giao diện (Compose UI)
│   ├── ContactScreen.kt                 # Màn hình chính: Hiển thị danh sách, form nhập liệu, trạng thái đồng bộ.
│   ├── ContactDetailScreen.kt           # Màn hình chi tiết: Xem thông tin, nút Sửa, Xóa.
│   ├── ContactEditScreen.kt             # Màn hình chỉnh sửa: Cập nhật tên và danh sách số điện thoại.
│   ├── ContactList.kt / ContactItem.kt  # Components danh sách và thẻ liên hệ.
│   └── ContactDialog.kt                 # AlertDialog xác nhận trước khi xóa liên hệ.
├── viewmodel/                           # Tầng ViewModel
│   └── ContactViewModel.kt              # Quản lý UI State, khởi chạy Coroutine, đăng ký ContentObserver đồng bộ.
├── repository/                          # Tầng Repository (Logic dữ liệu & Đồng bộ)
│   ├── ContactRepository.kt             # Quản lý giao dịch Room, khoá syncMutex, xử lý thuật toán đồng bộ 2 chiều.
│   ├── SystemContactReader.kt           # Truy vấn danh bạ hệ thống bằng ContentResolver & Cursor.
│   └── SystemContactWriter.kt           # Thao tác Tạo/Sửa/Xóa bản ghi hệ thống qua ContentProviderOperation.
├── data/                                # Tầng Lưu trữ Dữ liệu Nội bộ (Room DB)
│   ├── ContactEntity.kt                 # Bảng 'contacts' (id: String UUID, name: String).
│   ├── PhoneEntity.kt                   # Bảng 'phones' (id: Long auto, contactId: String FK, number: String).
│   ├── ContactWithPhones.kt             # Class ghép 1-N (@Embedded + @Relation) giữa Contact và danh sách Phone.
│   ├── ContactSyncEntity.kt             # Bảng 'contact_sync' (localId: String <-> rawContactId: Long).
│   ├── ContactDao.kt                    # Query SQL danh sách liên hệ nội bộ.
│   ├── ContactSyncDao.kt                # Truy vấn và lưu bản ghi liên kết ID đồng bộ.
│   └── ContactDatabase.kt               # RoomDatabase Version 2 với Migration_1_2 tự động khởi tạo bảng contact_sync.
├── navigation/                          # Điều hướng
│   └── AppNavigation.kt                 # Cấu hình NavHost giữa 3 màn hình ("contacts", "detail/{id}", "edit/{id}").
└── model/                               # Data Transfer Objects (SystemContact, LinkedSystemContact)
```

---

## 🗄 Mô Hình Lưu Trữ & Cơ Chế Liên Kết ID

### 1. Cách Room lưu liên hệ có nhiều số điện thoại
- **Quan hệ 1 - N**: Một `ContactEntity` chứa nhiều `PhoneEntity`.
- **Khóa ngoại (Foreign Key)**: `PhoneEntity.contactId` tham chiếu tới `ContactEntity.id` với `onDelete = ForeignKey.CASCADE`. Khi xóa `ContactEntity`, toàn bộ số điện thoại tương ứng sẽ tự động bị xóa trong Room.
- **Truy vấn**: `ContactDao.getContacts()` sử dụng `@Transaction` trả về `Flow<List<ContactWithPhones>>`.

### 2. Bảng `contact_sync` và Phân biệt ID
- `ContactSyncEntity` lưu cặp `(localId: String, rawContactId: Long)` để liên kết bền vững qua các lần đóng/mở ứng dụng.
- **Phân biệt `CONTACT_ID` vs `RAW_CONTACT_ID`**:
  - `CONTACT_ID` (Aggregate Contact ID): ID tổng hợp của hệ thống Android. Một `CONTACT_ID` có thể đại diện cho nhiều tài khoản hợp nhất.
  - `RAW_CONTACT_ID`: ID của bản ghi danh bạ gốc cụ thể thuộc về từng tài khoản. Ứng dụng liên kết trực tiếp với **`RAW_CONTACT_ID`** để thao tác chính xác vào các bản ghi dữ liệu (`StructuredName`, `Phone` trong `ContactsContract.Data`).

---

## 🔄 Luồng Đồng Bộ Chi Tiết

### 1. Luồng Thêm / Sửa / Xóa từ App
- **Thêm liên hệ**: `repository.insert` tạo `RawContact` ngoài hệ thống -> nhận lại `rawContactId` -> Lưu `ContactEntity`, các `PhoneEntity` và bản ghi liên kết `ContactSyncEntity` vào Room.
- **Sửa liên hệ**: `repository.update` kiểm tra `syncDao.find(contact.id)`. Nếu `rawContactId` còn tồn tại ngoài hệ thống -> gọi `writer.updateContact` (cập nhật lại `StructuredName` và các `Phone`). Nếu chưa có/đã bị xóa ngoài hệ thống -> tạo `RawContact` mới. Sau đó cập nhật lại Room và `contact_sync`.
- **Xóa liên hệ**: `repository.delete` tìm `rawContactId` liên kết và xóa `RawContact` ngoài hệ thống qua `resolver.delete()`, sau đó xóa `ContactEntity` và `ContactSyncEntity` trong Room.

### 2. Luồng Đồng Bộ Tự Động Hai Chiều (`synchronize()`)
1. Đọc toàn bộ bản ghi hệ thống (`reader.readForSync()`) và danh sách Room DB (`contactDao.getContacts()`).
2. **Hệ thống → Room**: Kiểm tra các liên kết trong `contact_sync`. Nếu bản ghi ngoài hệ thống đã bị xóa -> xóa tương ứng trong Room. Nếu thông tin bị thay đổi -> cập nhật vào Room.
3. **Room → Hệ thống**: Tìm các liên hệ nội bộ chưa được liên kết -> tạo mới `RawContact` ngoài hệ thống và lưu bản ghi `contact_sync`.
4. **Nhập mới từ Hệ thống**: Tìm các liên hệ có số điện thoại trên hệ thống chưa có trong bảng `contact_sync` -> nhập vào Room và lưu bản ghi liên kết.

---

## 🔒 Đồng Bộ Tự Động, ContentObserver & Quyền Truy Cập

- **Vai trò của `ContentObserver`**: Lắng nghe sự thay đổi dữ liệu từ URI `ContactsContract.AUTHORITY_URI`. Khi phát hiện sự kiện biến động từ danh bạ hệ thống (ví dụ người dùng sửa danh bạ bằng ứng dụng Contacts gốc), `ContentObserver` sẽ kích hoạt hàm `repository.synchronize()` (có gom nhóm sự kiện sau 400ms để tránh quá tải).
- **Phạm vi hoạt động**:
  - Đồng bộ tự động hoạt động **khi ứng dụng đang mở / ở foreground** và tự kiểm tra lại mỗi khi người dùng quay lại ứng dụng (`onResume`).
  - ⚠️ **Ứng dụng KHÔNG đồng bộ nền khi tiến trình (process) đã bị tắt hoàn toàn** (do chưa tích hợp WorkManager).
- **An toàn dữ liệu**: Khi truy vấn hệ thống gặp lỗi hoặc bị từ chối quyền, ứng dụng **KHÔNG ĐƯỢC coi là danh bạ rỗng hoặc đã bị xóa**, dữ liệu nội bộ trong Room DB sẽ được giữ nguyên an toàn.
- **Quyền truy cập**:
  - `android.permission.READ_CONTACTS`: Đọc danh bạ hệ thống.
  - `android.permission.WRITE_CONTACTS`: Thao tác tạo/sửa/xóa danh bạ hệ thống.
  - Các quyền được khai báo trong `AndroidManifest.xml` và xin quyền runtime linh hoạt khi người dùng thao tác.

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 1. Yêu cầu môi trường
- **Android Studio**: Ladybug (2024.2.1) hoặc mới hơn.
- **JDK**: Java 11 trở lên.
- **Android SDK**: `minSdk = 24`, `targetSdk = 37`.

### 2. Các bước khởi chạy
1. Clone dự án về máy tính:
   ```bash
   git clone https://github.com/bunny74-rgb/project.git
   ```
2. Mở dự án trong Android Studio (`File` -> `Open`).
3. Đợi Gradle thực hiện **Sync Project with Gradle Files**.
4. Chọn thiết bị ảo (Emulator) hoặc thiết bị thật đã bật chế độ USB Debugging.
5. Nhấn nút **Run** (`Shift + F10`) để biên dịch và chạy ứng dụng.

---

## 🧪 Hướng Dẫn Kiểm Thử & Trạng Thái Hiện Tại

### Trạng Thái Kiểm Thử Dự Án
- **Mã nguồn**: Đã triển khai đầy đủ các cấu trúc mã nguồn cho lưu trữ Room, Repository, ViewModel, ContentProvider Writer/Reader và ContentObserver.
- **Biên dịch (Build)**: **Đã biên dịch thành công 100%** (`app:assembleDebug` - Build finished successfully).
- **Phân tích tĩnh (Static Analysis)**: Đã qua bước kiểm tra cú pháp và luồng mã nguồn.
- **Thử nghiệm trên thiết bị**: Chưa thực hiện kiểm thử thủ công trực tiếp trên thiết bị thật/emulator (cần kết nối thiết bị để chạy kiểm thử UI thủ công).

### Kịch Bản Kiểm Thử Khuyến Nghị
1. **Thêm / Sửa / Xóa từ App**: Thao tác trên app và mở ứng dụng Danh bạ gốc của máy để kiểm tra thông tin được tạo, cập nhật hoặc xóa tương ứng.
2. **Sửa / Xóa từ danh bạ máy**: Mở app Danh bạ gốc sửa hoặc xóa liên hệ, sau đó quay lại app để kiểm tra tính năng đồng bộ tự động.
3. **Một người có nhiều số điện thoại**: Nhập danh sách số điện thoại phân cách bằng dấu phẩy (ví dụ: `0901111111, 0902222222`) và kiểm tra cả 2 bên.
4. **Hai người trùng tên**: Tạo 2 liên hệ cùng tên nhưng khác số điện thoại để kiểm tra tính độc lập của ID.
5. **Khởi động lại ứng dụng**: Đóng hoàn toàn app và mở lại để xác nhận tính bền vững của cơ sở dữ liệu Room và bảng liên kết `contact_sync`.
6. **Kiểm tra phân quyền**: Tắt quyền Danh bạ trong Cài đặt Android và kiểm tra ứng dụng xử lý thông báo lỗi an toàn, không bị văng app (crash).

---

## 📝 Giấy Phép (License)

Dự án được phát triển phục vụ mục đích học tập và nghiên cứu.
