#!/usr/bin/env python3
"""
Script to create sample events for demo purposes
Creates diverse events with different types, dates, and image scenarios
"""

import sqlite3
import json
from datetime import datetime, timedelta
import os
import random

DATABASE_PATH = 'events.db'

# Sample event types (matching the app's color scheme)
EVENT_TYPES = [
    {"id": 1, "name": "Meeting", "description": "Họp nhóm, họp dự án"},
    {"id": 2, "name": "Work", "description": "Công việc, deadline"},
    {"id": 3, "name": "Personal", "description": "Sự kiện cá nhân"},
    {"id": 4, "name": "Party", "description": "Tiệc tùng, sinh nhật"},
    {"id": 5, "name": "Conference", "description": "Hội thảo, workshop"}
]

# Sample events data
SAMPLE_EVENTS = [
    {
        "title": "Họp dự án Mobile App",
        "description": "Thảo luận về tiến độ phát triển ứng dụng di động, review code và lập kế hoạch cho sprint tiếp theo. Cần chuẩn bị demo cho khách hàng.",
        "type_id": 1,  # Meeting
        "start_date": (datetime.now() + timedelta(days=1, hours=9)).isoformat(),
        "location": "Phòng họp A, Tầng 3, Tòa nhà TechCorp",
        "has_images": True,
        "image_count": 2
    },
    {
        "title": "Deadline báo cáo quý 4",
        "description": "Hoàn thành báo cáo tài chính quý 4, tổng hợp dữ liệu từ các phòng ban và chuẩn bị presentation cho ban lãnh đạo.",
        "type_id": 2,  # Work
        "start_date": (datetime.now() + timedelta(days=2, hours=14)).isoformat(),
        "location": "Văn phòng chính, Tầng 5",
        "has_images": False,
        "image_count": 0
    },
    {
        "title": "Sinh nhật mẹ",
        "description": "Tổ chức tiệc sinh nhật cho mẹ tại nhà, mời gia đình và bạn bè thân thiết. Chuẩn bị bánh kem và quà tặng.",
        "type_id": 4,  # Party
        "start_date": (datetime.now() + timedelta(days=3, hours=18)).isoformat(),
        "location": "Nhà riêng, 123 Đường ABC, Quận 1",
        "has_images": True,
        "image_count": 3
    },
    {
        "title": "Workshop React Native",
        "description": "Tham gia workshop về React Native do công ty tổ chức. Học về state management, navigation và performance optimization.",
        "type_id": 5,  # Conference
        "start_date": (datetime.now() + timedelta(days=5, hours=10)).isoformat(),
        "location": "Hội trường lớn, Tầng 1, Tòa nhà TechCorp",
        "has_images": True,
        "image_count": 1
    },
    {
        "title": "Khám sức khỏe định kỳ",
        "description": "Khám sức khỏe định kỳ hàng năm tại bệnh viện. Kiểm tra tổng quát, xét nghiệm máu và tư vấn dinh dưỡng.",
        "type_id": 3,  # Personal
        "start_date": (datetime.now() + timedelta(days=7, hours=8)).isoformat(),
        "location": "Bệnh viện Đa khoa Trung ương, Phòng 301",
        "has_images": False,
        "image_count": 0
    },
    {
        "title": "Họp review code sprint 15",
        "description": "Review code cho sprint 15, kiểm tra chất lượng, performance và security. Thảo luận về best practices và refactoring.",
        "type_id": 1,  # Meeting
        "start_date": (datetime.now() + timedelta(days=1, hours=15)).isoformat(),
        "location": "Phòng họp B, Tầng 4",
        "has_images": True,
        "image_count": 4
    },
    {
        "title": "Tiệc cuối năm công ty",
        "description": "Tiệc cuối năm với toàn bộ nhân viên công ty. Tổng kết năm cũ, kế hoạch năm mới và trao thưởng cho nhân viên xuất sắc.",
        "type_id": 4,  # Party
        "start_date": (datetime.now() + timedelta(days=10, hours=19)).isoformat(),
        "location": "Khách sạn 5 sao, 456 Đường XYZ, Quận 3",
        "has_images": True,
        "image_count": 5
    },
    {
        "title": "Giao lưu với khách hàng",
        "description": "Gặp gỡ khách hàng mới để thảo luận về dự án hợp tác. Giới thiệu sản phẩm và dịch vụ của công ty.",
        "type_id": 2,  # Work
        "start_date": (datetime.now() + timedelta(days=4, hours=11)).isoformat(),
        "location": "Café Highlands, 789 Đường DEF, Quận 2",
        "has_images": False,
        "image_count": 0
    },
    {
        "title": "Chạy bộ buổi sáng",
        "description": "Chạy bộ buổi sáng tại công viên, tập luyện sức khỏe và thư giãn đầu óc. Khoảng cách 5km.",
        "type_id": 3,  # Personal
        "start_date": (datetime.now() + timedelta(days=1, hours=6)).isoformat(),
        "location": "Công viên Tao Đàn, Quận 1",
        "has_images": True,
        "image_count": 2
    },
    {
        "title": "Tech Talk: AI trong Mobile",
        "description": "Tham gia tech talk về ứng dụng AI trong phát triển mobile app. Chia sẻ kinh nghiệm và thảo luận về xu hướng tương lai.",
        "type_id": 5,  # Conference
        "start_date": (datetime.now() + timedelta(days=6, hours=14)).isoformat(),
        "location": "Coworking space, 321 Đường GHI, Quận 7",
        "has_images": True,
        "image_count": 3
    }
]

