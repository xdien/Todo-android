# API Specification - TodoEvent

## 📋 Tổng quan

Tài liệu này mô tả chi tiết các API endpoints cho ứng dụng TodoEvent, tuân thủ chuẩn RESTful API và MCP (Model Context Protocol) standard.

## 🔗 Base URL

```
Development: https://dev-api.todoevent.com/v1
Production: https://api.todoevent.com/v1
Mock Server: http://localhost:8080/v1
```

## 🔐 Authentication

### Bearer Token
Tất cả API calls (trừ public endpoints) yêu cầu Bearer token trong Authorization header:

```
Authorization: Bearer <access_token>
```

### Token Format
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "refresh_token_here"
}
```

## 📊 Response Format

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data here
  },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "title",
        "message": "Title is required"
      }
    ]
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 🎯 Events API

### 1. GET /events

**Mô tả**: Lấy danh sách sự kiện với hỗ trợ tìm kiếm và lọc.

**Endpoint**: `GET /events`

**Query Parameters**:
- `q` (string, optional): Từ khóa tìm kiếm theo tiêu đề
- `typeId` (string, optional): Lọc theo ID loại sự kiện
- `page` (integer, optional): Số trang (default: 1)
- `limit` (integer, optional): Số lượng item per page (default: 20, max: 100)
- `sortBy` (string, optional): Sắp xếp theo field (dateTime, title, createdAt)
- `sortOrder` (string, optional): Thứ tự sắp xếp (asc, desc, default: desc)

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "data": {
    "events": [
      {
        "id": "event_123",
        "title": "Họp nhóm dự án",
        "description": "Thảo luận về tiến độ dự án mới",
        "dateTime": "2024-01-15T14:00:00Z",
        "location": "Phòng họp A, Tầng 3",
        "typeId": "work",
        "typeName": "Công việc",
        "typeColor": "#FF5722",
        "images": [
          "https://api.todoevent.com/images/event_123_1.jpg",
          "https://api.todoevent.com/images/event_123_2.jpg"
        ],
        "createdAt": "2024-01-01T10:00:00Z",
        "updatedAt": "2024-01-01T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 150,
      "totalPages": 8,
      "hasNext": true,
      "hasPrev": false
    }
  },
  "message": "Events retrieved successfully"
}
```

**Error Codes**:
- `401`: Unauthorized - Token không hợp lệ
- `400`: Bad Request - Query parameters không hợp lệ
- `500`: Internal Server Error - Lỗi server

### 2. GET /events/{id}

**Mô tả**: Lấy chi tiết một sự kiện cụ thể.

**Endpoint**: `GET /events/{id}`

**Path Parameters**:
- `id` (string, required): ID của sự kiện

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "event_123",
    "title": "Họp nhóm dự án",
    "description": "Thảo luận về tiến độ dự án mới",
    "dateTime": "2024-01-15T14:00:00Z",
    "location": "Phòng họp A, Tầng 3",
    "typeId": "work",
    "typeName": "Công việc",
    "typeColor": "#FF5722",
    "images": [
      {
        "id": "img_1",
        "url": "https://api.todoevent.com/images/event_123_1.jpg",
        "thumbnail": "https://api.todoevent.com/images/event_123_1_thumb.jpg",
        "uploadedAt": "2024-01-01T10:00:00Z"
      }
    ],
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T10:00:00Z"
  },
  "message": "Event details retrieved successfully"
}
```

**Error Codes**:
- `401`: Unauthorized
- `404`: Not Found - Sự kiện không tồn tại
- `500`: Internal Server Error

### 3. POST /events

**Mô tả**: Tạo sự kiện mới.

**Endpoint**: `POST /events`

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "title": "Họp nhóm dự án",
  "description": "Thảo luận về tiến độ dự án mới",
  "dateTime": "2024-01-15T14:00:00Z",
  "location": "Phòng họp A, Tầng 3",
  "typeId": "work"
}
```

**Validation Rules**:
- `title`: Required, max 100 characters
- `description`: Required, max 500 characters
- `dateTime`: Required, ISO 8601 format, not in past
- `location`: Required, max 100 characters
- `typeId`: Required, must exist in event types

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "event_123",
    "title": "Họp nhóm dự án",
    "description": "Thảo luận về tiến độ dự án mới",
    "dateTime": "2024-01-15T14:00:00Z",
    "location": "Phòng họp A, Tầng 3",
    "typeId": "work",
    "typeName": "Công việc",
    "typeColor": "#FF5722",
    "images": [],
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T10:00:00Z"
  },
  "message": "Event created successfully"
}
```

**Error Codes**:
- `401`: Unauthorized
- `400`: Bad Request - Validation errors
- `422`: Unprocessable Entity - Business logic errors
- `500`: Internal Server Error

### 4. PUT /events/{id}

**Mô tả**: Cập nhật thông tin sự kiện.

**Endpoint**: `PUT /events/{id}`

**Path Parameters**:
- `id` (string, required): ID của sự kiện

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "title": "Họp nhóm dự án - Cập nhật",
  "description": "Thảo luận về tiến độ dự án mới - Cập nhật",
  "dateTime": "2024-01-15T15:00:00Z",
  "location": "Phòng họp B, Tầng 4",
  "typeId": "work"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "event_123",
    "title": "Họp nhóm dự án - Cập nhật",
    "description": "Thảo luận về tiến độ dự án mới - Cập nhật",
    "dateTime": "2024-01-15T15:00:00Z",
    "location": "Phòng họp B, Tầng 4",
    "typeId": "work",
    "typeName": "Công việc",
    "typeColor": "#FF5722",
    "images": [],
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T11:00:00Z"
  },
  "message": "Event updated successfully"
}
```

