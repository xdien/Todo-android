from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import datetime
import os
from werkzeug.utils import secure_filename
import uuid
import json
import sqlite3
import contextlib
from typing import List, Dict, Optional, Any

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Configuration
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'webp'}
DATABASE_PATH = 'events.db'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Create upload directory if it doesn't exist
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def get_db_connection():
    """Get database connection"""
    conn = sqlite3.connect(DATABASE_PATH)
    conn.row_factory = sqlite3.Row  # This enables column access by name
    return conn

@contextlib.contextmanager
def get_db():
    """Context manager for database operations"""
    conn = get_db_connection()
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()

def init_database():
    """Initialize database with tables and initial data"""
    with get_db() as conn:
        # Enable foreign keys
        conn.execute("PRAGMA foreign_keys = ON")
        
        # Create event_types table
        conn.execute('''
            CREATE TABLE IF NOT EXISTS event_types (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT
            )
        ''')
        
        # Create events table
        conn.execute('''
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                type_id INTEGER NOT NULL,
                start_date TEXT NOT NULL,
                location TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT,
                FOREIGN KEY (type_id) REFERENCES event_types (id)
            )
        ''')
        
        # Create images table
        conn.execute('''
            CREATE TABLE IF NOT EXISTS images (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER NOT NULL,
                original_name TEXT NOT NULL,
                filename TEXT NOT NULL,
                file_path TEXT NOT NULL,
                file_size INTEGER,
                uploaded_at TEXT NOT NULL,
                FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
            )
        ''')
        
        # Insert initial event types if table is empty
        cursor = conn.execute("SELECT COUNT(*) FROM event_types")
        if cursor.fetchone()[0] == 0:
            initial_types = [
                (1, "Hội thảo", "Sự kiện thảo luận chuyên đề"),
                (2, "Workshop", "Buổi học thực hành"),
                (3, "Seminar", "Buổi thuyết trình chuyên môn"),
                (4, "Conference", "Hội nghị lớn")
            ]
            conn.executemany(
                "INSERT INTO event_types (id, name, description) VALUES (?, ?, ?)",
                initial_types
            )
        
        # Insert initial events if table is empty
        cursor = conn.execute("SELECT COUNT(*) FROM events")
        if cursor.fetchone()[0] == 0:
            initial_events = [
                ("Hội thảo Công nghệ AI 2024", "Hội thảo về xu hướng và ứng dụng trí tuệ nhân tạo", 1, "2024-12-15T09:00:00", "Trung tâm Hội nghị Quốc gia", "2024-11-01T10:00:00"),
                ("Workshop React Advanced", "Workshop nâng cao về React và Next.js", 2, "2024-12-20T14:00:00", "Coworking Space Tech Hub", "2024-11-02T15:30:00"),
                ("Seminar Marketing Digital", "Chiến lược marketing trong thời đại số", 3, "2024-12-25T10:00:00", "Khách sạn Grand Plaza", "2024-11-03T08:45:00")
            ]
            conn.executemany(
                "INSERT INTO events (title, description, type_id, start_date, location, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                initial_events
            )

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def get_event_by_id(event_id: int) -> Optional[Dict[str, Any]]:
    """Get event by ID with images"""
    with get_db() as conn:
        # Get event
        event_cursor = conn.execute(
            "SELECT * FROM events WHERE id = ?",
            (event_id,)
        )
        event = event_cursor.fetchone()
        
        if not event:
            return None
        
        # Get images for this event
        images_cursor = conn.execute(
            "SELECT * FROM images WHERE event_id = ? ORDER BY uploaded_at DESC",
            (event_id,)
        )
        images = [dict(img) for img in images_cursor.fetchall()]
        
        # Convert to dict and add images
        event_dict = dict(event)
        event_dict['images'] = images
        
        return event_dict

