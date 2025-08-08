#!/usr/bin/env python3
"""
Script to create demo events specifically for video demo
Creates events with memorable names and diverse scenarios for quick demo
"""

import sqlite3
import json
from datetime import datetime, timedelta
import os
import random

DATABASE_PATH = 'events.db'

# Demo event types with Vietnamese names
DEMO_EVENT_TYPES = [
    {"id": 1, "name": "Meeting", "description": "Họp nhóm, họp dự án"},
    {"id": 2, "name": "Work", "description": "Công việc, deadline"},
    {"id": 3, "name": "Personal", "description": "Sự kiện cá nhân"},
    {"id": 4, "name": "Party", "description": "Tiệc tùng, sinh nhật"},
    {"id": 5, "name": "Conference", "description": "Hội thảo, workshop"}
]

# Demo events - designed for quick video demo
DEMO_EVENTS = [
    {
        "title": "Demo App Meeting",
        "description": "Cuộc họp demo ứng dụng sự kiện cá nhân. Thảo luận về tính năng mới và kế hoạch phát triển.",
        "type_id": 1,  # Meeting
        "start_date": (datetime.now() + timedelta(days=1, hours=10)).isoformat(),
        "location": "Phòng họp Demo, Tầng 2",
        "has_images": True,
        "image_count": 2,
        "demo_note": "Sự kiện đầu tiên để demo - có ảnh"
    },
    {
        "title": "Deadline Project",
        "description": "Hoàn thành dự án demo app. Kiểm tra cuối cùng và chuẩn bị presentation.",
        "type_id": 2,  # Work
        "start_date": (datetime.now() + timedelta(days=2, hours=16)).isoformat(),
        "location": "Văn phòng làm việc",
        "has_images": False,
        "image_count": 0,
        "demo_note": "Sự kiện Work - không có ảnh"
    },
    {
        "title": "Sinh nhật Demo",
        "description": "Tiệc sinh nhật demo với bạn bè. Tổ chức tại nhà với bánh kem và quà tặng.",
        "type_id": 4,  # Party
        "start_date": (datetime.now() + timedelta(days=3, hours=19)).isoformat(),
        "location": "Nhà riêng Demo",
        "has_images": True,
        "image_count": 3,
        "demo_note": "Sự kiện Party - nhiều ảnh"
    },
    {
        "title": "Workshop Demo",
        "description": "Workshop về phát triển ứng dụng mobile. Học về React Native và Flutter.",
        "type_id": 5,  # Conference
        "start_date": (datetime.now() + timedelta(days=4, hours=14)).isoformat(),
        "location": "Hội trường Workshop",
        "has_images": True,
        "image_count": 1,
        "demo_note": "Sự kiện Conference - 1 ảnh"
    },
    {
        "title": "Chạy bộ Demo",
        "description": "Chạy bộ buổi sáng tại công viên. Tập luyện sức khỏe và thư giãn.",
        "type_id": 3,  # Personal
        "start_date": (datetime.now() + timedelta(days=1, hours=6)).isoformat(),
        "location": "Công viên Demo",
        "has_images": True,
        "image_count": 2,
        "demo_note": "Sự kiện Personal - có ảnh"
    },
    {
        "title": "Review Code Demo",
        "description": "Review code cho dự án demo app. Kiểm tra chất lượng và performance.",
        "type_id": 1,  # Meeting
        "start_date": (datetime.now() + timedelta(days=1, hours=15)).isoformat(),
        "location": "Phòng họp Review",
        "has_images": True,
        "image_count": 4,
        "demo_note": "Sự kiện Meeting - nhiều ảnh"
    },
    {
        "title": "Tiệc Demo",
        "description": "Tiệc demo với team. Tổng kết dự án và ăn mừng thành công.",
        "type_id": 4,  # Party
        "start_date": (datetime.now() + timedelta(days=5, hours=18)).isoformat(),
        "location": "Nhà hàng Demo",
        "has_images": True,
        "image_count": 5,
        "demo_note": "Sự kiện Party - tối đa 5 ảnh"
    },
    {
        "title": "Gặp khách hàng Demo",
        "description": "Gặp gỡ khách hàng để demo sản phẩm. Giới thiệu tính năng mới.",
        "type_id": 2,  # Work
        "start_date": (datetime.now() + timedelta(days=3, hours=11)).isoformat(),
        "location": "Café Demo",
        "has_images": False,
        "image_count": 0,
        "demo_note": "Sự kiện Work - không ảnh"
    }
]