**Error Codes**:
- `401`: Unauthorized
- `404`: Not Found
- `400`: Bad Request - Validation errors
- `422`: Unprocessable Entity
- `500`: Internal Server Error

### 5. DELETE /events/{id}

**Mô tả**: Xóa sự kiện.

**Endpoint**: `DELETE /events/{id}`

**Path Parameters**:
- `id` (string, required): ID của sự kiện

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "data": null,
  "message": "Event deleted successfully"
}
```

**Error Codes**:
- `401`: Unauthorized
- `404`: Not Found
- `403`: Forbidden - Không có quyền xóa
- `500`: Internal Server Error

### 6. POST /events/{id}/images

**Mô tả**: Upload hình ảnh cho sự kiện.

**Endpoint**: `POST /events/{id}/images`

**Path Parameters**:
- `id` (string, required): ID của sự kiện

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: multipart/form-data
```

**Request Body**:
```
Form Data:
- images[]: File (required, max 5 files, max 5MB each)
```

**File Validation**:
- Allowed formats: JPG, JPEG, PNG, GIF
- Max file size: 5MB per file
- Max files: 5 per event
- Min resolution: 200x200 pixels

**Response**:
```json
{
  "success": true,
  "data": {
    "uploadedImages": [
      {
        "id": "img_1",
        "url": "https://api.todoevent.com/images/event_123_1.jpg",
        "thumbnail": "https://api.todoevent.com/images/event_123_1_thumb.jpg",
        "size": 1024000,
        "width": 1920,
        "height": 1080,
        "uploadedAt": "2024-01-01T10:00:00Z"
      }
    ],
    "totalImages": 1
  },
  "message": "Images uploaded successfully"
}
```

**Error Codes**:
- `401`: Unauthorized
- `404`: Not Found - Event không tồn tại
- `400`: Bad Request - File validation errors
- `413`: Payload Too Large - File quá lớn
- `500`: Internal Server Error

## 🏷 Event Types API

### 1. GET /event-types

**Mô tả**: Lấy danh sách loại sự kiện.

**Endpoint**: `GET /event-types`

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "work",
      "name": "Công việc",
      "color": "#FF5722",
      "icon": "work",
      "description": "Các sự kiện liên quan đến công việc",
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00Z"
    },
    {
      "id": "personal",
      "name": "Cá nhân",
      "color": "#2196F3",
      "icon": "person",
      "description": "Các sự kiện cá nhân",
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00Z"
    },
    {
      "id": "family",
      "name": "Gia đình",
      "color": "#4CAF50",
      "icon": "family_restroom",
      "description": "Các sự kiện gia đình",
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00Z"
    },
    {
      "id": "entertainment",
      "name": "Giải trí",
      "color": "#9C27B0",
      "icon": "celebration",
      "description": "Các sự kiện giải trí",
      "isActive": true,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ],
  "message": "Event types retrieved successfully"
}
```

## 🔍 Search API

### 1. GET /search

**Mô tả**: Tìm kiếm nâng cao với nhiều tiêu chí.

**Endpoint**: `GET /search`

**Query Parameters**:
- `q` (string, required): Từ khóa tìm kiếm
- `typeIds[]` (array, optional): Lọc theo nhiều loại sự kiện
- `dateFrom` (string, optional): Từ ngày (ISO 8601)
- `dateTo` (string, optional): Đến ngày (ISO 8601)
- `location` (string, optional): Tìm kiếm theo địa điểm
- `page` (integer, optional): Số trang
- `limit` (integer, optional): Số lượng item per page

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "data": {
    "events": [
      {
        "id": "event_123",
        "title": "Họp nhóm dự án",
        "description": "Thảo luận về tiến độ dự án mới",
        "dateTime": "2024-01-15T14:00:00Z",
        "location": "Phòng họp A, Tầng 3",
        "typeId": "work",
        "typeName": "Công việc",
        "typeColor": "#FF5722",
        "relevanceScore": 0.95,
        "highlightedTitle": "Họp nhóm <mark>dự án</mark>",
        "highlightedDescription": "Thảo luận về tiến độ <mark>dự án</mark> mới"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 25,
      "totalPages": 2,
      "hasNext": true,
      "hasPrev": false
    },
    "facets": {
      "types": [
        {
          "id": "work",
          "name": "Công việc",
          "count": 15
        },
        {
          "id": "personal",
          "name": "Cá nhân",
          "count": 10
        }
      ],
      "dateRanges": [
        {
          "range": "today",
          "count": 5
        },
        {
          "range": "this_week",
          "count": 12
        },
        {
          "range": "this_month",
          "count": 25
        }
      ]
    }
  },
  "message": "Search completed successfully"
}
```

