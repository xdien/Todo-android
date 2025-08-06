# Tài liệu Phát triển Tính năng - Ứng dụng Quản lý Sự kiện Cá nhân

## I. Tổng quan Dự án

### Mục tiêu
Xây dựng ứng dụng mobile "Quản lý sự kiện cá nhân" với khả năng lưu trữ offline và đồng bộ dữ liệu.

### Công nghệ sử dụng
- **Android**: Kotlin + Jetpack Compose v2
- **iOS**: Swift + SwiftUI (tương lai)
- **Backend**: REST API (mock/local JSON)
- **Database**: Room (Android) / CoreData (iOS)
- **Architecture**: Clean Architecture + MVVM + Repository Pattern

### Kiến trúc Clean Architecture
Dự án tuân thủ Clean Architecture với các layer rõ ràng:

#### 1. Presentation Layer (UI)
- **Jetpack Compose v2**: Sử dụng Compose version 2.2.0 với kotlinCompilerExtensionVersion tương ứng
- **ViewModels**: Quản lý state và business logic cho UI
- **Screens**: Các màn hình Compose được tổ chức theo tính năng

#### 2. Domain Layer (Business Logic)
- **Use Cases**: Tất cả Use Cases phải kế thừa từ abstract class `UseCase<Input, Output>`
- **Entities**: Các domain entities không phụ thuộc vào framework
- **Repository Interfaces**: Định nghĩa contracts cho data access

#### 3. Data Layer (Data Access)
- **Repository Implementations**: Implement các repository interfaces
- **Data Sources**: API và Database
- **Data Models**: DTOs và Database Entities

### UseCase Pattern Implementation
Tất cả Use Cases trong dự án phải tuân thủ pattern sau:

```kotlin
// Base UseCase class (common/UseCase.kt)
abstract class UseCase<Input, Output> {
    @Throws(Exception::class)
    abstract suspend fun execute(input: Input): Output
}

// Example UseCase implementation
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventsInput, List<Event>>() {
    
    override suspend fun execute(input: GetEventsInput): List<Event> {
        return repository.getEvents(input.query, input.typeId)
    }
}

// Input/Output data classes
data class GetEventsInput(
    val query: String? = null,
    val typeId: String? = null
)
```

### Jetpack Compose v2 Configuration
```kotlin
// build.gradle.kts
android {
    composeOptions {
        kotlinCompilerExtensionVersion = "2.2.0" // Compose v2
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom)) // BOM for version management
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
}
```

### Annotation Processing
Dự án sử dụng **KSP (Kotlin Symbol Processing)** thay vì kapt để tương thích với Compose v2:
```kotlin
plugins {
    alias(libs.plugins.ksp) // Thay vì kapt
}

dependencies {
    ksp(libs.hilt.compiler) // Thay vì kapt
    ksp(libs.room.compiler) // Thay vì kapt
}
```

## II. Kiến trúc Hệ thống

### 2.1 Cấu trúc thư mục
```
app/src/main/java/com/xdien/todoevent/
├── data/
│   ├── api/
│   │   ├── EventApi.kt
│   │   └── EventTypeApi.kt
│   ├── database/
│   │   ├── EventDatabase.kt
│   │   ├── EventDao.kt
│   │   └── entities/
│   │       ├── EventEntity.kt
│   │       └── EventTypeEntity.kt
│   ├── repository/
│   │   └── EventRepository.kt
│   └── model/
│       ├── Event.kt
│       └── EventType.kt
├── domain/
│   ├── usecase/
│   │   ├── GetEventsUseCase.kt
│   │   ├── CreateEventUseCase.kt
│   │   ├── UpdateEventUseCase.kt
│   │   └── DeleteEventUseCase.kt
│   └── mapper/
│       └── EventMapper.kt
├── presentation/
│   ├── ui/
│   │   ├── events/
│   │   │   ├── EventListScreen.kt
│   │   │   ├── EventDetailScreen.kt
│   │   │   └── EventFormScreen.kt
│   │   └── components/
│   │       ├── EventCard.kt
│   │       └── SearchBar.kt
│   └── viewmodel/
│       ├── EventListViewModel.kt
│       ├── EventDetailViewModel.kt# Tài liệu Phát triển Tính năng - Ứng dụng Quản lý Sự kiện Cá nhân

## I. Tổng quan Dự án

### Mục tiêu
Xây dựng ứng dụng mobile "Quản lý sự kiện cá nhân" với khả năng lưu trữ offline và đồng bộ dữ liệu.

### Công nghệ sử dụng
- **Android**: Kotlin + Jetpack Compose
- **iOS**: Swift + SwiftUI (tương lai)
- **Backend**: REST API (mock/local JSON)
- **Database**: Room (Android) / CoreData (iOS)
- **Architecture**: MVVM + Repository Pattern

## II. Kiến trúc Hệ thống

### 2.1 Cấu trúc thư mục
```
app/src/main/java/com/xdien/todoevent/
├── data/
│   ├── api/
│   │   ├── EventApi.kt
│   │   └── EventTypeApi.kt
│   ├── database/
│   │   ├── EventDatabase.kt
│   │   ├── EventDao.kt
│   │   └── entities/
│   │       ├── EventEntity.kt
│   │       └── EventTypeEntity.kt
│   ├── repository/
│   │   └── EventRepository.kt
│   └── model/
│       ├── Event.kt
│       └── EventType.kt
├── domain/
│   ├── usecase/
│   │   ├── GetEventsUseCase.kt
│   │   ├── CreateEventUseCase.kt
│   │   ├── UpdateEventUseCase.kt
│   │   └── DeleteEventUseCase.kt
│   └── mapper/
│       └── EventMapper.kt
├── presentation/
│   ├── ui/
│   │   ├── events/
│   │   │   ├── EventListScreen.kt
│   │   │   ├── EventDetailScreen.kt
│   │   │   └── EventFormScreen.kt
│   │   └── components/
│   │       ├── EventCard.kt
│   │       └── SearchBar.kt
│   └── viewmodel/
│       ├── EventListViewModel.kt
│       ├── EventDetailViewModel.kt
│       └── EventFormViewModel.kt
└── utils/
    ├── Constants.kt
    └── Extensions.kt