def create_demo_database():
    """Create demo database with sample events"""
    print("🎬 Creating Demo Database for Video Demo")
    print("=" * 50)
    
    try:
        conn = sqlite3.connect(DATABASE_PATH)
        conn.row_factory = sqlite3.Row
        
        # Clear existing data
        print("🧹 Clearing existing data...")
        conn.execute("DELETE FROM images")
        conn.execute("DELETE FROM events")
        conn.execute("DELETE FROM event_types")
        
        # Reset auto-increment
        conn.execute("DELETE FROM sqlite_sequence WHERE name='events'")
        conn.execute("DELETE FROM sqlite_sequence WHERE name='images'")
        conn.execute("DELETE FROM sqlite_sequence WHERE name='event_types'")
        
        # Insert event types
        print("📋 Creating event types...")
        for event_type in DEMO_EVENT_TYPES:
            conn.execute('''
                INSERT INTO event_types (id, name, description)
                VALUES (?, ?, ?)
            ''', (event_type["id"], event_type["name"], event_type["description"]))
        
        # Insert demo events
        print("📝 Creating demo events...")
        for i, event_data in enumerate(DEMO_EVENTS, 1):
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
            
            # Add demo images if needed
            if event_data["has_images"]:
                print(f"📁 Adding {event_data['image_count']} images for event {i}: {event_data['title']}")
                for img_idx in range(event_data["image_count"]):
                    # Create demo image record
                    filename = f"demo_event_{event_id}_img_{img_idx + 1}.jpg"
                    conn.execute('''
                        INSERT INTO images (event_id, original_name, filename, file_path, file_size, uploaded_at)
                        VALUES (?, ?, ?, ?, ?, ?)
                    ''', (
                        event_id,
                        f"demo_image_{img_idx + 1}.jpg",
                        filename,
                        f"/uploads/{filename}",
                        random.randint(800000, 1500000),  # 800KB - 1.5MB
                        datetime.now().isoformat()
                    ))
        
        conn.commit()
        print("✅ Demo database created successfully!")
        
        # Show summary
        cursor = conn.execute("SELECT COUNT(*) FROM events")
        events_count = cursor.fetchone()[0]
        cursor = conn.execute("SELECT COUNT(*) FROM images")
        images_count = cursor.fetchone()[0]
        cursor = conn.execute("SELECT COUNT(*) FROM event_types")
        types_count = cursor.fetchone()[0]
        
        print(f"\n📊 Demo Database Summary:")
        print(f"   - Events: {events_count}")
        print(f"   - Images: {images_count}")
        print(f"   - Event types: {types_count}")
        
        conn.close()
        return True
        
    except Exception as e:
        print(f"❌ Error creating demo database: {str(e)}")
        return False

def create_demo_images():
    """Create demo image files"""
    print("\n🖼️ Creating demo image files...")
    
    upload_dir = "uploads"
    os.makedirs(upload_dir, exist_ok=True)
    
    # Create demo image files
    demo_images = [
        "demo_event_1_img_1.jpg",
        "demo_event_1_img_2.jpg",
        "demo_event_3_img_1.jpg", 
        "demo_event_3_img_2.jpg",
        "demo_event_3_img_3.jpg",
        "demo_event_4_img_1.jpg",
        "demo_event_5_img_1.jpg",
        "demo_event_5_img_2.jpg",
        "demo_event_6_img_1.jpg",
        "demo_event_6_img_2.jpg",
        "demo_event_6_img_3.jpg",
        "demo_event_6_img_4.jpg",
        "demo_event_7_img_1.jpg",
        "demo_event_7_img_2.jpg",
        "demo_event_7_img_3.jpg",
        "demo_event_7_img_4.jpg",
        "demo_event_7_img_5.jpg"
    ]
    
    for img_name in demo_images:
        img_path = os.path.join(upload_dir, img_name)
        if not os.path.exists(img_path):
            with open(img_path, 'w') as f:
                f.write(f"# Demo image file: {img_name}\n")
                f.write(f"# Created for video demo purposes\n")
                f.write(f"# This is a placeholder file for demo\n")
                f.write(f"# File size: {random.randint(800000, 1500000)} bytes\n")
            print(f"✅ Created: {img_name}")
        else:
            print(f"⏭️ Skipped: {img_name} (already exists)")

