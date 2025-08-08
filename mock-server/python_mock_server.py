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
import logging

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('server.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Configuration
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'webp'}
DATABASE_PATH = 'events.db'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Create upload directory if it doesn't exist
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def log_request():
    """Log detailed request information"""
    logger.info("=" * 80)
    logger.info(f"🌐 NEW REQUEST: {request.method} {request.url}")
    logger.info(f"📅 Timestamp: {datetime.now().isoformat()}")
    logger.info(f"👤 Client IP: {request.remote_addr}")
    logger.info(f"🌍 User Agent: {request.headers.get('User-Agent', 'Unknown')}")
    
    # Log headers
    logger.info("📋 Headers:")
    for header, value in request.headers.items():
        if header.lower() not in ['authorization', 'cookie']:  # Skip sensitive headers
            logger.info(f"   {header}: {value}")
    
    # Log query parameters
    if request.args:
        logger.info("🔍 Query Parameters:")
        for key, value in request.args.items():
            logger.info(f"   {key}: {value}")
    
    # Log form data
    if request.form:
        logger.info("📝 Form Data:")
        for key, value in request.form.items():
            logger.info(f"   {key}: {value}")
    
    # Log JSON body
    if request.is_json:
        try:
            body = request.get_json()
            logger.info("📄 JSON Body:")
            logger.info(f"   {json.dumps(body, indent=2, ensure_ascii=False)}")
        except Exception as e:
            logger.error(f"❌ Error parsing JSON body: {e}")
    
    # Log files
    if request.files:
        logger.info("📁 Files:")
        for key, file in request.files.items():
            if file and file.filename:
                logger.info(f"   {key}: {file.filename} ({file.content_type})")
    
    logger.info("-" * 80)

def log_response(response_data, status_code):
    """Log response information"""
    logger.info(f"📤 RESPONSE: Status {status_code}")
    
    if isinstance(response_data, tuple):
        response_json, status = response_data
        try:
            response_dict = response_json.get_json()
            logger.info("📄 Response Body:")
            logger.info(f"   {json.dumps(response_dict, indent=2, ensure_ascii=False)}")
        except Exception as e:
            logger.error(f"❌ Error parsing response JSON: {e}")
    else:
        logger.info(f"📄 Response: {response_data}")
    
    logger.info("=" * 80)

# Request logging middleware
@app.before_request
def before_request():
    log_request()

@app.after_request
def after_request(response):
    log_response(response, response.status_code)
    return response

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
        logger.debug("✅ Database transaction committed successfully")
    except Exception as e:
        conn.rollback()
        logger.error(f"❌ Database transaction rolled back due to error: {str(e)}")
        raise
    finally:
        conn.close()
        logger.debug("🔒 Database connection closed")

