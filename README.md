# TodoEvent - Ứng dụng Quản lý Sự kiện Cá nhân

## 📱 Mô tả

TodoEvent là ứng dụng mobile giúp người dùng quản lý các sự kiện cá nhân một cách hiệu quả. Ứng dụng hỗ trợ lưu trữ offline, đồng bộ dữ liệu và giao diện người dùng thân thiện.

## 🚀 Tính năng chính

- ✅ **Quản lý sự kiện**: Thêm, sửa, xóa sự kiện
- ✅ **Tìm kiếm & Lọc**: Tìm kiếm theo tiêu đề, lọc theo loại sự kiện
- ✅ **Lưu trữ offline**: Hoạt động ngay cả khi không có mạng
- ✅ **Đồng bộ dữ liệu**: Tự động đồng bộ khi có kết nối mạng
- ✅ **Upload hình ảnh**: Hỗ trợ tối đa 5 hình ảnh cho mỗi sự kiện
- ✅ **Giao diện hiện đại**: Sử dụng Material Design 3

## 🛠 Công nghệ sử dụng

### Android
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose v2
- **Architecture**: Clean Architecture + MVVM + Repository Pattern
- **Database**: Room Database
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Testing**: JUnit, Espresso, Compose Testing
- **Annotation Processing**: KSP (Kotlin Symbol Processing)

### Backend (Mock)
- **API**: RESTful API với Retrofit
- **Mock Server**: Local JSON storage
- **Authentication**: Bearer Token (mock)

## 📋 Yêu cầu hệ thống

- **Android**: API Level 24+ (Android 7.0+)
- **Kotlin**: 2.0.21+
- **Gradle**: 8.11.1+
- **JDK**: 11+

## 🔧 Cài đặt và Chạy

### 1. Clone repository
```bash
git clone https://github.com/xdien/todoevent.git
cd todoevent
```

### 2. Cấu hình môi trường
Tạo file `local.properties` trong thư mục gốc:
```properties
sdk.dir=/path/to/your/android/sdk
```

### 3. Sync project
```bash
./gradlew clean
./gradlew build
```

### 4. Chạy ứng dụng
```bash
# Chạy trên thiết bị/emulator đã kết nối
./gradlew installDebug

# Hoặc mở project trong Android Studio và nhấn Run
```

## 📁 Cấu trúc dự án

```
todoevent/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/xdien/todoevent/
│   │   │   │   ├── common/         # Common classes (UseCase base)
│   │   │   │   ├── data/           # Data layer (Repository, API, Database)
│   │   │   │   ├── domain/         # Domain layer (UseCases, Entities)
│   │   │   │   ├── presentation/   # UI layer (Screens, ViewModels)
│   │   │   │   └── utils/          # Utilities
│   │   │   └── res/                # Resources
│   │   └── test/                   # Unit tests
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── FEATURE_DEVELOPMENT_GUIDE.md    # Hướng dẫn phát triển tính năng
├── CLEAN_ARCHITECTURE_GUIDE.md     # Hướng dẫn Clean Architecture
└── README.md
```

### Clean Architecture Layers

#### 1. Presentation Layer (UI)
- **Jetpack Compose v2**: Screens và UI components
- **ViewModels**: Quản lý state và business logic cho UI
- **UI State**: Data classes cho UI state

#### 2. Domain Layer (Business Logic)
- **Use Cases**: Tất cả UseCases kế thừa từ `UseCase<Input, Output>`
- **Entities**: Domain entities không phụ thuộc framework
- **Repository Interfaces**: Contracts cho data access

#### 3. Data Layer (Data Access)
- **Repository Implementations**: Implement repository interfaces
- **Data Sources**: API và Database
- **Data Models**: DTOs và Database Entities

## 🔌 API Endpoints

### Events
- `GET /events` - Lấy danh sách sự kiện
- `GET /events/{id}` - Lấy chi tiết sự kiện
- `POST /events` - Tạo sự kiện mới
- `PUT /events/{id}` - Cập nhật sự kiện
- `DELETE /events/{id}` - Xóa sự kiện
- `POST /events/{id}/images` - Upload hình ảnh

### Event Types
- `GET /event-types` - Lấy danh sách loại sự kiện

### Query Parameters
- `q` - Tìm kiếm theo tiêu đề
- `typeId` - Lọc theo loại sự kiện

## 🗄 Database Schema

### Events Table
```sql
CREATE TABLE events (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    dateTime TEXT NOT NULL,
    location TEXT NOT NULL,
    typeId TEXT NOT NULL,
    images TEXT, -- JSON array
    createdAt TEXT NOT NULL,
    updatedAt TEXT NOT NULL,
    isSynced INTEGER DEFAULT 0
);
```

### Event Types Table
```sql
CREATE TABLE event_types (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    color TEXT NOT NULL
);
```

## 🧪 Testing

### Unit Tests
```bash
# Chạy tất cả unit tests
./gradlew test

# Chạy tests cho module cụ thể
./gradlew app:test
```

