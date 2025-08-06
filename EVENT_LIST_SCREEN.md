# Màn hình Danh sách Sự kiện

## Tổng quan
Màn hình danh sách sự kiện được triển khai với LazyColumn và refresh button để hiển thị các sự kiện với thông tin đầy đủ bao gồm:
- Ảnh thumbnail
- Tiêu đề sự kiện
- Thời gian diễn ra
- Địa điểm

## Tính năng chính

### 1. Refresh Functionality
- Nút refresh trên TopAppBar để làm mới danh sách sự kiện
- Hiển thị indicator loading khi đang tải dữ liệu
- Tự động cập nhật trạng thái loading

### 2. EventCard Component
- Hiển thị ảnh thumbnail với Coil image loading
- Thông tin sự kiện được sắp xếp rõ ràng
- Hỗ trợ click để xem chi tiết sự kiện
- Responsive design với Material Design 3

### 3. Empty State
- Hiển thị khi không có sự kiện nào
- Hướng dẫn người dùng kéo xuống để làm mới

## Cấu trúc dữ liệu

### TodoEntity (Cập nhật)
```kotlin
data class TodoEntity(
    val id: Long = 0,
    val title: String,
    val description: String?,
    val thumbnailUrl: String? = null,    // URL ảnh thumbnail
    val eventTime: Long? = null,         // Timestamp thời gian sự kiện
    val location: String? = null,        // Địa điểm sự kiện
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

## Dependencies đã thêm

### Coil (Image Loading)
```kotlin
implementation("io.coil-kt:coil-compose:2.5.0")
```

## Cách sử dụng

### 1. Import màn hình
```kotlin
import com.xdien.todoevent.ui.screens.EventListScreen
```

### 2. Sử dụng trong Composable
```kotlin
@Composable
fun MyScreen() {
    EventListScreen(
        onEventClick = { event ->
            // Xử lý khi click vào sự kiện
        }
    )
}
```

### 3. Tùy chỉnh với modifier
```kotlin
EventListScreen(
    modifier = Modifier.padding(16.dp),
    onEventClick = { event ->
        // Xử lý click
    }
)
```

## Tính năng nâng cao

### 1. Refresh Button
- Nút refresh trên TopAppBar để làm mới danh sách
- Tự động gọi `viewModel.loadTodos()`
- Hiển thị loading indicator

### 2. Image Loading
- Sử dụng Coil để tải ảnh từ URL
- Hỗ trợ crossfade animation
- Fallback image khi không tải được ảnh

### 3. Date Formatting
- Tự động format thời gian theo định dạng dd/MM/yyyy HH:mm
- Hỗ trợ locale Việt Nam

## Dữ liệu mẫu
Màn hình được cấu hình với 4 sự kiện mẫu:
1. Hội thảo Công nghệ 2024
2. Workshop Lập trình Android
3. Meetup Cộng đồng Developer
4. Hackathon 2024

## Permissions
Đã thêm permission INTERNET vào AndroidManifest.xml để tải ảnh từ internet:
```xml
<uses-permission android:name="android.permission.INTERNET" />
``` 