```

### 2.2 Dependency Injection
Sử dụng Hilt để quản lý dependencies:
```kotlin
@HiltAndroidApp
class TodoEventApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideEventDatabase(@ApplicationContext context: Context): EventDatabase
    
    @Provides
    @Singleton
    fun provideEventRepository(api: EventApi, dao: EventDao): EventRepository
}
```

## III. Data Layer

### 3.1 API Models
```kotlin
// Event.kt
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val dateTime: String,
    val location: String,
    val typeId: String,
    val images: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

// EventType.kt
data class EventType(
    val id: String,
    val name: String,
    val color: String
)
```

### 3.2 Database Entities
```kotlin
// EventEntity.kt
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dateTime: String,
    val location: String,
    val typeId: String,
    val images: String, // JSON array as string
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false
)

// EventTypeEntity.kt
@Entity(tableName = "event_types")
data class EventTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String
)
```

### 3.3 API Interface
```kotlin
// EventApi.kt
interface EventApi {
    @GET("events")
    suspend fun getEvents(
        @Query("q") query: String? = null,
        @Query("typeId") typeId: String? = null
    ): List<Event>
    
    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: String): Event
    
    @POST("events")
    suspend fun createEvent(@Body event: Event): Event
    
    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") id: String, @Body event: Event): Event
    
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String)
    
    @Multipart
    @POST("events/{id}/images")
    suspend fun uploadImages(
        @Path("id") id: String,
        @Part images: List<MultipartBody.Part>
    ): List<String>
}

// EventTypeApi.kt
interface EventTypeApi {
    @GET("event-types")
    suspend fun getEventTypes(): List<EventType>
}
```

## IV. Domain Layer

### 4.1 Use Cases
Tất cả Use Cases phải kế thừa từ abstract class `UseCase<Input, Output>`:

```kotlin
// GetEventsUseCase.kt
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventsInput, Flow<List<Event>>>() {
    
    override suspend fun execute(input: GetEventsInput): Flow<List<Event>> {
        return repository.getEvents(input.query, input.typeId)
    }
}

// CreateEventUseCase.kt
class CreateEventUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<CreateEventInput, Result<Event>>() {
    
    override suspend fun execute(input: CreateEventInput): Result<Event> {
        return repository.createEvent(input.event)
    }
}

// Input data classes
data class GetEventsInput(
    val query: String? = null,
    val typeId: String? = null
)

data class CreateEventInput(
    val event: Event
)

data class UpdateEventInput(
    val eventId: String,
    val event: Event
)

data class DeleteEventInput(
    val eventId: String
)
```

### 4.2 Repository
```kotlin
// EventRepository.kt
@Singleton
class EventRepository @Inject constructor(
    private val api: EventApi,
    private val dao: EventDao,
    private val networkConnectivity: NetworkConnectivity
) {
    fun getEvents(query: String? = null, typeId: String? = null): Flow<List<Event>> {
        return flow {
            // Emit cached data first
            emit(dao.getEvents(query, typeId).map { it.toDomain() })
            
            // Try to fetch from API if online
            if (networkConnectivity.isOnline()) {
                try {
                    val events = api.getEvents(query, typeId)
                    dao.insertEvents(events.map { it.toEntity() })
                    emit(events)
                } catch (e: Exception) {
                    // Keep cached data if API fails
                }
            }
        }
    }
    
    suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val createdEvent = api.createEvent(event)
            dao.insertEvent(createdEvent.toEntity())
            Result.success(createdEvent)
        } catch (e: Exception) {
            // Store locally for later sync
            val localEvent = event.copy(id = UUID.randomUUID().toString(), isSynced = false)
            dao.insertEvent(localEvent.toEntity())
            Result.failure(e)
        }
    }
}
```

## V. Presentation Layer

### 5.1 ViewModels
ViewModels sử dụng UseCase pattern với input/output data classes:

```kotlin
// EventListViewModel.kt
@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTypeId = MutableStateFlow<String?>(null)
    
    init {
        loadEvents()
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        loadEvents()
    }
    
    fun onTypeFilterChange(typeId: String?) {
        _selectedTypeId.value = typeId
        loadEvents()
    }
    
    fun onRefresh() {
        loadEvents()
    }
    
    fun onDeleteEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val input = DeleteEventInput(eventId)
                deleteEventUseCase.execute(input)
                loadEvents() // Reload after deletion
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Không thể xóa sự kiện: ${e.message}"
                )
            }
        }
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val input = GetEventsInput(_searchQuery.value, _selectedTypeId.value)
                getEventsUseCase.execute(input)
                    .catch { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                    .collect { events ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            events = events,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi tải dữ liệu: ${e.message}"
                )
            }
        }
    }
}