### Instrumented Tests
```bash
# Chạy instrumented tests
./gradlew connectedAndroidTest
```

### UI Tests
```bash
# Chạy Compose UI tests
./gradlew app:testDebugUnitTest
```

## 📱 Hướng dẫn sử dụng

### 1. Màn hình danh sách sự kiện
- **Xem sự kiện**: Danh sách hiển thị thumbnail, tiêu đề, thời gian, địa điểm
- **Tìm kiếm**: Nhập từ khóa vào ô tìm kiếm để lọc sự kiện
- **Lọc theo loại**: Chọn loại sự kiện từ chip list
- **Làm mới**: Kéo xuống để refresh dữ liệu
- **Thêm mới**: Nhấn nút "+" để tạo sự kiện mới

### 2. Màn hình chi tiết sự kiện
- **Xem chi tiết**: Hình ảnh lớn, thông tin đầy đủ
- **Chỉnh sửa**: Nhấn nút "Chỉnh sửa" để sửa sự kiện
- **Xóa**: Nhấn nút "Xóa" và xác nhận
- **Chia sẻ**: Nhấn nút "Chia sẻ" để chia sẻ sự kiện

### 3. Màn hình thêm/sửa sự kiện
- **Tiêu đề**: Bắt buộc, tối đa 100 ký tự
- **Mô tả**: Bắt buộc, tối đa 500 ký tự
- **Thời gian**: Bắt buộc, không được chọn quá khứ
- **Địa điểm**: Bắt buộc, tối đa 100 ký tự
- **Loại sự kiện**: Chọn từ danh sách có sẵn
- **Hình ảnh**: Tối đa 5 ảnh, chọn từ camera hoặc gallery

## 🔄 Offline Mode

### Cách hoạt động
1. **Khi có mạng**: Dữ liệu được đồng bộ với server
2. **Khi mất mạng**: Ứng dụng sử dụng dữ liệu cache
3. **Khi có mạng trở lại**: Tự động đồng bộ dữ liệu chưa sync

### Dữ liệu được cache
- Danh sách sự kiện
- Chi tiết sự kiện
- Loại sự kiện
- Hình ảnh (LRU cache)

## 🚀 Build & Deploy

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
# Tạo keystore (chỉ lần đầu)
keytool -genkey -v -keystore todoevent.keystore -alias todoevent -keyalg RSA -keysize 2048 -validity 10000

# Build release
./gradlew assembleRelease
```

### Signing Configuration
Thêm vào `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            keyAlias = "todoevent"
            keyPassword = "your_key_password"
            storeFile = file("todoevent.keystore")
            storePassword = "your_store_password"
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## 📊 Performance

### Optimization Techniques
- **Image Caching**: LRU cache cho hình ảnh
- **Database Indexing**: Index trên các trường tìm kiếm
- **Pagination**: Load dữ liệu theo trang
- **Background Sync**: WorkManager cho đồng bộ
- **Memory Management**: Proper lifecycle management

### Metrics
- **App Size**: ~15MB (debug), ~8MB (release)
- **Startup Time**: <2s
- **Memory Usage**: <100MB
- **Battery Impact**: Minimal

## 🐛 Troubleshooting

### Lỗi thường gặp

#### 1. Build failed
```bash
# Clean và rebuild
./gradlew clean
./gradlew build
```

#### 2. Gradle sync failed
```bash
# Invalidate caches trong Android Studio
File -> Invalidate Caches and Restart
```

#### 3. Device not detected
```bash
# Kiểm tra ADB
adb devices

# Restart ADB
adb kill-server
adb start-server
```

#### 4. Network issues
- Kiểm tra kết nối mạng
- Kiểm tra firewall settings
- Thử chế độ offline

## 🤝 Contributing

### Quy trình đóng góp
1. Fork repository
2. Tạo feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Tạo Pull Request

### Coding Standards
- Sử dụng Kotlin coding conventions
- Tuân thủ Clean Architecture với UseCase pattern
- Tất cả UseCases phải kế thừa từ `UseCase<Input, Output>`
- Viết unit tests cho business logic
- Viết UI tests cho critical flows
- Document code với KDoc
- Follow Material Design guidelines
- Sử dụng KSP thay vì kapt cho annotation processing

## 📄 License

```
MIT License

Copyright (c) 2024 xdien

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 📞 Liên hệ

- **Developer**: xdien
- **Email**: xdien@example.com
- **GitHub**: [@xdien](https://github.com/xdien)
- **Project**: [TodoEvent](https://github.com/xdien/todoevent)

## 🔄 Changelog

### Version 1.0.0 (2024)
- ✅ Initial release
- ✅ Clean Architecture implementation
- ✅ UseCase pattern với base class
- ✅ Jetpack Compose v2
- ✅ Basic CRUD operations
- ✅ Offline support
- ✅ Image upload
- ✅ Search and filter
- ✅ Material Design 3 UI
- ✅ KSP annotation processing

---

**Lưu ý**: Đây là phiên bản beta. Một số tính năng có thể thay đổi trong các phiên bản tương lai. 