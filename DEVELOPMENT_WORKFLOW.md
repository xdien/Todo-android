# Development Workflow - TodoEvent

## 📋 Tổng quan

Tài liệu này mô tả quy trình phát triển, coding standards, và best practices cho dự án TodoEvent.

## 🏗 Kiến trúc Dự án

### Clean Architecture
```
┌─────────────────────────────────────┐
│           Presentation              │
│  (UI, ViewModels, Composables)     │
├─────────────────────────────────────┤
│             Domain                  │
│  (Use Cases, Entities, Interfaces) │
├─────────────────────────────────────┤
│              Data                   │
│  (Repository, API, Database)       │
└─────────────────────────────────────┘
```

### Dependency Flow
```
UI → ViewModel → UseCase → Repository → API/Database
```

## 🔄 Git Workflow

### Branch Strategy
```
main (production)
├── develop (integration)
│   ├── feature/event-list
│   ├── feature/event-detail
│   ├── feature/event-form
│   └── feature/offline-sync
├── hotfix/critical-bug
└── release/v1.0.0
```

### Commit Convention
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test changes
- `chore`: Build/tool changes

**Examples**:
```bash
feat(events): add event list screen with search functionality
fix(auth): resolve token refresh issue
docs(api): update API documentation
refactor(repository): extract common repository logic
test(viewmodel): add unit tests for EventListViewModel
```

### Pull Request Process
1. **Create Feature Branch**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/event-list
   ```

2. **Development**
   - Write code following coding standards
   - Add unit tests
   - Update documentation
   - Test locally

3. **Create Pull Request**
   - Title: `feat: add event list screen`
   - Description: Include what, why, and how
   - Assign reviewers
   - Add labels

4. **Code Review**
   - At least 2 approvals required
   - Address review comments
   - Update PR if needed

5. **Merge**
   - Squash and merge to develop
   - Delete feature branch

## 📝 Coding Standards

### Kotlin Conventions
```kotlin
// File naming: PascalCase
// EventListViewModel.kt, EventRepository.kt

// Class naming: PascalCase
class EventListViewModel : ViewModel()

// Function naming: camelCase
fun loadEvents() { }

// Variable naming: camelCase
val eventList = mutableListOf<Event>()

// Constants: UPPER_SNAKE_CASE
companion object {
    const val MAX_EVENTS_PER_PAGE = 20
    const val DEFAULT_TIMEOUT = 30_000L
}
```

### Package Structure
```kotlin
package com.xdien.todoevent

// Data layer
package com.xdien.todoevent.data
package com.xdien.todoevent.data.api
package com.xdien.todoevent.data.database
package com.xdien.todoevent.data.repository
package com.xdien.todoevent.data.model

// Domain layer
package com.xdien.todoevent.domain
package com.xdien.todoevent.domain.usecase
package com.xdien.todoevent.domain.model
package com.xdien.todoevent.domain.repository

// Presentation layer
package com.xdien.todoevent.presentation
package com.xdien.todoevent.presentation.ui
package com.xdien.todoevent.presentation.viewmodel
package com.xdien.todoevent.presentation.components
```

### Compose Conventions
```kotlin
// Screen composables: PascalCase
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit,
    onAddEventClick: () -> Unit
) {
    // Implementation
}

// Component composables: PascalCase
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation
}

// Preview composables: PascalCase + Preview suffix
@Preview(showBackground = true)
@Composable
fun EventCardPreview() {
    EventCard(
        event = mockEvent,
        onClick = { }
    )
}
```

### Dependency Injection
```kotlin
// Use @Inject constructor for classes
@Singleton
class EventRepository @Inject constructor(
    private val api: EventApi,
    private val dao: EventDao
) { }

// Use @Provides for interfaces
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideEventApi(retrofit: Retrofit): EventApi {
        return retrofit.create(EventApi::class.java)
    }
}
```

## 🧪 Testing Strategy

### Unit Tests
```kotlin
// Test class naming: ClassNameTest
class EventRepositoryTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var api: EventApi
    
    @Mock
    private lateinit var dao: EventDao
    
    private lateinit var repository: EventRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = EventRepository(api, dao)
    }
    
    @Test
    fun `getEvents should return cached data when offline`() = runTest {
        // Given
        val cachedEvents = listOf(createMockEvent())
        whenever(dao.getEvents()).thenReturn(flowOf(cachedEvents))
        
        // When
        val result = repository.getEvents().first()
        
        // Then
        assertEquals(cachedEvents, result)
    }
}
```

### UI Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class EventListScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun eventListScreen_displaysEvents() {
        // Given
        val events = listOf(createMockEvent())
        
        // When
        composeTestRule.setContent {
            EventListScreen(
                events = events,
                onEventClick = { },
                onAddEventClick = { }
            )
        }
        
        // Then
        composeTestRule.onNodeWithText(events[0].title).assertIsDisplayed()
    }
}
```