// EventListUiState.kt
data class EventListUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val eventTypes: List<EventType> = emptyList()
)
```

### 5.2 UI Screens
```kotlin
// EventListScreen.kt
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            modifier = Modifier.padding(16.dp)
        )
        
        // Type Filter
        EventTypeFilter(
            types = uiState.eventTypes,
            selectedTypeId = uiState.selectedTypeId,
            onTypeSelected = viewModel::onTypeFilterChange
        )
        
        // Event List
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isLoading),
            onRefresh = viewModel::onRefresh
        ) {
            LazyColumn {
                items(uiState.events) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
    
    // FAB for adding new event
    FloatingActionButton(
        onClick = onAddEventClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Event")
    }
}
```

## VI. API Mock Implementation

### 6.1 Mock Server Setup
```kotlin
// MockApiService.kt
class MockApiService {
    private val events = mutableListOf<Event>()
    private val eventTypes = listOf(
        EventType("1", "Công việc", "#FF5722"),
        EventType("2", "Cá nhân", "#2196F3"),
        EventType("3", "Gia đình", "#4CAF50"),
        EventType("4", "Giải trí", "#9C27B0")
    )
    
    suspend fun getEvents(query: String? = null, typeId: String? = null): List<Event> {
        delay(500) // Simulate network delay
        
        return events.filter { event ->
            val matchesQuery = query.isNullOrBlank() || 
                event.title.contains(query, ignoreCase = true)
            val matchesType = typeId.isNullOrBlank() || 
                event.typeId == typeId
            matchesQuery && matchesType
        }
    }
    
    suspend fun createEvent(event: Event): Event {
        delay(300)
        val newEvent = event.copy(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString()
        )
        events.add(newEvent)
        return newEvent
    }
}
```

### 6.2 Network Module
```kotlin
// NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.todoevent.com/") // Mock base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideEventApi(retrofit: Retrofit): EventApi {
        return retrofit.create(EventApi::class.java)
    }
}
```

## VII. Offline Storage

### 7.1 Room Database
```kotlin
// EventDatabase.kt
@Database(
    entities = [EventEntity::class, EventTypeEntity::class],
    version = 1
)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun eventTypeDao(): EventTypeDao
    
    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null
        
        fun getDatabase(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// EventDao.kt
@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE (:query IS NULL OR title LIKE '%' || :query || '%') AND (:typeId IS NULL OR typeId = :typeId) ORDER BY dateTime DESC")
    fun getEvents(query: String? = null, typeId: String? = null): Flow<List<EventEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("SELECT * FROM events WHERE isSynced = 0")
    suspend fun getUnsyncedEvents(): List<EventEntity>
}
```

### 7.2 Sync Service
```kotlin
// SyncService.kt
@AndroidEntryPoint
class SyncService : WorkManager() {
    
    @Inject
    lateinit var eventRepository: EventRepository
    