def init_database():
    """Initialize database with tables and initial data"""
    logger.info("🗄️ Initializing database...")
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
            logger.info("📝 Inserting initial event types...")
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
        # cursor = conn.execute("SELECT COUNT(*) FROM events")
        # if cursor.fetchone()[0] == 0:
        #     logger.info("📝 Inserting initial events...")
        #     initial_events = [
        #         ("Hội thảo Công nghệ AI 2024", "Hội thảo về xu hướng và ứng dụng trí tuệ nhân tạo", 1, "2024-12-15T09:00:00", "Trung tâm Hội nghị Quốc gia", "2024-11-01T10:00:00"),
        #         ("Workshop React Advanced", "Workshop nâng cao về React và Next.js", 2, "2024-12-20T14:00:00", "Coworking Space Tech Hub", "2024-11-02T15:30:00"),
        #         ("Seminar Marketing Digital", "Chiến lược marketing trong thời đại số", 3, "2024-12-25T10:00:00", "Khách sạn Grand Plaza", "2024-11-03T08:45:00")
        #     ]
        #     conn.executemany(
        #         "INSERT INTO events (title, description, type_id, start_date, location, created_at) VALUES (?, ?, ?, ?, ?, ?)",
        #         initial_events
        #     )
    
    logger.info("✅ Database initialization completed")

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def get_event_by_id(event_id: int) -> Optional[Dict[str, Any]]:
    """Get event by ID with images"""
    logger.debug(f"🔍 Getting event by ID: {event_id}")
    with get_db() as conn:
        # Get event
        event_cursor = conn.execute(
            "SELECT * FROM events WHERE id = ?",
            (event_id,)
        )
        event = event_cursor.fetchone()
        
        if not event:
            logger.warning(f"⚠️ Event not found with ID: {event_id}")
            return None
        
        # Get images for this event
        images_cursor = conn.execute(
            "SELECT * FROM images WHERE event_id = ? ORDER BY uploaded_at DESC",
            (event_id,)
        )
        images = [dict(img) for img in images_cursor.fetchall()]
        
        # Convert to dict and add images - use snake_case for all response fields
        event_dict = dict(event)
        # Map type_id to event_type_id for consistency
        event_dict['event_type_id'] = event_dict.pop('type_id', None)
        event_dict['images'] = images
        
        logger.debug(f"✅ Found event: {event_dict['title']} with {len(images)} images")
        return event_dict

def get_all_events(keyword: str = None, type_id: int = None) -> List[Dict[str, Any]]:
    """Get all events with optional filtering"""
    logger.debug(f"🔍 Getting events with filters - keyword: {keyword}, type_id: {type_id}")
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
            # Map type_id to event_type_id for consistency - use snake_case
            event['event_type_id'] = event.pop('type_id', None)
            # Get images for this event
            images_cursor = conn.execute(
                "SELECT * FROM images WHERE event_id = ? ORDER BY uploaded_at DESC",
                (event['id'],)
            )
            event['images'] = [dict(img) for img in images_cursor.fetchall()]
            events.append(event)
        
        logger.debug(f"✅ Found {len(events)} events")
        return events