def get_all_events(keyword: str = None, type_id: int = None) -> List[Dict[str, Any]]:
    """Get all events with optional filtering"""
    with get_db() as conn:
        query = "SELECT * FROM events WHERE 1=1"
        params = []
        
        if keyword:
            query += " AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)"
            params.extend([f'%{keyword.lower()}%', f'%{keyword.lower()}%'])
        
        if type_id:
            query += " AND type_id = ?"
            params.append(type_id)
        
        query += " ORDER BY created_at DESC"
        
        cursor = conn.execute(query, params)
        events = []
        
        for event_row in cursor.fetchall():
            event = dict(event_row)
            # Get images for this event
            images_cursor = conn.execute(
                "SELECT * FROM images WHERE event_id = ? ORDER BY uploaded_at DESC",
                (event['id'],)
            )
            event['images'] = [dict(img) for img in images_cursor.fetchall()]
            events.append(event)
        
        return events

def create_event(event_data: Dict[str, Any]) -> Dict[str, Any]:
    """Create a new event"""
    with get_db() as conn:
        cursor = conn.execute('''
            INSERT INTO events (title, description, type_id, start_date, location, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (
            event_data['title'],
            event_data['description'],
            event_data['typeId'],
            event_data['startDate'],
            event_data['location'],
            datetime.now().isoformat()
        ))
        
        event_id = cursor.lastrowid
        return get_event_by_id(event_id)

def update_event(event_id: int, event_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Update an existing event"""
    with get_db() as conn:
        # Check if event exists
        cursor = conn.execute("SELECT id FROM events WHERE id = ?", (event_id,))
        if not cursor.fetchone():
            return None
        
        # Build update query dynamically
        update_fields = []
        params = []
        
        field_mapping = {
            'title': 'title',
            'description': 'description',
            'typeId': 'type_id',
            'startDate': 'start_date',
            'location': 'location'
        }
        
        for field, db_field in field_mapping.items():
            if field in event_data and event_data[field]:
                update_fields.append(f"{db_field} = ?")
                params.append(event_data[field])
        
        if update_fields:
            update_fields.append("updated_at = ?")
            params.append(datetime.now().isoformat())
            params.append(event_id)
            
            query = f"UPDATE events SET {', '.join(update_fields)} WHERE id = ?"
            conn.execute(query, params)
        
        return get_event_by_id(event_id)

def delete_event(event_id: int) -> bool:
    """Delete an event and its images"""
    with get_db() as conn:
        cursor = conn.execute("SELECT id FROM events WHERE id = ?", (event_id,))
        if not cursor.fetchone():
            return False
        
        # Delete event (images will be deleted automatically due to CASCADE)
        conn.execute("DELETE FROM events WHERE id = ?", (event_id,))
        return True

def get_event_images(event_id: int) -> List[Dict[str, Any]]:
    """Get all images for an event"""
    with get_db() as conn:
        cursor = conn.execute(
            "SELECT * FROM images WHERE event_id = ? ORDER BY uploaded_at DESC",
            (event_id,)
        )
        return [dict(img) for img in cursor.fetchall()]

def add_event_images(event_id: int, image_files) -> List[Dict[str, Any]]:
    """Add images to an event"""
    with get_db() as conn:
        # Check if event exists
        cursor = conn.execute("SELECT id FROM events WHERE id = ?", (event_id,))
        if not cursor.fetchone():
            return []
        
        uploaded_images = []
        
        for file in image_files:
            if file and file.filename != '' and allowed_file(file.filename):
                # Generate unique filename
                file_extension = file.filename.rsplit('.', 1)[1].lower()
                unique_filename = f"{uuid.uuid4().hex}.{file_extension}"
                
                # Save file
                file_path = os.path.join(app.config['UPLOAD_FOLDER'], unique_filename)
                file.save(file_path)
                
                # Save to database
                cursor = conn.execute('''
                    INSERT INTO images (event_id, original_name, filename, file_path, file_size, uploaded_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                ''', (
                    event_id,
                    file.filename,
                    unique_filename,
                    file_path,
                    os.path.getsize(file_path),
                    datetime.now().isoformat()
                ))
                
                image_id = cursor.lastrowid
                
                # Get the inserted image
                img_cursor = conn.execute("SELECT * FROM images WHERE id = ?", (image_id,))
                image_data = dict(img_cursor.fetchone())
                image_data['url'] = f"/uploads/{unique_filename}"
                uploaded_images.append(image_data)
        
        return uploaded_images

def get_all_event_types() -> List[Dict[str, Any]]:
    """Get all event types"""
    with get_db() as conn:
        cursor = conn.execute("SELECT * FROM event_types ORDER BY id")
        return [dict(row) for row in cursor.fetchall()]

def create_response(success=True, data=None, message="", status_code=200):
    response = {
        "success": success,
        "data": data,
        "message": message
    }
    return jsonify(response), status_code

# Initialize database on startup
init_database()

# API Routes

@app.route('/', methods=['GET'])
def home():
    return create_response(
        data={
            "server": "Mock Events API Server (SQLite)",
            "version": "2.0.0",
            "database": DATABASE_PATH,
            "endpoints": [
                "GET /events",
                "GET /events/<id>",
                "POST /events",
                "PUT /events/<id>",
                "DELETE /events/<id>",
                "POST /events/<id>/images",
                "GET /event-types"
            ]
        },
        message="Mock Events API Server is running with SQLite database"
    )

@app.route('/events', methods=['GET'])
def get_events():
    """GET /events - Lấy danh sách sự kiện với tìm kiếm"""
    try:
        # Get query parameters
        keyword = request.args.get('q', '').lower()
        type_id = request.args.get('typeId', type=int)
        
        # Get filtered events
        filtered_events = get_all_events(keyword, type_id)
        
        return create_response(
            data={
                "events": filtered_events,
                "total": len(filtered_events),
                "filters": {
                    "keyword": keyword if keyword else None,
                    "typeId": type_id
                }
            },
            message=f"Lấy danh sách sự kiện thành công. Tìm thấy {len(filtered_events)} sự kiện."
        )
    
    except Exception as e:
        return create_response(
            success=False,
            message=f"Lỗi khi lấy danh sách sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events/<int:event_id>', methods=['GET'])
def get_event_detail(event_id):
    """GET /events/<id> - Lấy chi tiết sự kiện"""
    try:
        event = get_event_by_id(event_id)
        
        if not event:
            return create_response(
                success=False,
                message=f"Không tìm thấy sự kiện với ID {event_id}",
                status_code=404
            )
        
        return create_response(
            data=event,
            message="Lấy chi tiết sự kiện thành công"
        )
    
    except Exception as e:
        return create_response(
            success=False,
            message=f"Lỗi khi lấy chi tiết sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events', methods=['POST'])
def create_event_endpoint():
    """POST /events - Tạo sự kiện mới"""
    try:
        data = request.get_json()
        
        # Validate required fields
        required_fields = ['title', 'description', 'typeId', 'startDate', 'location']
        for field in required_fields:
            if field not in data or not data[field]:
                return create_response(
                    success=False,
                    message=f"Thiếu trường bắt buộc: {field}",
                    status_code=400
                )
        
        # Validate typeId exists
        event_types = get_all_event_types()
        if not any(et['id'] == data['typeId'] for et in event_types):
            return create_response(
                success=False,
                message="TypeId không hợp lệ",
                status_code=400
            )
        
        # Create new event
        new_event = create_event(data)
        
        return create_response(
            data=new_event,
            message="Tạo sự kiện thành công",
            status_code=201
        )
    
    except Exception as e:
        return create_response(
            success=False,
            message=f"Lỗi khi tạo sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events/<int:event_id>', methods=['PUT'])
def update_event_endpoint(event_id):
    """PUT /events/<id> - Cập nhật sự kiện"""
    try:
        data = request.get_json()
        
        # Update event
        updated_event = update_event(event_id, data)
        
        if not updated_event:
            return create_response(
                success=False,
                message=f"Không tìm thấy sự kiện với ID {event_id}",
                status_code=404
            )
        
        # Determine updated fields
        updated_fields = []
        field_mapping = {
            'title': 'title',
            'description': 'description',
            'typeId': 'type_id',
            'startDate': 'start_date',
            'location': 'location'
        }
        
        for field in data:
            if field in field_mapping and data[field]:
                updated_fields.append(field)
        
        return create_response(
            data={
                "event": updated_event,
                "updatedFields": updated_fields
            },
            message=f"Cập nhật sự kiện thành công. Đã cập nhật: {', '.join(updated_fields)}"
        )
    
    except Exception as e:
        return create_response(
            success=False,
            message=f"Lỗi khi cập nhật sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events/<int:event_id>', methods=['DELETE'])
def delete_event_endpoint(event_id):
    """DELETE /events/<id> - Xóa sự kiện"""
    try:
        success = delete_event(event_id)
        
        if not success:
            return create_response(
                success=False,
                message=f"Không tìm thấy sự kiện với ID {event_id}",
                status_code=404
            )
        
        return create_response(
            data={"deletedEventId": event_id},
            message="Xóa sự kiện thành công"
        )
    
    except Exception as e:
        return create_response(
            success=False,
            message=f"Lỗi khi xóa sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events/<int:event_id>/images', methods=['POST'])
def upload_images(event_id):
    """POST /events/<id>/images - Upload hình ảnh cho sự kiện"""
    try:
        # Check if files are in request
        if 'images' not in request.files:
            return create_response(
                success=False,
                message="Không tìm thấy file trong request",
                status_code=400
            )
        
        files = request.files.getlist('images')
        
        if not files or all(file.filename == '' for file in files):
            return create_response(
                success=False,
                message="Không có file nào được chọn",
                status_code=400
            )
        
        # Upload images
        uploaded_images = add_event_images(event_id, files)
        
        if not uploaded_images:
            return create_response(
                success=False,
                message="Không có file hợp lệ nào được upload hoặc sự kiện không tồn tại",
                status_code=400
            )
        
        # Get updated event with all images
        event = get_event_by_id(event_id)
        
        return create_response(
            data={
                "eventId": event_id,
                "uploadedImages": uploaded_images,
                "totalImages": len(event['images']) if event else len(uploaded_images)
            },
            message=f"Upload thành công {len(uploaded_images)} hình ảnh",
            status_code=201
        )
    
    except Exception as e:
        return create_response(
            success=False,
            message=f"Lỗi khi upload hình ảnh: {str(e)}",
            status_code=500
        )

@app.route('/event-types', methods=['GET'])
def get_event_types():
    """GET /event-types - Lấy danh sách loại sự kiện"""
    try:
        event_types = get_all_event_types()
        
        return create_response(
            data=event_types,
            message="Lấy danh sách loại sự kiện thành công"
        )
    
    except Exception as e:
        return create_response(
            success=False,
            message=f"Lỗi khi lấy danh sách loại sự kiện: {str(e)}",
            status_code=500
        )

# Serve uploaded files
@app.route('/uploads/<filename>')
def uploaded_file(filename):
    from flask import send_from_directory
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

# Error handlers
@app.errorhandler(404)
def not_found(error):
    return create_response(
        success=False,
        message="Endpoint không tồn tại",
        status_code=404
    )

@app.errorhandler(405)
def method_not_allowed(error):
    return create_response(
        success=False,
        message="Method không được hỗ trợ",
        status_code=405
    )

@app.errorhandler(500)
def internal_error(error):
    return create_response(
        success=False,
        message="Lỗi server nội bộ",
        status_code=500
    )

if __name__ == '__main__':
    print("🚀 Starting Mock Events API Server with SQLite...")
    print(f"📍 Database: {DATABASE_PATH}")
    print("📍 Server will be available at: http://localhost:5000")
    print("📝 API Documentation:")
    print("   GET    /events          - Lấy danh sách sự kiện")
    print("   GET    /events/<id>     - Chi tiết sự kiện")
    print("   POST   /events          - Tạo sự kiện mới")
    print("   PUT    /events/<id>     - Cập nhật sự kiện")
    print("   DELETE /events/<id>     - Xóa sự kiện")
    print("   POST   /events/<id>/images - Upload hình ảnh")
    print("   GET    /event-types     - Loại sự kiện")
    print("✨ CORS enabled - Có thể gọi từ mọi domain")
    print("🗄️  SQLite database với quan hệ một-nhiều events-images")
    print("-" * 50)
    
    app.run(debug=True, host='0.0.0.0', port=5000)