    override fun doWork(): Result {
        return try {
            // Sync unsynced events
            eventRepository.syncUnsyncedEvents()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncWork = OneTimeWorkRequestBuilder<SyncService>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(syncWork)
        }
    }
}
```

## VIII. Validation & Error Handling

### 8.1 Form Validation
```kotlin
// EventFormValidator.kt
object EventFormValidator {
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Error("Tiêu đề không được để trống")
            title.length > 100 -> ValidationResult.Error("Tiêu đề không được quá 100 ký tự")
            else -> ValidationResult.Success
        }
    }
    
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult.Error("Mô tả không được để trống")
            description.length > 500 -> ValidationResult.Error("Mô tả không được quá 500 ký tự")
            else -> ValidationResult.Success
        }
    }
    
    fun validateDateTime(dateTime: LocalDateTime): ValidationResult {
        return when {
            dateTime.isBefore(LocalDateTime.now()) -> ValidationResult.Error("Thời gian không được trong quá khứ")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

### 8.2 Error Handling
```kotlin
// Result.kt
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// ErrorHandler.kt
object ErrorHandler {
    fun handle(throwable: Throwable): String {
        return when (throwable) {
            is IOException -> "Lỗi kết nối mạng. Vui lòng kiểm tra lại."
            is HttpException -> "Lỗi server. Vui lòng thử lại sau."
            else -> "Đã xảy ra lỗi không xác định."
        }
    }
}
```

## IX. Testing Strategy

### 9.1 Unit Tests
```kotlin
// EventRepositoryTest.kt
@RunWith(MockitoJUnitRunner::class)
class EventRepositoryTest {
    
    @Mock
    private lateinit var api: EventApi
    
    @Mock
    private lateinit var dao: EventDao
    
    @Mock
    private lateinit var networkConnectivity: NetworkConnectivity
    
    private lateinit var repository: EventRepository
    
    @Before
    fun setup() {
        repository = EventRepository(api, dao, networkConnectivity)
    }
    
    @Test
    fun `getEvents should return cached data when offline`() = runTest {
        // Given
        whenever(networkConnectivity.isOnline()).thenReturn(false)
        val cachedEvents = listOf(createMockEvent())
        whenever(dao.getEvents(any(), any())).thenReturn(flowOf(cachedEvents))
        
        // When
        val result = repository.getEvents().first()
        
        // Then
        assertEquals(cachedEvents, result)
    }
}
```

### 9.2 UI Tests
```kotlin
// EventListScreenTest.kt
@RunWith(AndroidJUnit4::class)
class EventListScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun eventListScreen_displaysEvents() {
        // Given
        val events = listOf(createMockEvent())
        val uiState = EventListUiState(events = events)
        
        // When
        composeTestRule.setContent {
            EventListScreen(
                uiState = uiState,
                onEventClick = {},
                onAddEventClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText(events[0].title).assertIsDisplayed()
    }
}
```

## X. Performance Optimization

### 10.1 Image Loading
```kotlin
// ImageLoader.kt
object ImageLoader {
    private val imageCache = LruCache<String, Bitmap>(100)
    
    fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView,
        placeholder: Int = R.drawable.placeholder
    ) {
        // Check cache first
        imageCache.get(url)?.let {
            imageView.setImageBitmap(it)
            return
        }
        
        // Load from network
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = loadBitmapFromUrl(url)
                imageCache.put(url, bitmap)
                
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(placeholder)
                }
            }
        }
    }
}
```

### 10.2 Pagination
```kotlin
// PaginationHelper.kt
class PaginationHelper<T>(
    private val pageSize: Int = 20,
    private val loadMoreThreshold: Int = 5
) {
    private var currentPage = 0
    private var isLoading = false
    private var hasMoreData = true
    
    suspend fun loadNextPage(
        loadFunction: suspend (Int, Int) -> List<T>
    ): List<T> {
        if (isLoading || !hasMoreData) return emptyList()
        
        isLoading = true
        val items = loadFunction(currentPage * pageSize, pageSize)
        
        if (items.size < pageSize) {
            hasMoreData = false
        }
        
        currentPage++
        isLoading = false
        return items
    }
}
```

## XI. Security Considerations

### 11.1 Data Encryption
```kotlin
// EncryptionHelper.kt
object EncryptionHelper {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    
    fun encrypt(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv
        
        return Base64.encodeToString(iv + encryptedBytes, Base64.DEFAULT)
    }
    
    fun decrypt(encryptedData: String, key: SecretKey): String {
        val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = decoded.sliceArray(0..11)
        val encryptedBytes = decoded.sliceArray(12 until decoded.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}
```

### 11.2 Input Sanitization
```kotlin
// InputSanitizer.kt
object InputSanitizer {
    fun sanitizeText(input: String): String {
        return input.trim()
            .replace(Regex("[<>\"']"), "") // Remove potential HTML/script tags
            .replace(Regex("\\s+"), " ") // Normalize whitespace
    }
    
    fun validateImageFile(file: File): Boolean {
        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif")
        val maxSize = 5 * 1024 * 1024 // 5MB
        
        val extension = file.extension.lowercase()
        return allowedExtensions.contains(extension) && file.length() <= maxSize
    }
}
```

## XII. Deployment & CI/CD

### 12.1 Build Configuration
```kotlin
// build.gradle.kts (app level)
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            debuggable = true
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.todoevent.com/\"")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"https://api.todoevent.com/\"")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    signingConfigs {
        create("release") {
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
            storeFile = file(System.getenv("KEYSTORE_PATH"))
            storePassword = System.getenv("STORE_PASSWORD")
        }
    }
}
```

### 12.2 GitHub Actions Workflow
```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run instrumented tests
      run: ./gradlew connectedAndroidTest
    
    - name: Build debug APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## XIII. Monitoring & Analytics

### 13.1 Crash Reporting
```kotlin
// CrashReporter.kt
object CrashReporter {
    fun init(context: Context) {
        // Initialize crash reporting service (e.g., Firebase Crashlytics)
    }
    
    fun logException(throwable: Throwable) {
        // Log exception to crash reporting service
    }
    
    fun logEvent(eventName: String, parameters: Map<String, String> = emptyMap()) {
        // Log custom events for analytics
    }
}
```

### 13.2 Performance Monitoring
```kotlin
// PerformanceMonitor.kt
object PerformanceMonitor {
    fun startTrace(traceName: String) {
        // Start performance trace
    }
    
    fun endTrace(traceName: String) {
        // End performance trace
    }
    
    fun addMetric(traceName: String, metricName: String, value: Long) {
        // Add custom metric to trace
    }
}
```

## XIV. Documentation Standards

### 14.1 Code Documentation
```kotlin
/**
 * Repository class for managing event data operations.
 * 
 * This class handles both local database operations and remote API calls,
 * providing a unified interface for event data management with offline support.
 * 
 * @param api The remote API service for event operations
 * @param dao The local database access object
 * @param networkConnectivity Network connectivity checker
 */
@Singleton
class EventRepository @Inject constructor(
    private val api: EventApi,
    private val dao: EventDao,
    private val networkConnectivity: NetworkConnectivity
) {
    /**
     * Retrieves events from local cache and optionally syncs with remote API.
     * 
     * @param query Optional search query to filter events by title
     * @param typeId Optional event type filter
     * @return Flow of event lists, emitting cached data first then updated data
     */
    fun getEvents(query: String? = null, typeId: String? = null): Flow<List<Event>> {
        // Implementation
    }
}
```

### 14.2 API Documentation
```kotlin
/**
 * API interface for event-related operations.
 * 
 * All endpoints follow RESTful conventions and return JSON responses.
 * Authentication is handled via Bearer token in Authorization header.
 */
interface EventApi {
    /**
     * Retrieves a list of events with optional filtering.
     * 
     * @param query Optional search term to filter events by title
     * @param typeId Optional event type ID to filter events
     * @return List of events matching the criteria
     * 
     * @throws HttpException 401 if unauthorized
     * @throws HttpException 500 if server error
     */
    @GET("events")
    suspend fun getEvents(
        @Query("q") query: String? = null,
        @Query("typeId") typeId: String? = null
    ): List<Event>
}
```

## XV. Future Enhancements

### 15.1 Planned Features
1. **Push Notifications**: Remind users about upcoming events
2. **Calendar Integration**: Sync with device calendar
3. **Location Services**: Add event location on map
4. **Social Features**: Share events with friends
5. **Recurring Events**: Support for recurring event patterns
6. **Event Templates**: Pre-defined event templates
7. **Multi-language Support**: Internationalization
8. **Dark Mode**: Theme support

### 15.2 Technical Improvements
1. **GraphQL Migration**: Replace REST API with GraphQL
2. **Real-time Sync**: WebSocket for real-time updates
3. **Advanced Caching**: Redis for better performance
4. **Microservices**: Break down monolithic backend
5. **Containerization**: Docker deployment
6. **Kubernetes**: Container orchestration
7. **Monitoring**: Prometheus + Grafana
8. **Logging**: ELK stack integration

---

## XVI. Clean Architecture Best Practices

### 16.1 UseCase Implementation Guidelines

#### Tất cả Use Cases phải tuân thủ các quy tắc sau:

1. **Kế thừa từ base class**: Mọi UseCase phải extend `UseCase<Input, Output>`
2. **Input/Output data classes**: Sử dụng data classes riêng biệt cho input và output
3. **Single Responsibility**: Mỗi UseCase chỉ thực hiện một chức năng cụ thể
4. **Dependency Injection**: Inject dependencies thông qua constructor
5. **Error Handling**: Xử lý lỗi và throw exceptions khi cần thiết

```kotlin
// ✅ Correct UseCase implementation
class GetEventByIdUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventByIdInput, Event>() {
    
    override suspend fun execute(input: GetEventByIdInput): Event {
        return repository.getEventById(input.eventId)
            ?: throw EventNotFoundException("Event not found: ${input.eventId}")
    }
}

data class GetEventByIdInput(val eventId: String)

// ❌ Wrong - không kế thừa UseCase
class GetEventByIdUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(eventId: String): Event {
        return repository.getEventById(eventId) ?: throw EventNotFoundException()
    }
}
```

#### UseCase Categories:

1. **Query UseCases**: Chỉ đọc dữ liệu, không thay đổi state
   ```kotlin
   class GetEventsUseCase : UseCase<GetEventsInput, Flow<List<Event>>>
   class GetEventByIdUseCase : UseCase<GetEventByIdInput, Event>
   class SearchEventsUseCase : UseCase<SearchEventsInput, List<Event>>
   ```

2. **Command UseCases**: Thay đổi dữ liệu, có thể có side effects
   ```kotlin
   class CreateEventUseCase : UseCase<CreateEventInput, Result<Event>>
   class UpdateEventUseCase : UseCase<UpdateEventInput, Result<Event>>
   class DeleteEventUseCase : UseCase<DeleteEventInput, Unit>
   ```

3. **Complex UseCases**: Kết hợp nhiều operations
   ```kotlin
   class SyncEventsUseCase : UseCase<SyncEventsInput, SyncResult>
   class ExportEventsUseCase : UseCase<ExportEventsInput, ExportResult>
   ```

### 16.2 Layer Separation Rules

#### Domain Layer (Core Business Logic)
- **Không phụ thuộc** vào bất kỳ framework nào
- Chứa entities, use cases, repository interfaces
- Định nghĩa business rules và validation logic

```kotlin
// Domain entities - pure business objects
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val dateTime: LocalDateTime,
    val location: String,
    val typeId: String
) {
    fun isValid(): Boolean {
        return title.isNotBlank() && 
               description.isNotBlank() && 
               dateTime.isAfter(LocalDateTime.now())
    }
}
```

#### Data Layer (Implementation)
- Implement repository interfaces từ domain layer
- Chứa data sources (API, Database)
- Chuyển đổi giữa data models và domain entities

```kotlin
// Repository implementation
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val api: EventApi,
    private val dao: EventDao,
    private val mapper: EventMapper
) : EventRepository {
    
    override suspend fun getEvents(query: String?, typeId: String?): Flow<List<Event>> {
        return dao.getEvents(query, typeId).map { entities ->
            entities.map { mapper.toDomain(it) }
        }
    }
}
```

#### Presentation Layer (UI)
- Chỉ chứa UI logic và state management
- Sử dụng ViewModels để giao tiếp với domain layer
- Không chứa business logic

```kotlin
// ViewModel - chỉ quản lý UI state
@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {
    
    fun loadEvents(query: String? = null) {
        viewModelScope.launch {
            try {
                val input = GetEventsInput(query)
                val events = getEventsUseCase.execute(input)
                _uiState.value = EventListUiState.Success(events)
            } catch (e: Exception) {
                _uiState.value = EventListUiState.Error(e.message)
            }
        }
    }
}
```

### 16.3 Dependency Injection với Hilt

#### Module Organization:
```kotlin
// Domain Module - UseCases
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    
    @Provides
    @Singleton
    fun provideGetEventsUseCase(repository: EventRepository): GetEventsUseCase {
        return GetEventsUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideCreateEventUseCase(repository: EventRepository): CreateEventUseCase {
        return CreateEventUseCase(repository)
    }
}