def create_event(event_data: Dict[str, Any]) -> Dict[str, Any]:
    """Create a new event"""
    logger.info(f"📝 Creating new event: {event_data.get('title', 'Unknown')}")
    with get_db() as conn:
        # First, let's check the current max ID to understand the sequence
        cursor = conn.execute("SELECT MAX(id) FROM events")
        max_id_result = cursor.fetchone()
        current_max_id = max_id_result[0] if max_id_result[0] is not None else 0
        logger.info(f"🔍 Current max event ID: {current_max_id}")
        
        # Handle both camelCase and snake_case field names
        title = event_data.get('title') or event_data.get('title', '')
        description = event_data.get('description') or event_data.get('description', '')
        event_type_id = event_data.get('eventTypeId') or event_data.get('event_type_id')
        start_date = event_data.get('startDate') or event_data.get('start_date')
        location = event_data.get('location') or event_data.get('location', '')
        
        # Validate required fields
        if not all([title, description, event_type_id, start_date, location]):
            raise ValueError("Missing required fields: title, description, eventTypeId, startDate, location")
        
        # Insert the new event
        cursor = conn.execute('''
            INSERT INTO events (title, description, type_id, start_date, location, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (
            title,
            description,
            event_type_id,
            start_date,
            location,
            datetime.now().isoformat()
        ))
        
        event_id = cursor.lastrowid
        logger.info(f"✅ Event created with ID: {event_id}")
        
        # Verify the event was actually created within the same transaction
        verify_cursor = conn.execute("SELECT id, title FROM events WHERE id = ?", (event_id,))
        verify_result = verify_cursor.fetchone()
        
        if not verify_result:
            logger.error(f"❌ CRITICAL: Event with ID {event_id} was not found after creation!")
            raise Exception(f"Event creation failed - ID {event_id} not found in database")
        
        # Get the complete event data within the same transaction
        try:
            # Get event data directly in this transaction
            event_cursor = conn.execute(
                "SELECT * FROM events WHERE id = ?",
                (event_id,)
            )
            event = event_cursor.fetchone()
            
            if not event:
                logger.error(f"❌ CRITICAL: Event with ID {event_id} not found in same transaction!")
                raise Exception(f"Event not found in same transaction - ID {event_id}")
            
            # Get images for this event
            images_cursor = conn.execute(
                "SELECT * FROM images WHERE event_id = ? ORDER BY uploaded_at DESC",
                (event_id,)
            )
            images = [dict(img) for img in images_cursor.fetchall()]
            
            # Convert to dict and add images - use snake_case for response
            event_dict = dict(event)
            # Map type_id to event_type_id for consistency
            event_dict['event_type_id'] = event_dict.pop('type_id', None)
            event_dict['images'] = images
            
            logger.info(f"✅ Event data retrieved successfully: ID {event_id}, Title: {event_dict['title']}")
            return event_dict
            
        except Exception as e:
            logger.error(f"❌ CRITICAL: Could not retrieve event data for ID {event_id}: {str(e)}")
            # Return basic event data instead of raising exception
            return {
                "id": event_id,
                "title": "Event Created Successfully",
                "description": "Event was created but data retrieval failed",
                "event_type_id": 1,
                "start_date": datetime.now().isoformat(),
                "location": "Unknown",
                "created_at": datetime.now().isoformat(),
                "images": []
            }

def update_event(event_id: int, event_data: Dict[str, Any]) -> Optional[Dict[str, Any]]:
    """Update an existing event"""
    logger.info(f"📝 Updating event ID: {event_id}")
    with get_db() as conn:
        # Check if event exists
        cursor = conn.execute("SELECT id FROM events WHERE id = ?", (event_id,))
        if not cursor.fetchone():
            logger.warning(f"⚠️ Event not found for update: {event_id}")
            return None
        
        # Build update query dynamically - handle both camelCase and snake_case
        update_fields = []
        params = []
        
        field_mapping = {
            'title': 'title',
            'description': 'description',
            'eventTypeId': 'type_id',
            'event_type_id': 'type_id',
            'startDate': 'start_date',
            'start_date': 'start_date',
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
            logger.info(f"✅ Event updated with fields: {', '.join(update_fields[:-1])}")
        
        return get_event_by_id(event_id)

def delete_event(event_id: int) -> bool:
    """Delete an event and its images"""
    logger.info(f"🗑️ Deleting event ID: {event_id}")
    with get_db() as conn:
        cursor = conn.execute("SELECT id FROM events WHERE id = ?", (event_id,))
        if not cursor.fetchone():
            logger.warning(f"⚠️ Event not found for deletion: {event_id}")
            return False
        
        # Delete event (images will be deleted automatically due to CASCADE)
        conn.execute("DELETE FROM events WHERE id = ?", (event_id,))
        logger.info(f"✅ Event deleted successfully: {event_id}")
        return True

def get_event_images(event_id: int) -> List[Dict[str, Any]]:
    """Get all images for an event"""
    logger.debug(f"🔍 Getting images for event ID: {event_id}")
    with get_db() as conn:
        cursor = conn.execute(
            "SELECT * FROM images WHERE event_id = ? ORDER BY uploaded_at DESC",
            (event_id,)
        )
        images = [dict(img) for img in cursor.fetchall()]
        logger.debug(f"✅ Found {len(images)} images for event {event_id}")
        return images

def add_event_images(event_id: int, image_files) -> List[Dict[str, Any]]:
    """Add images to an event"""
    logger.info(f"📁 Adding images to event ID: {event_id}")
    with get_db() as conn:
        # Check if event exists with more detailed logging
        cursor = conn.execute("SELECT id, title FROM events WHERE id = ?", (event_id,))
        event_result = cursor.fetchone()
        if not event_result:
            logger.error(f"❌ Event not found for image upload: {event_id}")
            logger.error(f"🔍 Checking all events in database...")
            all_events_cursor = conn.execute("SELECT id, title FROM events ORDER BY id DESC LIMIT 10")
            all_events = all_events_cursor.fetchall()
            logger.error(f"📋 Recent events in database: {[dict(e) for e in all_events]}")
            return []
        
        logger.info(f"✅ Found event: ID {event_result[0]}, Title: {event_result[1]}")
        
        uploaded_images = []
        
        for file in image_files:
            if file and file.filename != '' and allowed_file(file.filename):
                logger.info(f"📸 Processing image: {file.filename}")
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
                
                # Return snake_case response for consistency
                mapped_image = {
                    'id': image_data['id'],
                    'event_id': image_data['event_id'],
                    'original_name': image_data['original_name'],
                    'filename': image_data['filename'],
                    'file_path': image_data['file_path'],
                    'file_size': image_data['file_size'],
                    'uploaded_at': image_data['uploaded_at']
                }
                uploaded_images.append(mapped_image)
                
                logger.info(f"✅ Image uploaded: {file.filename} -> {unique_filename}")
        
        logger.info(f"✅ Total images uploaded: {len(uploaded_images)}")
        return uploaded_images

def get_all_event_types() -> List[Dict[str, Any]]:
    """Get all event types"""
    logger.debug("🔍 Getting all event types")
    with get_db() as conn:
        cursor = conn.execute("SELECT * FROM event_types ORDER BY id")
        types = [dict(row) for row in cursor.fetchall()]
        logger.debug(f"✅ Found {len(types)} event types")
        return types

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
    logger.info("🏠 Home endpoint accessed")
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
                "GET /event-types",
                "GET /debug/events"
            ]
        },
        message="Mock Events API Server is running with SQLite database"
    )

@app.route('/events', methods=['GET'])
def get_events():
    """GET /events - Lấy danh sách sự kiện với tìm kiếm"""
    try:
        # Get query parameters - handle both camelCase and snake_case
        keyword = request.args.get('q', '').lower()
        type_id = request.args.get('typeId') or request.args.get('event_type_id')
        if type_id:
            type_id = int(type_id)
        
        logger.info(f"🔍 Getting events with filters - keyword: '{keyword}', type_id: {type_id}")
        
        # Get filtered events
        filtered_events = get_all_events(keyword, type_id)
        
        return create_response(
            data={
                "events": filtered_events,
                "total": len(filtered_events),
                "filters": {
                    "keyword": keyword if keyword else None,
                    "event_type_id": type_id
                }
            },
            message=f"Lấy danh sách sự kiện thành công. Tìm thấy {len(filtered_events)} sự kiện."
        )
    
    except Exception as e:
        logger.error(f"❌ Error getting events: {str(e)}")
        return create_response(
            success=False,
            message=f"Lỗi khi lấy danh sách sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events/<int:event_id>', methods=['GET'])
def get_event_detail(event_id):
    """GET /events/<id> - Lấy chi tiết sự kiện"""
    try:
        logger.info(f"🔍 Getting event detail for ID: {event_id}")
        event = get_event_by_id(event_id)
        
        if not event:
            logger.warning(f"⚠️ Event not found: {event_id}")
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
        logger.error(f"❌ Error getting event detail: {str(e)}")
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
        
        # Validate required fields - handle both camelCase and snake_case
        required_fields_camel = ['title', 'description', 'eventTypeId', 'startDate', 'location']
        required_fields_snake = ['title', 'description', 'event_type_id', 'start_date', 'location']
        
        # Check if we have camelCase or snake_case fields
        has_camel = all(field in data for field in required_fields_camel)
        has_snake = all(field in data for field in required_fields_snake)
        
        if not (has_camel or has_snake):
            missing_fields = []
            if not has_camel:
                missing_fields.extend([f for f in required_fields_camel if f not in data or not data[f]])
            if not has_snake:
                missing_fields.extend([f for f in required_fields_snake if f not in data or not data[f]])
            
            logger.warning(f"⚠️ Missing required fields: {missing_fields}")
            return create_response(
                success=False,
                message=f"Thiếu trường bắt buộc: {', '.join(missing_fields)}",
                status_code=400
            )
        
        # Validate event_type_id exists
        event_types = get_all_event_types()
        event_type_id = data.get('eventTypeId') or data.get('event_type_id')
        if not any(et['id'] == event_type_id for et in event_types):
            logger.warning(f"⚠️ Invalid event_type_id: {event_type_id}")
            return create_response(
                success=False,
                message="event_type_id không hợp lệ",
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
        logger.error(f"❌ Error creating event: {str(e)}")
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
        logger.info(f"📝 Updating event ID: {event_id}")
        
        # Update event
        updated_event = update_event(event_id, data)
        
        if not updated_event:
            logger.warning(f"⚠️ Event not found for update: {event_id}")
            return create_response(
                success=False,
                message=f"Không tìm thấy sự kiện với ID {event_id}",
                status_code=404
            )
        
        # Determine updated fields - handle both camelCase and snake_case
        updated_fields = []
        field_mapping = {
            'title': 'title',
            'description': 'description',
            'eventTypeId': 'event_type_id',
            'event_type_id': 'event_type_id',
            'startDate': 'start_date',
            'start_date': 'start_date',
            'location': 'location'
        }
        
        for field in data:
            if field in field_mapping and data[field]:
                # Map back to snake_case for response
                updated_fields.append(field_mapping[field])
        
        return create_response(
            data={
                "event": updated_event,
                "updated_fields": updated_fields
            },
            message=f"Cập nhật sự kiện thành công. Đã cập nhật: {', '.join(updated_fields)}"
        )
    
    except Exception as e:
        logger.error(f"❌ Error updating event: {str(e)}")
        return create_response(
            success=False,
            message=f"Lỗi khi cập nhật sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events/<int:event_id>', methods=['DELETE'])
def delete_event_endpoint(event_id):
    """DELETE /events/<id> - Xóa sự kiện"""
    try:
        logger.info(f"🗑️ Deleting event ID: {event_id}")
        success = delete_event(event_id)
        
        if not success:
            logger.warning(f"⚠️ Event not found for deletion: {event_id}")
            return create_response(
                success=False,
                message=f"Không tìm thấy sự kiện với ID {event_id}",
                status_code=404
            )
        
        return create_response(
            data={"deleted_event_id": event_id},
            message="Xóa sự kiện thành công"
        )
    
    except Exception as e:
        logger.error(f"❌ Error deleting event: {str(e)}")
        return create_response(
            success=False,
            message=f"Lỗi khi xóa sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/events/<int:event_id>/images', methods=['POST'])
def upload_images(event_id):
    """POST /events/<id>/images - Upload hình ảnh cho sự kiện"""
    try:
        logger.info(f"📁 Uploading images for event ID: {event_id}")
        
        # Check if files are in request
        if 'images' not in request.files:
            logger.warning("⚠️ No files found in request")
            return create_response(
                success=False,
                message="Không tìm thấy file trong request",
                status_code=400
            )
        
        files = request.files.getlist('images')
        logger.info(f"📁 Received {len(files)} files")
        
        if not files or all(file.filename == '' for file in files):
            logger.warning("⚠️ No valid files selected")
            return create_response(
                success=False,
                message="Không có file nào được chọn",
                status_code=400
            )
        
        # Upload images
        uploaded_images = add_event_images(event_id, files)
        
        if not uploaded_images:
            logger.warning("⚠️ No valid files uploaded or event not found")
            return create_response(
                success=False,
                message="Không có file hợp lệ nào được upload hoặc sự kiện không tồn tại",
                status_code=400
            )
        
        # Get updated event with all images
        event = get_event_by_id(event_id)
        
        return create_response(
            data={
                "event_id": event_id,
                "uploaded_images": uploaded_images,
                "total_images": len(event['images']) if event else len(uploaded_images)
            },
            message=f"Upload thành công {len(uploaded_images)} hình ảnh",
            status_code=201
        )
    
    except Exception as e:
        logger.error(f"❌ Error uploading images: {str(e)}")
        return create_response(
            success=False,
            message=f"Lỗi khi upload hình ảnh: {str(e)}",
            status_code=500
        )

@app.route('/event-types', methods=['GET'])
def get_event_types():
    """GET /event-types - Lấy danh sách loại sự kiện"""
    try:
        logger.info("🔍 Getting event types")
        event_types = get_all_event_types()
        
        return create_response(
            data=event_types,
            message="Lấy danh sách loại sự kiện thành công"
        )
    
    except Exception as e:
        logger.error(f"❌ Error getting event types: {str(e)}")
        return create_response(
            success=False,
            message=f"Lỗi khi lấy danh sách loại sự kiện: {str(e)}",
            status_code=500
        )

@app.route('/debug/events', methods=['GET'])
def debug_events():
    """GET /debug/events - Debug endpoint to check database state"""
    try:
        logger.info("🔍 Debug: Checking database state")
        with get_db() as conn:
            # Get total count
            count_cursor = conn.execute("SELECT COUNT(*) FROM events")
            total_count = count_cursor.fetchone()[0]
            
            # Get recent events
            recent_cursor = conn.execute("SELECT id, title, created_at FROM events ORDER BY id DESC LIMIT 10")
            recent_events = [dict(row) for row in recent_cursor.fetchall()]
            
            # Get max ID
            max_cursor = conn.execute("SELECT MAX(id) FROM events")
            max_id = max_cursor.fetchone()[0]
            
            # Get auto-increment info
            auto_cursor = conn.execute("SELECT seq FROM sqlite_sequence WHERE name='events'")
            auto_result = auto_cursor.fetchone()
            auto_increment = auto_result[0] if auto_result else 0
            
            debug_info = {
                "total_events": total_count,
                "max_id": max_id,
                "auto_increment": auto_increment,
                "recent_events": recent_events,
                "database_path": DATABASE_PATH
            }
            
            logger.info(f"🔍 Debug info: {debug_info}")
            
            return create_response(
                data=debug_info,
                message="Debug information retrieved successfully"
            )
    
    except Exception as e:
        logger.error(f"❌ Error in debug endpoint: {str(e)}")
        return create_response(
            success=False,
            message=f"Lỗi khi lấy thông tin debug: {str(e)}",
            status_code=500
        )

# Serve uploaded files
@app.route('/uploads/<filename>')
def uploaded_file(filename):
    logger.debug(f"📁 Serving file: {filename}")
    from flask import send_from_directory
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

# Error handlers
@app.errorhandler(404)
def not_found(error):
    logger.warning(f"⚠️ 404 Error: {request.url}")
    return create_response(
        success=False,
        message="Endpoint không tồn tại",
        status_code=404
    )

@app.errorhandler(405)
def method_not_allowed(error):
    logger.warning(f"⚠️ 405 Error: {request.method} {request.url}")
    return create_response(
        success=False,
        message="Method không được hỗ trợ",
        status_code=405
    )

@app.errorhandler(500)
def internal_error(error):
    logger.error(f"❌ 500 Error: {str(error)}")
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
    print("   GET    /debug/events    - Debug database state")
    print("   📝 Note: API now uses 'event_type_id' instead of 'typeId' for consistency")
    print("✨ CORS enabled - Có thể gọi từ mọi domain")
    print("🗄️  SQLite database với quan hệ một-nhiều events-images")
    print("📊 Comprehensive request logging enabled")
    print("📄 Log file: server.log")
    print("-" * 50)
    
    app.run(debug=True, host='0.0.0.0', port=5000)