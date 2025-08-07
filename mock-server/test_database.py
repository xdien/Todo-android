#!/usr/bin/env python3
"""
Test script to check database functionality and create test events
"""

import sqlite3
import json
from datetime import datetime
import os

DATABASE_PATH = 'events.db'

def test_database():
    """Test database functionality"""
    print("🔍 Testing database functionality...")
    
    if not os.path.exists(DATABASE_PATH):
        print(f"❌ Database file not found: {DATABASE_PATH}")
        return
    
    try:
        conn = sqlite3.connect(DATABASE_PATH)
        conn.row_factory = sqlite3.Row
        
        # Check tables
        cursor = conn.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = [row[0] for row in cursor.fetchall()]
        print(f"📋 Tables in database: {tables}")
        
        # Check events count
        cursor = conn.execute("SELECT COUNT(*) FROM events")
        events_count = cursor.fetchone()[0]
        print(f"📊 Total events: {events_count}")
        
        # Check recent events
        cursor = conn.execute("SELECT id, title, created_at FROM events ORDER BY id DESC LIMIT 5")
        recent_events = [dict(row) for row in cursor.fetchall()]
        print(f"📋 Recent events: {json.dumps(recent_events, indent=2, ensure_ascii=False)}")
        
        # Check auto-increment
        cursor = conn.execute("SELECT seq FROM sqlite_sequence WHERE name='events'")
        auto_result = cursor.fetchone()
        auto_increment = auto_result[0] if auto_result else 0
        print(f"🆔 Auto-increment value: {auto_increment}")
        
        # Check max ID
        cursor = conn.execute("SELECT MAX(id) FROM events")
        max_id = cursor.fetchone()[0]
        print(f"🆔 Max event ID: {max_id}")
        
        conn.close()
        
    except Exception as e:
        print(f"❌ Error testing database: {str(e)}")

def create_test_event():
    """Create a test event"""
    print("\n📝 Creating test event...")
    
    try:
        conn = sqlite3.connect(DATABASE_PATH)
        conn.row_factory = sqlite3.Row
        
        # Insert test event
        cursor = conn.execute('''
            INSERT INTO events (title, description, type_id, start_date, location, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (
            "Test Event - Database Check",
            "This is a test event to verify database functionality",
            1,  # Hội thảo
            "2024-12-30T10:00:00",
            "Test Location",
            datetime.now().isoformat()
        ))
        
        event_id = cursor.lastrowid
        conn.commit()
        
        print(f"✅ Test event created with ID: {event_id}")
        
        # Verify the event was created
        cursor = conn.execute("SELECT * FROM events WHERE id = ?", (event_id,))
        event = cursor.fetchone()
        if event:
            print(f"✅ Event verification successful: {dict(event)}")
        else:
            print(f"❌ Event verification failed - event not found")
        
        conn.close()
        return event_id
        
    except Exception as e:
        print(f"❌ Error creating test event: {str(e)}")
        return None

def test_image_upload(event_id):
    """Test image upload functionality"""
    if not event_id:
        print("❌ No event ID provided for image upload test")
        return
    
    print(f"\n📁 Testing image upload for event ID: {event_id}")
    
    try:
        conn = sqlite3.connect(DATABASE_PATH)
        conn.row_factory = sqlite3.Row
        
        # Check if event exists
        cursor = conn.execute("SELECT id, title FROM events WHERE id = ?", (event_id,))
        event = cursor.fetchone()
        
        if not event:
            print(f"❌ Event {event_id} not found for image upload test")
            return
        
        print(f"✅ Found event: {dict(event)}")
        
        # Insert test image record
        cursor = conn.execute('''
            INSERT INTO images (event_id, original_name, filename, file_path, file_size, uploaded_at)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (
            event_id,
            "test_image.jpg",
            "test_image_123.jpg",
            "/uploads/test_image_123.jpg",
            1024,
            datetime.now().isoformat()
        ))
        
        image_id = cursor.lastrowid
        conn.commit()
        
        print(f"✅ Test image record created with ID: {image_id}")
        
        # Verify image was created
        cursor = conn.execute("SELECT * FROM images WHERE id = ?", (image_id,))
        image = cursor.fetchone()
        if image:
            print(f"✅ Image verification successful: {dict(image)}")
        else:
            print(f"❌ Image verification failed - image not found")
        
        conn.close()
        
    except Exception as e:
        print(f"❌ Error testing image upload: {str(e)}")

if __name__ == "__main__":
    print("🚀 Database Test Script")
    print("=" * 50)
    
    # Test database
    test_database()
    
    # Create test event
    test_event_id = create_test_event()
    
    # Test image upload
    if test_event_id:
        test_image_upload(test_event_id)
    
    print("\n✅ Database test completed!")