def init_database():
    """Initialize database with sample data"""
    print("🚀 Initializing database with sample events...")
    
    try:
        conn = sqlite3.connect(DATABASE_PATH)
        conn.row_factory = sqlite3.Row
        
        # Clear existing data
        conn.execute("DELETE FROM images")
        conn.execute("DELETE FROM events")
        conn.execute("DELETE FROM event_types")
        
        # Reset auto-increment
        conn.execute("DELETE FROM sqlite_sequence WHERE name='events'")
        conn.execute("DELETE FROM sqlite_sequence WHERE name='images'")
        conn.execute("DELETE FROM sqlite_sequence WHERE name='event_types'")
        
        # Insert event types
        print("📋 Creating event types...")
        for event_type in EVENT_TYPES:
            conn.execute('''
                INSERT INTO event_types (id, name, description)
                VALUES (?, ?, ?)
            ''', (event_type["id"], event_type["name"], event_type["description"]))
        
        # Insert sample events
        print("📝 Creating sample events...")
        for i, event_data in enumerate(SAMPLE_EVENTS, 1):
            cursor = conn.execute('''
                INSERT INTO events (title, description, type_id, start_date, location, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (
                event_data["title"],
                event_data["description"],
                event_data["type_id"],
                event_data["start_date"],
                event_data["location"],
                datetime.now().isoformat()
            ))
            
            event_id = cursor.lastrowid
            
            # Add sample images if needed
            if event_data["has_images"]:
                print(f"📁 Adding {event_data['image_count']} images for event {i}")
                for img_idx in range(event_data["image_count"]):
                    # Create sample image record
                    filename = f"sample_event_{event_id}_img_{img_idx + 1}.jpg"
                    conn.execute('''
                        INSERT INTO images (event_id, original_name, filename, file_path, file_size, uploaded_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                    ''', (
                        event_id,
                        f"event_image_{img_idx + 1}.jpg",
                        filename,
                        f"/uploads/{filename}",
                        random.randint(500000, 2000000),  # 500KB - 2MB
                        datetime.now().isoformat()
                    ))
        
        conn.commit()
        print("✅ Sample data created successfully!")
        
        # Verify data
        cursor = conn.execute("SELECT COUNT(*) FROM events")
        events_count = cursor.fetchone()[0]
        cursor = conn.execute("SELECT COUNT(*) FROM images")
        images_count = cursor.fetchone()[0]
        cursor = conn.execute("SELECT COUNT(*) FROM event_types")
        types_count = cursor.fetchone()[0]
        
        print(f"📊 Database summary:")
        print(f"   - Events: {events_count}")
        print(f"   - Images: {images_count}")
        print(f"   - Event types: {types_count}")
        
        conn.close()
        return True
        
    except Exception as e:
        print(f"❌ Error creating sample data: {str(e)}")
        return False

def create_sample_images():
    """Create sample image files for demo"""
    print("\n🖼️ Creating sample image files...")
    
    upload_dir = "uploads"
    os.makedirs(upload_dir, exist_ok=True)
    
    # Create some sample image files (empty files for demo)
    sample_images = [
        "sample_event_1_img_1.jpg",
        "sample_event_1_img_2.jpg", 
        "sample_event_3_img_1.jpg",
        "sample_event_3_img_2.jpg",
        "sample_event_3_img_3.jpg",
        "sample_event_4_img_1.jpg",
        "sample_event_6_img_1.jpg",
        "sample_event_6_img_2.jpg",
        "sample_event_6_img_3.jpg",
        "sample_event_6_img_4.jpg",
        "sample_event_7_img_1.jpg",
        "sample_event_7_img_2.jpg",
        "sample_event_7_img_3.jpg",
        "sample_event_7_img_4.jpg",
        "sample_event_7_img_5.jpg",
        "sample_event_9_img_1.jpg",
        "sample_event_9_img_2.jpg",
        "sample_event_10_img_1.jpg",
        "sample_event_10_img_2.jpg",
        "sample_event_10_img_3.jpg"
    ]
    
    for img_name in sample_images:
        img_path = os.path.join(upload_dir, img_name)
        if not os.path.exists(img_path):
            with open(img_path, 'w') as f:
                f.write(f"# Sample image file: {img_name}\n")
                f.write(f"# Created for demo purposes\n")
                f.write(f"# This is a placeholder file\n")
            print(f"✅ Created: {img_name}")
        else:
            print(f"⏭️ Skipped: {img_name} (already exists)")

def show_sample_data():
    """Display sample data for verification"""
    print("\n📋 Sample Events Preview:")
    print("=" * 80)
    
    for i, event in enumerate(SAMPLE_EVENTS, 1):
        event_type = next(et for et in EVENT_TYPES if et["id"] == event["type_id"])
        start_date = datetime.fromisoformat(event["start_date"]).strftime("%d/%m/%Y %H:%M")
        
        print(f"{i:2d}. {event['title']}")
        print(f"    📅 {start_date} | 📍 {event['location']}")
        print(f"    🏷️ {event_type['name']} | 📸 {event['image_count']} ảnh")
        print(f"    📝 {event['description'][:80]}...")
        print()

def main():
    print("🎯 Sample Events Creator for Demo")
    print("=" * 50)
    
    # Show preview
    show_sample_data()
    
    # Create sample data
    if init_database():
        create_sample_images()
        print("\n🎉 Sample data creation completed!")
        print("\n📱 You can now run the mock server and test the app with this sample data.")
        print("   Run: python3 python_mock_server.py")
    else:
        print("\n❌ Failed to create sample data!")

if __name__ == "__main__":
    main()
