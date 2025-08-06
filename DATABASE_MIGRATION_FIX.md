# Database Migration Fix

## Vấn đề gặp phải

Khi chạy ứng dụng, gặp lỗi runtime:

```
java.lang.IllegalStateException: Migration didn't properly handle: todos(com.xdien.todoevent.data.entity.TodoEntity).
```

### Nguyên nhân

Lỗi xảy ra do sự không nhất quán giữa schema database hiện tại và schema mong đợi của Room:

- **Schema hiện tại**: `updatedAt` có `notNull = 'false'`
- **Schema mong đợi**: `updatedAt` có `notNull = 'true'`

Điều này xảy ra vì:
1. Trong `TodoEntity`, `updatedAt` được định nghĩa là `Long` (không nullable)
2. Nhưng trong migration, cột được tạo là nullable
3. Room phát hiện sự khác biệt và báo lỗi

## Giải pháp

### 1. Tăng Database Version

Tăng version từ 3 lên 4 để tạo migration mới:

```kotlin
@Database(
    entities = [TodoEntity::class],
    version = 4,  // Tăng từ 3 lên 4
    exportSchema = false
)
```

### 2. Tạo Migration Mới (MIGRATION_3_4)

Migration này sẽ:
- Cập nhật các giá trị NULL thành giá trị mặc định
- Tạo lại bảng với schema đúng
- Copy dữ liệu từ bảng cũ sang bảng mới

```kotlin
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Cập nhật các giá trị NULL
        db.execSQL("UPDATE todos SET updatedAt = createdAt WHERE updatedAt IS NULL")
        
        // Tạo bảng mới với schema đúng
        db.execSQL("CREATE TABLE todos_new (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "title TEXT NOT NULL, " +
            "description TEXT, " +
            "thumbnailUrl TEXT, " +
            "galleryImages TEXT, " +
            "eventTime INTEGER, " +
            "eventEndTime INTEGER, " +
            "location TEXT, " +
            "eventType TEXT, " +
            "isCompleted INTEGER NOT NULL, " +
            "createdAt INTEGER NOT NULL, " +
            "updatedAt INTEGER NOT NULL" +  // NOT NULL constraint
            ")")
        
        // Copy dữ liệu
        db.execSQL("INSERT INTO todos_new SELECT * FROM todos")
        
        // Xóa bảng cũ và đổi tên bảng mới
        db.execSQL("DROP TABLE todos")
        db.execSQL("ALTER TABLE todos_new RENAME TO todos")
    }
}
```

### 3. Cập nhật Migration List

Thêm migration mới vào danh sách:

```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
```

## Cấu trúc Database Cuối Cùng

Sau khi migration hoàn tất, database sẽ có cấu trúc:

```sql
CREATE TABLE todos (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    thumbnailUrl TEXT,
    galleryImages TEXT,
    eventTime INTEGER,
    eventEndTime INTEGER,
    location TEXT,
    eventType TEXT,
    isCompleted INTEGER NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
```

## Các Trường Dữ Liệu

| Trường | Kiểu | Nullable | Mô tả |
|--------|------|----------|-------|
| id | INTEGER | NOT NULL | Primary key, auto increment |
| title | TEXT | NOT NULL | Tiêu đề sự kiện |
| description | TEXT | NULL | Mô tả sự kiện |
| thumbnailUrl | TEXT | NULL | URL hình ảnh chính |
| galleryImages | TEXT | NULL | JSON array của URLs hình ảnh |
| eventTime | INTEGER | NULL | Thời gian bắt đầu (timestamp) |
| eventEndTime | INTEGER | NULL | Thời gian kết thúc (timestamp) |
| location | TEXT | NULL | Địa điểm sự kiện |
| eventType | TEXT | NULL | Loại sự kiện |
| isCompleted | INTEGER | NOT NULL | Trạng thái hoàn thành (0/1) |
| createdAt | INTEGER | NOT NULL | Thời gian tạo (timestamp) |
| updatedAt | INTEGER | NOT NULL | Thời gian cập nhật (timestamp) |

## TypeConverter

Để xử lý `List<String>` cho `galleryImages`, sử dụng Gson TypeConverter:

```kotlin
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, type)
        }
    }
}
```

## Cách Test Migration

1. **Clean Build**: `./gradlew clean build`
2. **Uninstall App**: Gỡ cài đặt app khỏi device/emulator
3. **Install App**: Cài đặt lại app
4. **Verify**: Kiểm tra xem app có chạy được không

## Lưu Ý Quan Trọng

1. **Backup Data**: Luôn backup dữ liệu trước khi thay đổi schema
2. **Test Migration**: Test migration trên device thật với dữ liệu thật
3. **Version Control**: Luôn tăng version database khi thay đổi schema
4. **Rollback Plan**: Có kế hoạch rollback nếu migration thất bại

## Troubleshooting

### Nếu vẫn gặp lỗi migration:

1. **Clear App Data**: Xóa dữ liệu app trong Settings
2. **Uninstall App**: Gỡ cài đặt hoàn toàn
3. **Check Schema**: Kiểm tra schema trong Room Inspector
4. **Log Migration**: Thêm log để debug migration process

### Debug Migration:

```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
.addCallback(object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("Database", "Database created")
    }
    
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        Log.d("Database", "Database opened")
    }
})
```

## Kết quả

Sau khi áp dụng fix này:
- ✅ Database migration hoạt động đúng
- ✅ Schema nhất quán với Entity
- ✅ App chạy được mà không gặp lỗi runtime
- ✅ Dữ liệu được bảo toàn (nếu có) 