### Test Coverage
- **Unit Tests**: >80% coverage
- **UI Tests**: Critical user flows
- **Integration Tests**: Repository + API interactions

## 🔧 Development Environment

### Required Tools
- **Android Studio**: Arctic Fox or newer
- **JDK**: 11 or newer
- **Git**: 2.30 or newer
- **Gradle**: 8.11.1 or newer

### IDE Configuration
```xml
<!-- .editorconfig -->
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
indent_style = space
indent_size = 4
max_line_length = 120

[*.{xml,yml,yaml}]
indent_style = space
indent_size = 2
```

### Code Style
```kotlin
// ktlint configuration
android {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}
```

## 📦 Dependency Management

### Version Catalog
```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.0.21"
compose = "2024.09.00"
hilt = "2.48"
room = "2.6.1"
retrofit = "2.9.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
```

### Dependency Rules
- Use version catalog for all dependencies
- Keep dependencies up to date
- Avoid transitive dependencies
- Use specific versions, not ranges

## 🚀 Build & Deploy

### Build Variants
```kotlin
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
            buildConfigField("String", "API_BASE_URL", "\"https://api.todoevent.com/\"")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    productFlavors {
        create("mock") {
            dimension = "api"
            buildConfigField("String", "API_BASE_URL", "\"http://localhost:8080/\"")
        }
        
        create("real") {
            dimension = "api"
            buildConfigField("String", "API_BASE_URL", "\"https://api.todoevent.com/\"")
        }
    }
}
```

### CI/CD Pipeline
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

## 📊 Code Quality

### Static Analysis
```kotlin
// build.gradle.kts
plugins {
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

ktlint {
    android.set(true)
    verbose.set(true)
    filter {
        exclude { element -> element.file.path.contains("build/") }
    }
}

detekt {
    config = files("$projectDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
}
```

### Code Review Checklist
- [ ] Code follows Kotlin conventions
- [ ] Proper error handling
- [ ] Unit tests added
- [ ] Documentation updated
- [ ] No hardcoded strings
- [ ] Proper resource management
- [ ] Security considerations
- [ ] Performance impact assessed

## 🔍 Debugging

### Logging Strategy
```kotlin
object Logger {
    private const val TAG = "TodoEvent"
    
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, throwable)
    }
}
```

### Debug Tools
- **Layout Inspector**: For UI debugging
- **Network Inspector**: For API debugging
- **Database Inspector**: For Room debugging
- **Memory Profiler**: For performance debugging

## 📚 Documentation

### Code Documentation
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

### README Updates
- Update README.md for new features
- Update API documentation
- Update setup instructions
- Update troubleshooting guide

## 🚨 Security

### Code Security
- No hardcoded secrets
- Use BuildConfig for API URLs
- Validate all inputs
- Sanitize user data
- Use HTTPS for all network calls

### Data Security
- Encrypt sensitive data
- Use secure storage for tokens
- Implement proper authentication
- Follow OWASP guidelines

## 📈 Performance

### Optimization Guidelines
- Use lazy loading for images
- Implement pagination
- Cache frequently accessed data
- Minimize network calls
- Use background processing

### Performance Monitoring
```kotlin
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

## 🔄 Release Process

### Release Checklist
- [ ] All tests passing
- [ ] Code review completed
- [ ] Documentation updated
- [ ] Version bumped
- [ ] Changelog updated
- [ ] Release notes prepared
- [ ] APK signed
- [ ] Play Store submission

### Version Management
```kotlin
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### Release Notes Template
```markdown
## Version 1.0.0 (2024-01-01)

### New Features
- Event list with search and filter
- Event detail view
- Event creation and editing
- Offline support

### Bug Fixes
- Fixed crash when network is unavailable
- Resolved image loading issues

### Improvements
- Improved app performance
- Enhanced user interface
- Better error handling
```

---

## 📞 Support

- **Development Team**: dev-team@todoevent.com
- **Technical Lead**: tech-lead@todoevent.com
- **Project Manager**: pm@todoevent.com
- **Documentation**: docs@todoevent.com

## 📄 License

This development workflow is licensed under the MIT License. 