def show_demo_events():
    """Display demo events for verification"""
    print("\n🎬 Demo Events for Video Demo:")
    print("=" * 80)
    
    for i, event in enumerate(DEMO_EVENTS, 1):
        event_type = next(et for et in DEMO_EVENT_TYPES if et["id"] == event["type_id"])
        start_date = datetime.fromisoformat(event["start_date"]).strftime("%d/%m %H:%M")
        
        print(f"{i}. {event['title']}")
        print(f"   📅 {start_date} | 📍 {event['location']}")
        print(f"   🏷️ {event_type['name']} | 📸 {event['image_count']} ảnh")
        print(f"   💡 {event['demo_note']}")
        print()

def create_demo_script():
    """Create demo script for video recording"""
    print("\n📝 Demo Script for Video Recording:")
    print("=" * 80)
    
    script = """
🎬 VIDEO DEMO SCRIPT - APP SỰ KIỆN CÁ NHÂN
==========================================

📱 PHẦN 1: GIỚI THIỆU UI (30 giây)
- Mở app, hiển thị danh sách sự kiện
- Giới thiệu: "Đây là app quản lý sự kiện cá nhân với 8 sự kiện mẫu"
- Chỉ ra: Chip filter (Meeting, Work, Personal, Party, Conference)
- Chỉ ra: Search bar và nút Settings

🔍 PHẦN 2: TÌM KIẾM & LỌC (30 giây)
- Nhấn Search → Nhập "Demo" → Hiển thị kết quả
- Clear search → Chọn chip "Meeting" → Chỉ hiển thị 3 sự kiện Meeting
- Bỏ chọn chip → Hiển thị lại tất cả

➕ PHẦN 3: TẠO SỰ KIỆN MỚI (45 giây)
- Nhấn nút "+" → Màn hình tạo mới
- Nhập: "Demo Video Event"
- Nhập mô tả: "Sự kiện tạo trong video demo"
- Chọn thời gian: Ngày mai 15:00
- Nhập địa điểm: "Studio Demo"
- Chọn loại: "Personal"
- Thêm 2 ảnh từ gallery
- Nhấn "Lưu" → Thành công

👁️ PHẦN 4: XEM CHI TIẾT & CHỈNH SỬA (45 giây)
- Nhấn vào "Demo App Meeting" → Màn hình chi tiết
- Xem thông tin đầy đủ và ảnh
- Nhấn "Chỉnh sửa" → Thay đổi tiêu đề thành "Demo App Meeting Updated"
- Lưu → Quay về chi tiết → Thấy thay đổi

📤 PHẦN 5: CHIA SẺ & XÓA (30 giây)
- Nhấn icon "Chia sẻ" → Hiển thị native share
- Quay lại → Nhấn "Xóa" → Dialog xác nhận
- Nhấn "Xóa" → Quay về danh sách → Sự kiện đã xóa

🔄 PHẦN 6: REFRESH & KẾT LUẬN (30 giây)
- Pull-to-refresh → Loading indicator
- "App hỗ trợ offline cache và sync với server"
- "Có thể tìm kiếm, lọc, tạo, sửa, xóa sự kiện"
- "Upload ảnh và validation form đầy đủ"
"""
    
    print(script)

def main():
    print("🎬 Demo Events Creator for Video Demo")
    print("=" * 50)
    
    # Show demo events
    show_demo_events()
    
    # Create demo database
    if create_demo_database():
        create_demo_images()
        print("\n🎉 Demo database creation completed!")
        
        # Show demo script
        create_demo_script()
        
        print("\n📱 Next steps:")
        print("   1. Run: python3 python_mock_server.py")
        print("   2. Open app and test with demo data")
        print("   3. Follow the demo script for video recording")
    else:
        print("\n❌ Failed to create demo database!")

if __name__ == "__main__":
    main()