// Data Module - Repositories và Data Sources
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideEventRepository(
        api: EventApi,
        dao: EventDao,
        mapper: EventMapper
    ): EventRepository {
        return EventRepositoryImpl(api, dao, mapper)
    }
}
```

### 16.4 Testing Strategy cho Clean Architecture

#### Unit Tests cho UseCases:
```kotlin
@RunWith(MockitoJUnitRunner::class)
class GetEventsUseCaseTest {
    
    @Mock
    private lateinit var repository: EventRepository
    
    private lateinit var useCase: GetEventsUseCase
    
    @Before
    fun setup() {
        useCase = GetEventsUseCase(repository)
    }
    
    @Test
    fun `execute should return events from repository`() = runTest {
        // Given
        val input = GetEventsInput(query = "test")
        val expectedEvents = listOf(createMockEvent())
        whenever(repository.getEvents(input.query, input.typeId))
            .thenReturn(flowOf(expectedEvents))
        
        // When
        val result = useCase.execute(input)
        
        // Then
        assertEquals(expectedEvents, result.first())
    }
}
```

#### Integration Tests:
```kotlin
@HiltAndroidTest
class EventRepositoryIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: EventRepository
    
    @Inject
    lateinit var database: EventDatabase
    
    @Test
    fun `repository should save and retrieve events`() = runTest {
        // Given
        val event = createTestEvent()
        
        // When
        repository.createEvent(event)
        val retrievedEvents = repository.getEvents().first()
        
        // Then
        assertTrue(retrievedEvents.contains(event))
    }
}
```

---

## Kết luận

Tài liệu này cung cấp hướng dẫn chi tiết để phát triển ứng dụng "Quản lý sự kiện cá nhân" với Clean Architecture, Jetpack Compose v2, offline support, và các best practices hiện đại. Dự án được thiết kế để dễ dàng mở rộng và bảo trì trong tương lai.

### Liên hệ
- **Developer**: xdien
- **Project**: TodoEvent
- **Version**: 1.0.0
- **Last Updated**: 2024 
    fun provideEventRepository(api: EventApi, dao: EventDao): EventRepository
}
```