## 📊 Analytics API

### 1. GET /analytics/events

**Mô tả**: Thống kê về sự kiện.

**Endpoint**: `GET /analytics/events`

**Query Parameters**:
- `period` (string, optional): Khoảng thời gian (day, week, month, year)
- `typeId` (string, optional): Lọc theo loại sự kiện
- `dateFrom` (string, optional): Từ ngày
- `dateTo` (string, optional): Đến ngày

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Response**:
```json
{
  "success": true,
  "data": {
    "summary": {
      "totalEvents": 150,
      "completedEvents": 120,
      "upcomingEvents": 30,
      "cancelledEvents": 5
    },
    "byType": [
      {
        "typeId": "work",
        "typeName": "Công việc",
        "count": 80,
        "percentage": 53.3
      },
      {
        "typeId": "personal",
        "typeName": "Cá nhân",
        "count": 45,
        "percentage": 30.0
      }
    ],
    "byMonth": [
      {
        "month": "2024-01",
        "count": 25
      },
      {
        "month": "2024-02",
        "count": 30
      }
    ],
    "trends": {
      "growthRate": 15.5,
      "mostActiveDay": "Wednesday",
      "mostActiveHour": 14
    }
  },
  "message": "Analytics retrieved successfully"
}
```

## 🔄 Sync API

### 1. POST /sync

**Mô tả**: Đồng bộ dữ liệu offline với server.

**Endpoint**: `POST /sync`

**Headers**:
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "lastSyncTimestamp": "2024-01-01T10:00:00Z",
  "offlineEvents": [
    {
      "localId": "local_123",
      "title": "Sự kiện offline",
      "description": "Sự kiện được tạo khi offline",
      "dateTime": "2024-01-15T14:00:00Z",
      "location": "Văn phòng",
      "typeId": "work",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ],
  "deletedEventIds": ["event_456"]
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "syncedEvents": [
      {
        "localId": "local_123",
        "serverId": "event_789",
        "status": "created"
      }
    ],
    "conflicts": [],
    "serverUpdates": [
      {
        "id": "event_999",
        "action": "updated",
        "timestamp": "2024-01-01T11:00:00Z"
      }
    ],
    "lastSyncTimestamp": "2024-01-01T12:00:00Z"
  },
  "message": "Sync completed successfully"
}
```

## 🚨 Error Codes

### Standard Error Codes
- `400`: Bad Request - Request không hợp lệ
- `401`: Unauthorized - Chưa xác thực hoặc token hết hạn
- `403`: Forbidden - Không có quyền truy cập
- `404`: Not Found - Resource không tồn tại
- `409`: Conflict - Xung đột dữ liệu
- `422`: Unprocessable Entity - Validation errors
- `429`: Too Many Requests - Rate limit exceeded
- `500`: Internal Server Error - Lỗi server
- `503`: Service Unavailable - Service không khả dụng

### Custom Error Codes
- `EVENT_NOT_FOUND`: Sự kiện không tồn tại
- `INVALID_DATE_TIME`: Thời gian không hợp lệ
- `FILE_TOO_LARGE`: File quá lớn
- `INVALID_FILE_TYPE`: Loại file không được hỗ trợ
- `MAX_IMAGES_EXCEEDED`: Vượt quá số lượng hình ảnh cho phép
- `SYNC_CONFLICT`: Xung đột khi đồng bộ

## 📈 Rate Limiting

### Limits
- **GET requests**: 1000 requests/hour
- **POST/PUT/DELETE requests**: 100 requests/hour
- **File uploads**: 50 uploads/hour

### Headers
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640995200
```

## 🔐 Security

### CORS Policy
```
Access-Control-Allow-Origin: https://todoevent.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type
Access-Control-Max-Age: 86400
```

### Content Security Policy
```
Content-Security-Policy: default-src 'self'; img-src 'self' data: https:; script-src 'self' 'unsafe-inline'
```

## 📝 API Versioning

### Version Strategy
- URL versioning: `/v1/events`
- Header versioning: `Accept: application/vnd.todoevent.v1+json`

### Deprecation Policy
- Deprecated APIs will be marked with `X-API-Deprecated` header
- 6 months notice before removal
- Migration guides provided

## 🧪 Testing

### Test Endpoints
```
GET /test/health - Health check
GET /test/events - Mock events data
POST /test/reset - Reset test data
```

### Test Data
```json
{
  "testEvents": [
    {
      "id": "test_event_1",
      "title": "Test Event 1",
      "description": "This is a test event",
      "dateTime": "2024-12-31T23:59:59Z",
      "location": "Test Location",
      "typeId": "work"
    }
  ]
}
```

---

## 📞 Support

- **API Documentation**: https://docs.todoevent.com
- **Developer Portal**: https://developers.todoevent.com
- **Support Email**: api-support@todoevent.com
- **Status Page**: https://status.todoevent.com

## 📄 License

This API specification is licensed under the MIT License. 