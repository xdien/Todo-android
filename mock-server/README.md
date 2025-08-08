# Mock Events API Server

## Overview
Mock server cho TodoEvent app sử dụng Flask và SQLite. Server này cung cấp RESTful API endpoints cho việc quản lý events, images và event types.

## 🎯 Chuẩn Naming Convention

### Server Response (snake_case)
Tất cả response từ server đều sử dụng `snake_case`:
```json
{
  "success": true,
  "data": {
    "events": [
      {
        "id": 1,
        "title": "Event Title",
        "description": "Event Description",
        "event_type_id": 1,
        "start_date": "2024-12-15T09:00:00",
        "location": "Event Location",
        "created_at": "2024-11-01T10:00:00",
        "updated_at": null,
        "images": [
          {
            "id": 1,
            "event_id": 1,
            "original_name": "image.jpg",
            "filename": "unique_filename.jpg",
            "file_path": "uploads/unique_filename.jpg",
            "file_size": 123456,
            "uploaded_at": "2024-11-01T10:00:00"
          }
        ]
      }
    ],
    "total": 1,
    "filters": {
      "keyword": null,
      "event_type_id": null
    }
  },
  "message": "Success message"
}
```

### Client Request (camelCase)
Tất cả request từ client đều sử dụng `camelCase`:
```json
{
  "title": "Event Title",
  "description": "Event Description", 
  "eventTypeId": 1,
  "startDate": "2024-12-15T09:00:00",
  "location": "Event Location"
}
```

### Automatic Mapping
Server tự động xử lý mapping giữa `camelCase` (request) và `snake_case` (database/response):
- ✅ `eventTypeId` ↔ `event_type_id`
- ✅ `startDate` ↔ `start_date`
- ✅ `createdAt` ↔ `created_at`
- ✅ `updatedAt` ↔ `updated_at`
- ✅ `originalName` ↔ `original_name`
- ✅ `filePath` ↔ `file_path`
- ✅ `fileSize` ↔ `file_size`
- ✅ `uploadedAt` ↔ `uploaded_at`

## 🚀 API Endpoints

### Events
- `GET /events` - Lấy danh sách events (hỗ trợ filter)
- `GET /events/<id>` - Lấy chi tiết event
- `POST /events` - Tạo event mới
- `PUT /events/<id>` - Cập nhật event
- `DELETE /events/<id>` - Xóa event

### Images
- `POST /events/<id>/images` - Upload images cho event

### Event Types
- `GET /event-types` - Lấy danh sách event types

### Debug
- `GET /debug/events` - Debug database state

## 🔧 Cấu hình

### Database
- SQLite database: `events.db`
- Tự động tạo tables và sample data khi khởi động

### File Upload
- Thư mục upload: `uploads/`
- Hỗ trợ: PNG, JPG, JPEG, GIF, WEBP
- Max file size: Không giới hạn (có thể cấu hình)

### CORS
- Enabled cho tất cả domains
- Hỗ trợ cross-origin requests

## 📊 Database Schema

### Events Table
```sql
CREATE TABLE events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    type_id INTEGER NOT NULL,
    start_date TEXT NOT NULL,
    location TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT,
    FOREIGN KEY (type_id) REFERENCES event_types (id)
);
```

### Images Table
```sql
CREATE TABLE images (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id INTEGER NOT NULL,
    original_name TEXT NOT NULL,
    filename TEXT NOT NULL,
    file_path TEXT NOT NULL,
    file_size INTEGER,
    uploaded_at TEXT NOT NULL,
    FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);
```

### Event Types Table
```sql
CREATE TABLE event_types (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);
```

## 🏃‍♂️ Chạy Server

```bash
# Cài đặt dependencies
pip install -r requirements.txt

# Chạy server
python python_mock_server.py
```

Server sẽ chạy tại: `http://localhost:5000`

## 📝 Logging

- Log file: `server.log`
- Console logging: Enabled
- Request/Response logging: Detailed
- Error logging: Comprehensive

## 🔄 Migration Notes

### Version 2.0.0 - Snake Case Standardization
- ✅ Tất cả response sử dụng `snake_case`
- ✅ Tất cả request hỗ trợ `camelCase`
- ✅ Automatic mapping giữa `camelCase` và `snake_case`
- ✅ Consistent field naming across all endpoints
- ✅ Backward compatibility với existing clients

### Breaking Changes
- Response fields đã thay đổi từ `camelCase` sang `snake_case`
- Client cần cập nhật để xử lý `snake_case` response
- Gson configuration cần `LOWER_CASE_WITH_UNDERSCORES` policy 