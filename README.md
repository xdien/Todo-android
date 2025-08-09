# TodoEvent - Ứng dụng Quản lý Sự kiện Cá nhân

## 📱 Mô tả

TodoEvent là ứng dụng mobile giúp người dùng quản lý các sự kiện cá nhân một cách hiệu quả. Ứng dụng hỗ trợ lưu trữ offline, đồng bộ dữ liệu và giao diện người dùng thân thiện.

## 🎥 Demo

[![TodoEvent Demo](https://img.youtube.com/vi/vsI75Icuk6E/0.jpg)](https://www.youtube.com/watch?v=vsI75Icuk6E)

**Xem demo đầy đủ:** [TodoEvent Demo trên YouTube](https://www.youtube.com/watch?v=vsI75Icuk6E)

## 🚀 Tính năng chính

- ✅ **Quản lý sự kiện**: Thêm, sửa, xóa sự kiện
- ✅ **Tìm kiếm & Lọc**: Tìm kiếm theo tiêu đề, lọc theo loại sự kiện
- ✅ **Lưu trữ offline**: Hoạt động ngay cả khi không có mạng
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
- **Annotation Processing**: KSP (Kotlin Symbol Processing)

### Backend (Mock)
- **API**: RESTful API by fastapi
- **Mock Server**: Local database (SQLite)

## 📋 Yêu cầu hệ thống
- **Android Studio**: 2025.1.1 Patch 1
- **Android**: API Level 28+ (Android 9.0+)
- **Kotlin**: 2.0.21+
- **Gradle**: 8.11.1+
- **JDK**: 11+
- **Python**: 3.12

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
1. **Khi có mạng**: Dữ liệu được đồng bộ với server bằng cách kéo xuống để refresh
2. **Khi mất mạng**: Ứng dụng sử dụng dữ liệu cache

### Dữ liệu được cache
- Danh sách sự kiện
- Chi tiết sự kiện
- Loại sự kiện


### Optimization Techniques
- **Database Indexing**: Index trên các trường tìm kiếm
- **Memory Management**: Proper lifecycle management

> 📖 **Xem hướng dẫn chi tiết:** [SETUP_GUIDE.md](./SETUP_GUIDE.md)

---
## Vấn đề tồn đọng
- Đối với android dưới 13 thì photo picker không hoạt động như đúng yêu cầu.
- Vấn đề edit image trong màn hình chi tiết sự kiện, chưa triển khai, vì cần API xóa image để đảm bảo đồng bộ đa thiết bị.
- Một số case đi từ màn hình search sang màn hình chi tiết có thể cần cải tiến khi người dùng thục hiện xóa sự kiện thì cũng xóa ở màn kết quả search.
- Cần đổi RecyclerView sang Lazy cho đúng chuẩn jetpack compose. Nhưng do yêu cầu dùng RecyclerView. 
- Navigation trong android chưa dùng đúng cách, cần sửa lại.
- Chưa tối ưu tham số để lưu(cache) và hiển thị hình ảnh có thể chậm lag khi xử lý với số danh sách lớn.
- Chưa xử lý upload image trong service đặt biệt của android
- Danh sách RecyclerView có thể cần tối ưu thêm như preloading.

## Chiến thuật cache
- So sánh sự khác nhau giữ API và local database (sqlite) để tối ưu performance. tiến hành insert/update/delete cho local database. Resolve conflict ưu tiên thời gian cập nhật cuối gần nhất. 

## Đồng bộ sự kiện trong app 
- Sử dụng EvenBus

