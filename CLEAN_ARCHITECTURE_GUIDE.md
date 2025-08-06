# Clean Architecture & UseCase Pattern Guide

## Tổng quan

Dự án TodoEvent sử dụng Clean Architecture với Jetpack Compose v2 và tuân thủ UseCase pattern. Tài liệu này cung cấp hướng dẫn chi tiết về cách implement Clean Architecture trong dự án.

## I. Clean Architecture Layers

### 1. Presentation Layer (UI)
- **Mục đích**: Hiển thị UI và xử lý user interactions
- **Thành phần**: 
  - Jetpack Compose Screens
  - ViewModels
  - UI State classes
- **Dependency**: Chỉ phụ thuộc vào Domain Layer

### 2. Domain Layer (Business Logic)
- **Mục đích**: Chứa business logic và rules
- **Thành phần**:
  - Use Cases (kế thừa từ `UseCase<Input, Output>`)
  - Domain Entities
  - Repository Interfaces
- **Dependency**: Không phụ thuộc vào bất kỳ framework nào

### 3. Data Layer (Data Access)
- **Mục đích**: Quản lý data access và storage
- **Thành phần**:
  - Repository Implementations
  - Data Sources (API, Database)
  - Data Models (DTOs, Entities)
- **Dependency**: Phụ thuộc vào Domain Layer

## II. UseCase Pattern Implementation

### Base UseCase Class
```kotlin
// common/UseCase.kt
abstract class UseCase<Input, Output> {
    @Throws(Exception::class)
    abstract suspend fun execute(input: Input): Output
}
```

### UseCase Implementation Rules

#### 1. Kế thừa từ UseCase base class
```kotlin
// ✅ Correct
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventsInput, Flow<List<Event>>>() {
    
    override suspend fun execute(input: GetEventsInput): Flow<List<Event>> {
        return repository.getEvents(input.query, input.typeId)
    }
}

// ❌ Wrong
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(query: String?): Flow<List<Event>> {
        return repository.getEvents(query, null)
    }
}
```

#### 2. Sử dụng Input/Output data classes
```kotlin
// Input data class
data class GetEventsInput(
    val query: String? = null,
    val typeId: String? = null
)

// Output là return type của execute method
// Flow<List<Event>> trong ví dụ trên
```

#### 3. Single Responsibility Principle
```kotlin
// ✅ Good - Mỗi UseCase chỉ làm một việc
class GetEventsUseCase : UseCase<GetEventsInput, Flow<List<Event>>>
class CreateEventUseCase : UseCase<CreateEventInput, Result<Event>>
class DeleteEventUseCase : UseCase<DeleteEventInput, Unit>

// ❌ Bad - UseCase làm nhiều việc
class EventManagementUseCase : UseCase<EventManagementInput, EventManagementOutput>
```

### UseCase Categories

#### 1. Query UseCases (Read-only)
```kotlin
class GetEventsUseCase : UseCase<GetEventsInput, Flow<List<Event>>>
class GetEventByIdUseCase : UseCase<GetEventByIdInput, Event>
class SearchEventsUseCase : UseCase<SearchEventsInput, List<Event>>
class GetEventTypesUseCase : UseCase<Unit, List<EventType>>
```

#### 2. Command UseCases (Write operations)
```kotlin
class CreateEventUseCase : UseCase<CreateEventInput, Result<Event>>
class UpdateEventUseCase : UseCase<UpdateEventInput, Result<Event>>
class DeleteEventUseCase : UseCase<DeleteEventInput, Unit>
class UploadEventImageUseCase : UseCase<UploadImageInput, Result<String>>
```

#### 3. Complex UseCases (Multiple operations)
```kotlin
class SyncEventsUseCase : UseCase<SyncEventsInput, SyncResult>
class ExportEventsUseCase : UseCase<ExportEventsInput, ExportResult>
class BackupEventsUseCase : UseCase<BackupInput, BackupResult>
```

## III. Dependency Injection với Hilt

### Module Organization

#### Domain Module
```kotlin
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
    
    @Provides
    @Singleton
    fun provideUpdateEventUseCase(repository: EventRepository): UpdateEventUseCase {
        return UpdateEventUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideDeleteEventUseCase(repository: EventRepository): DeleteEventUseCase {
        return DeleteEventUseCase(repository)
    }
}
```

#### Data Module
```kotlin
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
    
    @Provides
    @Singleton
    fun provideEventApi(retrofit: Retrofit): EventApi {
        return retrofit.create(EventApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideEventDao(database: EventDatabase): EventDao {
        return database.eventDao()
    }
}
```

## IV. ViewModel Implementation

### Sử dụng UseCase trong ViewModel
```kotlin
@HiltViewModel
class EventListViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val deleteEventUseCase: DeleteEventUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()
    
    fun loadEvents(query: String? = null, typeId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val input = GetEventsInput(query, typeId)
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
    
    fun deleteEvent(eventId: String) {
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
}
```

## V. Error Handling