## III. Data Layer

### 3.1 API Models
```kotlin
// Event.kt
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val dateTime: String,
    val location: String,
    val typeId: String,
    val images: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

// EventType.kt
data class EventType(
    val id: String,
    val name: String,
    val color: String
)
```

### 3.2 Database Entities
```kotlin
// EventEntity.kt
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dateTime: String,
    val location: String,
    val typeId: String,
    val images: String, // JSON array as string
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false
)

// EventTypeEntity.kt
@Entity(tableName = "event_types")
data class EventTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String
)
```

### 3.3 API Interface
```kotlin
// EventApi.kt
interface EventApi {
    @GET("events")
    suspend fun getEvents(
        @Query("q") query: String? = null,
        @Query("typeId") typeId: String? = null
    ): List<Event>
    
    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: String): Event
    
    @POST("events")
    suspend fun createEvent(@Body event: Event): Event
    
    @PUT("events/{id}")
    suspend fun updateEvent(@Path("id") id: String, @Body event: Event): Event
    
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: String)
    
    @Multipart
    @POST("events/{id}/images")
    suspend fun uploadImages(
        @Path("id") id: String,
        @Part images: List<MultipartBody.Part>
    ): List<String>
}

// EventTypeApi.kt
interface EventTypeApi {
    @GET("event-types")
    suspend fun getEventTypes(): List<EventType>
}
```

## IV. Domain Layer

### 4.1 Use Cases
```kotlin
// GetEventsUseCase.kt
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(query: String? = null, typeId: String? = null): Flow<List<Event>> {
        return repository.getEvents(query, typeId)
    }
}

// CreateEventUseCase.kt
class CreateEventUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(event: Event): Result<Event> {
        return repository.createEvent(event)
    }
}
```

### 4.2 Repository
```kotlin
// EventRepository.kt
@Singleton
class EventRepository @Inject constructor(
    private val api: EventApi,
    private val dao: EventDao,
    private val networkConnectivity: NetworkConnectivity
) {
    fun getEvents(query: String? = null, typeId: String? = null): Flow<List<Event>> {
        return flow {
            // Emit cached data first
            emit(dao.getEvents(query, typeId).map { it.toDomain() })
            
            // Try to fetch from API if online
            if (networkConnectivity.isOnline()) {
                try {
                    val events = api.getEvents(query, typeId)
                    dao.insertEvents(events.map { it.toEntity() })
                    emit(events)
                } catch (e: Exception) {
                    // Keep cached data if API fails
                }
            }
        }
    }
    
    suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val createdEvent = api.createEvent(event)
            dao.insertEvent(createdEvent.toEntity())
            Result.success(createdEvent)
        } catch (e: Exception) {
            // Store locally for later sync
            val localEvent = event.copy(id = UUID.randomUUID().toString(), isSynced = false)
            dao.insertEvent(localEvent.toEntity())
            Result.failure(e)
        }
    }
}
```

## V. Presentation Layer

### 5.1 ViewModels
```kotlin
// EventListViewModel.kt
@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTypeId = MutableStateFlow<String?>(null)
    
    init {
        loadEvents()
    }
    
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        loadEvents()
    }
    
    fun onTypeFilterChange(typeId: String?) {
        _selectedTypeId.value = typeId
        loadEvents()
    }
    
    fun onRefresh() {
        loadEvents()
    }
    
    private fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getEventsUseCase(_searchQuery.value, _selectedTypeId.value)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
                .collect { events ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        events = events,
                        error = null
                    )
                }
        }
    }
}

// EventListUiState.kt
data class EventListUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val eventTypes: List<EventType> = emptyList()
)
```

### 5.2 UI Screens
```kotlin
// EventListScreen.kt
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            modifier = Modifier.padding(16.dp)
        )
        
        // Type Filter
        EventTypeFilter(
            types = uiState.eventTypes,
            selectedTypeId = uiState.selectedTypeId,
            onTypeSelected = viewModel::onTypeFilterChange
        )
        
        // Event List
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isLoading),
            onRefresh = viewModel::onRefresh
        ) {
            LazyColumn {
                items(uiState.events) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
    
    // FAB for adding new event
    FloatingActionButton(
        onClick = onAddEventClick,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Event")
    }
}
```

## VI. API Mock Implementation

### 6.1 Mock Server Setup
```kotlin
// MockApiService.kt
class MockApiService {
    private val events = mutableListOf<Event>()
    private val eventTypes = listOf(
        EventType("1", "Công việc", "#FF5722"),
        EventType("2", "Cá nhân", "#2196F3"),
        EventType("3", "Gia đình", "#4CAF50"),
        EventType("4", "Giải trí", "#9C27B0")
    )
    
    suspend fun getEvents(query: String? = null, typeId: String? = null): List<Event> {
        delay(500) // Simulate network delay
        
        return events.filter { event ->
            val matchesQuery = query.isNullOrBlank() || 
                event.title.contains(query, ignoreCase = true)
            val matchesType = typeId.isNullOrBlank() || 
                event.typeId == typeId
            matchesQuery && matchesType
        }
    }
    
    suspend fun createEvent(event: Event): Event {
        delay(300)
        val newEvent = event.copy(
            id = UUID.randomUUID().toString(),
            createdAt = Instant.now().toString(),
            updatedAt = Instant.now().toString()
        )
        events.add(newEvent)
        return newEvent
    }
}
```

