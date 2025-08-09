# 🚀 Hướng dẫn chạy dự án TodoEvent (Linux)

Hướng dẫn này sẽ giúp bạn chạy dự án TodoEvent từ client đến mock server trên môi trường Linux.

## 📋 Yêu cầu hệ thống

- **Android Studio**: 2025.1.1 Patch 1
- **Android**: API Level 28+ (Android 9.0+)
- **Kotlin**: 2.0.21+
- **Gradle**: 8.11.1+
- **JDK**: 11+
- **Python**: 3.12

## 🔧 Cài đặt và Chạy

### Bước 1: Clone repository
```bash
# Clone repository từ GitHub
git clone https://github.com/xdien/todoevent.git

# Di chuyển vào thư mục dự án
cd todoevent

# Kiểm tra cấu trúc thư mục
ls -la
echo "✅ Repository đã được clone thành công!"
```

### Bước 2: Cấu hình môi trường
Tạo file `local.properties` trong thư mục gốc:
```properties
sdk.dir=/path/to/your/android/sdk
```

### Bước 3: Khởi động Mock Server
```bash
cd mock-server

# Cài đặt dependencies Python
pip install -r requirements.txt

# Khởi động server
python python_mock_server.py
```

**Mock server sẽ chạy tại:** `http://localhost:8000`

### Bước 4: Chạy Android App
```bash
# Từ thư mục gốc
cd app

# Build và cài đặt app
./gradlew installDebug

# Hoặc chạy trực tiếp
./gradlew assembleDebug
```

### Bước 5: Cấu hình API (nếu cần)
- Mở app và vào Settings
- Cập nhật API Base URL:
  - **Emulator**: `http://10.0.2.2:8000`
  - **Thiết bị thật**: `http://YOUR_IP:8000`

## ⚠️ Lưu ý quan trọng

1. **Mock server phải chạy trước** khi test app
2. **Đảm bảo thiết bị/emulator** có thể truy cập IP của máy host
3. **Database mẫu** đã có sẵn trong `mock-server/events.db`
4. **Kiểm tra firewall** nếu gặp vấn đề kết nối

## 🔍 Kiểm tra hoạt động

### Mock Server
- Truy cập: `http://localhost:8000/docs` để xem API documentation
- Kiểm tra logs trong terminal để đảm bảo server đang chạy

### Android App
- App sẽ tự động kết nối với mock server
- Kiểm tra logs trong Android Studio để xem kết nối API
- Test các chức năng: tạo, sửa, xóa sự kiện

## 🛠 Troubleshooting

### Mock Server không khởi động
```bash
# Kiểm tra Python version
python --version

# Kiểm tra dependencies
pip list

# Kiểm tra port đang sử dụng
netstat -tulpn | grep :8000
```

### App không kết nối được API
- Kiểm tra IP address của máy host: `ip addr show`
- Đảm bảo thiết bị và máy host cùng mạng
- Kiểm tra firewall settings
- Test kết nối: `ping YOUR_IP`

### Build errors
```bash
# Clean và rebuild
./gradlew clean
./gradlew build

# Sync project
./gradlew --refresh-dependencies
```

## 📱 Test trên thiết bị thật

1. **Kết nối thiết bị** với máy tính qua USB
2. **Bật USB Debugging** trên thiết bị
3. **Chạy lệnh** để cài đặt app:
   ```bash
   ./gradlew installDebug
   ```
4. **Cập nhật API URL** trong app với IP thật của máy host

## 🔄 Restart quy trình

Khi cần restart toàn bộ:
```bash
# 1. Dừng mock server (Ctrl+C)
# 2. Restart mock server
cd mock-server
python python_mock_server.py

# 3. Restart app
cd app
./gradlew installDebug
```

---

**Hỗ trợ:** Nếu gặp vấn đề, hãy kiểm tra logs và đảm bảo tất cả dependencies đã được cài đặt đúng cách.