### UseCase Error Handling
```kotlin
class GetEventByIdUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventByIdInput, Event>() {
    
    override suspend fun execute(input: GetEventByIdInput): Event {
        return try {
            repository.getEventById(input.eventId)
                ?: throw EventNotFoundException("Event not found: ${input.eventId}")
        } catch (e: Exception) {
            when (e) {
                is EventNotFoundException -> throw e
                is IOException -> throw NetworkException("Network error: ${e.message}")
                else -> throw UnknownException("Unknown error: ${e.message}")
            }
        }
    }
}
```

### Custom Exceptions
```kotlin
class EventNotFoundException(message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message)
class UnknownException(message: String) : Exception(message)
```

## VI. Testing Strategy

### Unit Tests cho UseCases
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
    
    @Test
    fun `execute should throw exception when repository fails`() = runTest {
        // Given
        val input = GetEventsInput(query = "test")
        val exception = IOException("Network error")
        whenever(repository.getEvents(input.query, input.typeId))
            .thenThrow(exception)
        
        // When & Then
        assertThrows(IOException::class.java) {
            useCase.execute(input)
        }
    }
}
```

### Integration Tests
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

## VII. Best Practices

### 1. Naming Conventions
```kotlin
// UseCase names
GetEventsUseCase
CreateEventUseCase
UpdateEventUseCase
DeleteEventUseCase

// Input/Output names
GetEventsInput
CreateEventInput
UpdateEventInput
DeleteEventInput
```

### 2. File Organization
```
domain/
├── usecase/
│   ├── GetEventsUseCase.kt
│   ├── CreateEventUseCase.kt
│   ├── UpdateEventUseCase.kt
│   └── DeleteEventUseCase.kt
├── entity/
│   └── Event.kt
└── repository/
    └── EventRepository.kt
```

### 3. Documentation
```kotlin
/**
 * UseCase để lấy danh sách sự kiện với optional filtering.
 * 
 * @param repository Repository để truy cập dữ liệu sự kiện
 */
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventsInput, Flow<List<Event>>>() {
    
    /**
     * Thực thi UseCase để lấy danh sách sự kiện.
     * 
     * @param input Input parameters chứa query và typeId
     * @return Flow của danh sách sự kiện
     * @throws IOException Khi có lỗi network
     * @throws Exception Khi có lỗi khác
     */
    override suspend fun execute(input: GetEventsInput): Flow<List<Event>> {
        return repository.getEvents(input.query, input.typeId)
    }
}
```

## VIII. Migration Guide

### Từ MVVM sang Clean Architecture

#### Bước 1: Tạo UseCase base class
```kotlin
// common/UseCase.kt
abstract class UseCase<Input, Output> {
    @Throws(Exception::class)
    abstract suspend fun execute(input: Input): Output
}
```

#### Bước 2: Refactor existing UseCases
```kotlin
// Before
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(query: String?): Flow<List<Event>> {
        return repository.getEvents(query, null)
    }
}

// After
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventsInput, Flow<List<Event>>>() {
    
    override suspend fun execute(input: GetEventsInput): Flow<List<Event>> {
        return repository.getEvents(input.query, input.typeId)
    }
}

data class GetEventsInput(
    val query: String? = null,
    val typeId: String? = null
)
```

#### Bước 3: Update ViewModels
```kotlin
// Before
fun loadEvents(query: String?) {
    viewModelScope.launch {
        getEventsUseCase(query)
            .collect { events ->
                _uiState.value = _uiState.value.copy(events = events)
            }
    }
}

// After
fun loadEvents(query: String?, typeId: String? = null) {
    viewModelScope.launch {
        try {
            val input = GetEventsInput(query, typeId)
            getEventsUseCase.execute(input)
                .collect { events ->
                    _uiState.value = _uiState.value.copy(events = events)
                }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }
}
```

## IX. Common Patterns

### 1. Result Pattern
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class CreateEventUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<CreateEventInput, Result<Event>>() {
    
    override suspend fun execute(input: CreateEventInput): Result<Event> {
        return try {
            val event = repository.createEvent(input.event)
            Result.Success(event)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

### 2. Flow Pattern
```kotlin
class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) : UseCase<GetEventsInput, Flow<List<Event>>>() {
    
    override suspend fun execute(input: GetEventsInput): Flow<List<Event>> {
        return repository.getEvents(input.query, input.typeId)
            .catch { error ->
                emit(emptyList()) // Fallback to empty list
            }
    }
}
```

### 3. Validation Pattern
```kotlin
class CreateEventUseCase @Inject constructor(
    private val repository: EventRepository,
    private val validator: EventValidator
) : UseCase<CreateEventInput, Result<Event>>() {
    
    override suspend fun execute(input: CreateEventInput): Result<Event> {
        // Validate input
        val validationResult = validator.validate(input.event)
        if (!validationResult.isValid) {
            return Result.Error(ValidationException(validationResult.error))
        }
        
        // Create event
        return try {
            val event = repository.createEvent(input.event)
            Result.Success(event)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

---

## Kết luận

Clean Architecture với UseCase pattern giúp dự án có cấu trúc rõ ràng, dễ test và maintain. Việc tuân thủ các quy tắc trong tài liệu này sẽ đảm bảo code quality và scalability của dự án.

### Liên hệ
- **Developer**: xdien
- **Project**: TodoEvent
- **Version**: 1.0.0
- **Last Updated**: 2024 