### 6.2 Network Module
```kotlin
// NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.todoevent.com/") // Mock base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideEventApi(retrofit: Retrofit): EventApi {
        return retrofit.create(EventApi::class.java)
    }
}
```

## VII. Offline Storage

### 7.1 Room Database
```kotlin
// EventDatabase.kt
@Database(
    entities = [EventEntity::class, EventTypeEntity::class],
    version = 1
)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun eventTypeDao(): EventTypeDao
    
    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null
        
        fun getDatabase(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// EventDao.kt
@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE (:query IS NULL OR title LIKE '%' || :query || '%') AND (:typeId IS NULL OR typeId = :typeId) ORDER BY dateTime DESC")
    fun getEvents(query: String? = null, typeId: String? = null): Flow<List<EventEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("SELECT * FROM events WHERE isSynced = 0")
    suspend fun getUnsyncedEvents(): List<EventEntity>
}
```

### 7.2 Sync Service
```kotlin
// SyncService.kt
@AndroidEntryPoint
class SyncService : WorkManager() {
    
    @Inject
    lateinit var eventRepository: EventRepository
    
    override fun doWork(): Result {
        return try {
            // Sync unsynced events
            eventRepository.syncUnsyncedEvents()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncWork = OneTimeWorkRequestBuilder<SyncService>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueue(syncWork)
        }
    }
}
```

## VIII. Validation & Error Handling

### 8.1 Form Validation
```kotlin
// EventFormValidator.kt
object EventFormValidator {
    fun validateTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.Error("Tiêu đề không được để trống")
            title.length > 100 -> ValidationResult.Error("Tiêu đề không được quá 100 ký tự")
            else -> ValidationResult.Success
        }
    }
    
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult.Error("Mô tả không được để trống")
            description.length > 500 -> ValidationResult.Error("Mô tả không được quá 500 ký tự")
            else -> ValidationResult.Success
        }
    }
    
    fun validateDateTime(dateTime: LocalDateTime): ValidationResult {
        return when {
            dateTime.isBefore(LocalDateTime.now()) -> ValidationResult.Error("Thời gian không được trong quá khứ")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
```

### 8.2 Error Handling
```kotlin
// Result.kt
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// ErrorHandler.kt
object ErrorHandler {
    fun handle(throwable: Throwable): String {
        return when (throwable) {
            is IOException -> "Lỗi kết nối mạng. Vui lòng kiểm tra lại."
            is HttpException -> "Lỗi server. Vui lòng thử lại sau."
            else -> "Đã xảy ra lỗi không xác định."
        }
    }
}
```

## IX. Testing Strategy

### 9.1 Unit Tests
```kotlin
// EventRepositoryTest.kt
@RunWith(MockitoJUnitRunner::class)
class EventRepositoryTest {
    
    @Mock
    private lateinit var api: EventApi
    
    @Mock
    private lateinit var dao: EventDao
    
    @Mock
    private lateinit var networkConnectivity: NetworkConnectivity
    
    private lateinit var repository: EventRepository
    
    @Before
    fun setup() {
        repository = EventRepository(api, dao, networkConnectivity)
    }
    
    @Test
    fun `getEvents should return cached data when offline`() = runTest {
        // Given
        whenever(networkConnectivity.isOnline()).thenReturn(false)
        val cachedEvents = listOf(createMockEvent())
        whenever(dao.getEvents(any(), any())).thenReturn(flowOf(cachedEvents))
        
        // When
        val result = repository.getEvents().first()
        
        // Then
        assertEquals(cachedEvents, result)
    }
}
```

### 9.2 UI Tests
```kotlin
// EventListScreenTest.kt
@RunWith(AndroidJUnit4::class)
class EventListScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun eventListScreen_displaysEvents() {
        // Given
        val events = listOf(createMockEvent())
        val uiState = EventListUiState(events = events)
        
        // When
        composeTestRule.setContent {
            EventListScreen(
                uiState = uiState,
                onEventClick = {},
                onAddEventClick = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText(events[0].title).assertIsDisplayed()
    }
}
```

## X. Performance Optimization

### 10.1 Image Loading
```kotlin
// ImageLoader.kt
object ImageLoader {
    private val imageCache = LruCache<String, Bitmap>(100)
    
    fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView,
        placeholder: Int = R.drawable.placeholder
    ) {
        // Check cache first
        imageCache.get(url)?.let {
            imageView.setImageBitmap(it)
            return
        }
        
        // Load from network
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = loadBitmapFromUrl(url)
                imageCache.put(url, bitmap)
                
                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(placeholder)
                }
            }
        }
    }
}
```

### 10.2 Pagination
```kotlin
// PaginationHelper.kt
class PaginationHelper<T>(
    private val pageSize: Int = 20,
    private val loadMoreThreshold: Int = 5
) {
    private var currentPage = 0
    private var isLoading = false
    private var hasMoreData = true
    
    suspend fun loadNextPage(
        loadFunction: suspend (Int, Int) -> List<T>
    ): List<T> {
        if (isLoading || !hasMoreData) return emptyList()
        
        isLoading = true
        val items = loadFunction(currentPage * pageSize, pageSize)
        
        if (items.size < pageSize) {
            hasMoreData = false
        }
        
        currentPage++
        isLoading = false
        return items
    }
}
```

