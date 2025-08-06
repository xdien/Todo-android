# Mock Events API Server

Mock server cho ứng dụng TodoEvent, cung cấp các API endpoints để test ứng dụng Android.

## Yêu cầu hệ thống

- **OS**: Linux
- **Python**: 3.7+ 
- **pip**: Package manager cho Python

## Cài đặt và chạy

### 1. Cài đặt dependencies

```bash
cd mock-server
pip install -r requirements.txt
```

### 2. Chạy server

```bash
python python_mock_server.py
```

Server sẽ khởi động tại: **http://localhost:5000**

## API Endpoints

- `GET /events` - Lấy danh sách sự kiện
- `GET /events/<id>` - Chi tiết sự kiện
- `POST /events` - Tạo sự kiện mới
- `PUT /events/<id>` - Cập nhật sự kiện
- `DELETE /events/<id>` - Xóa sự kiện
- `POST /events/<id>/images` - Upload hình ảnh
- `GET /event-types` - Loại sự kiện

## Dừng server

Nhấn `Ctrl + C` để dừng server. 