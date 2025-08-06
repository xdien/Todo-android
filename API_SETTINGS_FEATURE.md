# Tính năng Cài đặt API Server

## Mô tả
Tính năng này cho phép người dùng cấu hình địa chỉ API server để kết nối đến mock server hoặc server thực tế.

## Cách sử dụng

### 1. Truy cập cài đặt API
- Mở ứng dụng TodoEvent
- Ở màn hình chính, nhấn vào icon **Settings** (⚙️) ở góc trên bên phải
- Dialog cài đặt API sẽ xuất hiện

### 2. Cấu hình địa chỉ API
- Nhập địa chỉ API server vào ô "Địa chỉ API Server"
- Ví dụ: `http://10.0.2.2:8000` (cho mock server trên localhost)
- Nhấn **Lưu** để lưu cài đặt

### 3. Lưu trữ và khôi phục
- Cài đặt được lưu vào SharedPreferences
- Khi khởi động lại app, địa chỉ API sẽ được tự động khôi phục
- Nếu chưa có cài đặt, sẽ sử dụng địa chỉ mặc định: `http://10.0.2.2:8000`

## Cấu trúc code

### Files chính:
1. **SharedPreferencesHelper.kt** - Quản lý lưu trữ cài đặt
2. **NetworkManager.kt** - Quản lý kết nối mạng và cập nhật URL động
3. **ApiSettingsDialog.kt** - UI dialog cài đặt
4. **PersonalEventComposeActivity.kt** - Màn hình chính với button cài đặt

### Tính năng:
- ✅ Lưu địa chỉ API vào SharedPreferences
- ✅ Đọc địa chỉ API khi app khởi động
- ✅ Cập nhật Retrofit instance động khi URL thay đổi
- ✅ Refresh data sau khi thay đổi URL
- ✅ UI được viết bằng Jetpack Compose
- ✅ Validation input (không cho phép URL rỗng)

## Mock Server
Để test tính năng này, bạn có thể sử dụng mock server có sẵn trong thư mục `mock-server/`:

```bash
cd mock-server
python python_mock_server.py
```

Mock server sẽ chạy trên `http://localhost:8000` và có thể truy cập từ Android emulator qua `http://10.0.2.2:8000`.

## Lưu ý
- Đảm bảo mock server đang chạy trước khi thay đổi URL
- URL phải có protocol (http:// hoặc https://)
- App sẽ tự động refresh data sau khi lưu URL mới 