## XI. Security Considerations

### 11.1 Data Encryption
```kotlin
// EncryptionHelper.kt
object EncryptionHelper {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    
    fun encrypt(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        val iv = cipher.iv
        
        return Base64.encodeToString(iv + encryptedBytes, Base64.DEFAULT)
    }
    
    fun decrypt(encryptedData: String, key: SecretKey): String {
        val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = decoded.sliceArray(0..11)
        val encryptedBytes = decoded.sliceArray(12 until decoded.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}
```

### 11.2 Input Sanitization
```kotlin
// InputSanitizer.kt
object InputSanitizer {
    fun sanitizeText(input: String): String {
        return input.trim()
            .replace(Regex("[<>\"']"), "") // Remove potential HTML/script tags
            .replace(Regex("\\s+"), " ") // Normalize whitespace
    }
    
    fun validateImageFile(file: File): Boolean {
        val allowedExtensions = listOf("jpg", "jpeg", "png", "gif")
        val maxSize = 5 * 1024 * 1024 // 5MB
        
        val extension = file.extension.lowercase()
        return allowedExtensions.contains(extension) && file.length() <= maxSize
    }
}
```

## XII. Deployment & CI/CD

### 12.1 Build Configuration
```kotlin
// build.gradle.kts (app level)
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            debuggable = true
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.todoevent.com/\"")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "API_BASE_URL", "\"https://api.todoevent.com/\"")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    signingConfigs {
        create("release") {
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
            storeFile = file(System.getenv("KEYSTORE_PATH"))
            storePassword = System.getenv("STORE_PASSWORD")
        }
    }
}
```

### 12.2 GitHub Actions Workflow
```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run instrumented tests
      run: ./gradlew connectedAndroidTest
    
    - name: Build debug APK
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## XIII. Monitoring & Analytics

### 13.1 Crash Reporting
```kotlin
// CrashReporter.kt
object CrashReporter {
    fun init(context: Context) {
        // Initialize crash reporting service (e.g., Firebase Crashlytics)
    }
    
    fun logException(throwable: Throwable) {
        // Log exception to crash reporting service
    }
    
    fun logEvent(eventName: String, parameters: Map<String, String> = emptyMap()) {
        // Log custom events for analytics
    }
}
```

### 13.2 Performance Monitoring
```kotlin
// PerformanceMonitor.kt
object PerformanceMonitor {
    fun startTrace(traceName: String) {
        // Start performance trace
    }
    
    fun endTrace(traceName: String) {
        // End performance trace
    }
    
    fun addMetric(traceName: String, metricName: String, value: Long) {
        // Add custom metric to trace
    }
}
```

## XIV. Documentation Standards

### 14.1 Code Documentation
```kotlin
/**
 * Repository class for managing event data operations.
 * 
 * This class handles both local database operations and remote API calls,
 * providing a unified interface for event data management with offline support.
 * 
 * @param api The remote API service for event operations
 * @param dao The local database access object
 * @param networkConnectivity Network connectivity checker
 */
@Singleton
class EventRepository @Inject constructor(
    private val api: EventApi,
    private val dao: EventDao,
    private val networkConnectivity: NetworkConnectivity
) {
    /**
     * Retrieves events from local cache and optionally syncs with remote API.
     * 
     * @param query Optional search query to filter events by title
     * @param typeId Optional event type filter
     * @return Flow of event lists, emitting cached data first then updated data
     */
    fun getEvents(query: String? = null, typeId: String? = null): Flow<List<Event>> {
        // Implementation
    }
}
```

### 14.2 API Documentation
```kotlin
/**
 * API interface for event-related operations.
 * 
 * All endpoints follow RESTful conventions and return JSON responses.
 * Authentication is handled via Bearer token in Authorization header.
 */
interface EventApi {
    /**
     * Retrieves a list of events with optional filtering.
     * 
     * @param query Optional search term to filter events by title
     * @param typeId Optional event type ID to filter events
     * @return List of events matching the criteria
     * 
     * @throws HttpException 401 if unauthorized
     * @throws HttpException 500 if server error
     */
    @GET("events")
    suspend fun getEvents(
        @Query("q") query: String? = null,
        @Query("typeId") typeId: String? = null
    ): List<Event>
}
```

## XV. Future Enhancements

### 15.1 Planned Features
1. **Push Notifications**: Remind users about upcoming events
2. **Calendar Integration**: Sync with device calendar
3. **Location Services**: Add event location on map
4. **Social Features**: Share events with friends
5. **Recurring Events**: Support for recurring event patterns
6. **Event Templates**: Pre-defined event templates
7. **Multi-language Support**: Internationalization
8. **Dark Mode**: Theme support

### 15.2 Technical Improvements
1. **GraphQL Migration**: Replace REST API with GraphQL
2. **Real-time Sync**: WebSocket for real-time updates
3. **Advanced Caching**: Redis for better performance
4. **Microservices**: Break down monolithic backend
5. **Containerization**: Docker deployment
6. **Kubernetes**: Container orchestration
7. **Monitoring**: Prometheus + Grafana
8. **Logging**: ELK stack integration

---

## Kết luận

Tài liệu này cung cấp hướng dẫn chi tiết để phát triển ứng dụng "Quản lý sự kiện cá nhân" với kiến trúc MVVM, offline support, và các best practices hiện đại. Dự án được thiết kế để dễ dàng mở rộng và bảo trì trong tương lai.

### Liên hệ
- **Developer**: xdien
- **Project**: TodoEvent
- **Version**: 1.0.0
- **Last Updated**: 2024 