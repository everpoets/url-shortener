# Guia de Implementação do Room Database

Este guia explica como adicionar e usar o Room Database no projeto UrlShortener.

## Índice
- [Configuração](#configuração)
- [Estrutura Básica](#estrutura-básica)
- [Implementação](#implementação)
- [Migrações](#migrações)
- [Boas Práticas](#boas-práticas)
- [Exemplos Práticos](#exemplos-práticos)

## Configuração

### Dependências

O projeto já possui as dependências básicas do Room configuradas:

**gradle/libs.versions.toml:**
```toml
[versions]
roomVersion = "2.7.2"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "roomVersion" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "roomVersion" }
```

**app/build.gradle.kts:**
```kotlin
plugins {
    id("androidx.room") version "2.7.2" apply false
}

dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
```

### Dependências Opcionais

Para funcionalidades adicionais, adicione ao `libs.versions.toml`:

```toml
[libraries]
# Para suporte a Kotlin Coroutines
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "roomVersion" }

# Para suporte a RxJava3
androidx-room-rxjava3 = { group = "androidx.room", name = "room-rxjava3", version.ref = "roomVersion" }

# Para testes
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "roomVersion" }
```

E no `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.androidx.room.ktx) // Para coroutines
    testImplementation(libs.androidx.room.testing) // Para testes
}
```

## Estrutura Básica

O Room possui três componentes principais:

### 1. Entity (Entidade)
Define a estrutura da tabela no banco de dados.

```kotlin
@Entity(tableName = "urls")
data class UrlEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "original_url")
    val originalUrl: String,
    
    @ColumnInfo(name = "short_url")
    val shortUrl: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "access_count", defaultValue = "0")
    val accessCount: Int = 0
)
```

### 2. DAO (Data Access Object)
Define as operações de acesso aos dados.

```kotlin
@Dao
interface UrlDao {
    @Query("SELECT * FROM urls ORDER BY created_at DESC")
    suspend fun getAllUrls(): List<UrlEntity>
    
    @Query("SELECT * FROM urls WHERE id = :id")
    suspend fun getUrlById(id: Long): UrlEntity?
    
    @Query("SELECT * FROM urls WHERE short_url = :shortUrl")
    suspend fun getUrlByShortUrl(shortUrl: String): UrlEntity?
    
    @Insert
    suspend fun insertUrl(url: UrlEntity): Long
    
    @Update
    suspend fun updateUrl(url: UrlEntity)
    
    @Delete
    suspend fun deleteUrl(url: UrlEntity)
    
    @Query("DELETE FROM urls WHERE id = :id")
    suspend fun deleteUrlById(id: Long)
    
    @Query("UPDATE urls SET access_count = access_count + 1 WHERE id = :id")
    suspend fun incrementAccessCount(id: Long)
}
```

### 3. Database
Define o banco de dados e suas configurações.

```kotlin
@Database(
    entities = [UrlEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun urlDao(): UrlDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "url_shortener_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

## Implementação

### 1. Criar as Classes

Crie os arquivos na estrutura:
```
app/src/main/java/com/everpoets/urlshortener/
├── data/
│   ├── database/
│   │   ├── entities/
│   │   │   └── UrlEntity.kt
│   │   ├── dao/
│   │   │   └── UrlDao.kt
│   │   ├── converters/
│   │   │   └── Converters.kt
│   │   └── AppDatabase.kt
│   └── repository/
│       └── UrlRepository.kt
```

### 2. Type Converters (se necessário)

Para tipos complexos como Date, List, etc:

```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

### 3. Repository Pattern

```kotlin
class UrlRepository @Inject constructor(
    private val urlDao: UrlDao
) {
    suspend fun getAllUrls(): List<UrlEntity> = urlDao.getAllUrls()
    
    suspend fun getUrlById(id: Long): UrlEntity? = urlDao.getUrlById(id)
    
    suspend fun insertUrl(url: UrlEntity): Long = urlDao.insertUrl(url)
    
    suspend fun updateUrl(url: UrlEntity) = urlDao.updateUrl(url)
    
    suspend fun deleteUrl(url: UrlEntity) = urlDao.deleteUrl(url)
    
    suspend fun incrementAccessCount(id: Long) = urlDao.incrementAccessCount(id)
}
```

### 4. Integração com Hilt

**DatabaseModule.kt:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "url_shortener_database"
        ).build()
    }
    
    @Provides
    fun provideUrlDao(database: AppDatabase): UrlDao = database.urlDao()
}
```

## Migrações

### Definindo Migrações

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE urls ADD COLUMN description TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX index_urls_short_url ON urls(short_url)")
    }
}
```

### Aplicando Migrações

```kotlin
@Database(
    entities = [UrlEntity::class],
    version = 3, // Atualizar versão
    exportSchema = true // Recomendado para produção
)
abstract class AppDatabase : RoomDatabase() {
    // ...
    
    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "url_shortener_database"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
        }
    }
}
```

## Boas Práticas

### 1. Sempre use suspend functions
```kotlin
// ✅ Correto
@Query("SELECT * FROM urls")
suspend fun getAllUrls(): List<UrlEntity>

// ❌ Evitar (bloqueia a thread principal)
@Query("SELECT * FROM urls")
fun getAllUrls(): List<UrlEntity>
```

### 2. Use Flow para observar mudanças
```kotlin
@Query("SELECT * FROM urls ORDER BY created_at DESC")
fun getAllUrlsFlow(): Flow<List<UrlEntity>>
```

### 3. Defina índices para consultas frequentes
```kotlin
@Entity(
    tableName = "urls",
    indices = [
        Index(value = ["short_url"], unique = true),
        Index(value = ["created_at"])
    ]
)
```

### 4. Use @Transaction para operações complexas
```kotlin
@Dao
interface UrlDao {
    @Transaction
    suspend fun insertUrlAndUpdateStats(url: UrlEntity, stats: StatsEntity) {
        insertUrl(url)
        updateStats(stats)
    }
}
```

### 5. Validação de Schema
```kotlin
@Database(
    entities = [UrlEntity::class],
    version = 1,
    exportSchema = true // Gera arquivos de schema para validação
)
```

## Exemplos Práticos

### Uso no ViewModel

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: UrlRepository
) : ViewModel() {
    
    private val _urls = MutableLiveData<List<UrlEntity>>()
    val urls: LiveData<List<UrlEntity>> = _urls
    
    fun loadUrls() {
        viewModelScope.launch {
            try {
                _urls.value = repository.getAllUrls()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun saveUrl(originalUrl: String, shortUrl: String) {
        viewModelScope.launch {
            try {
                val urlEntity = UrlEntity(
                    originalUrl = originalUrl,
                    shortUrl = shortUrl
                )
                repository.insertUrl(urlEntity)
                loadUrls() // Recarregar lista
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

### Testes

```kotlin
@RunWith(AndroidJUnit4::class)
class UrlDaoTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: AppDatabase
    private lateinit var urlDao: UrlDao
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        urlDao = database.urlDao()
    }
    
    @After
    fun closeDb() {
        database.close()
    }
    
    @Test
    fun insertAndGetUrl() = runTest {
        val url = UrlEntity(
            originalUrl = "https://example.com",
            shortUrl = "abc123"
        )
        
        val id = urlDao.insertUrl(url)
        val retrieved = urlDao.getUrlById(id)
        
        assertThat(retrieved?.originalUrl).isEqualTo("https://example.com")
    }
}
```

## Recursos Adicionais

- [Documentação Oficial do Room](https://developer.android.com/jetpack/androidx/releases/room)
- [Codelabs do Room](https://developer.android.com/codelabs/android-room-with-a-view-kotlin)
- [Guia de Migrações](https://developer.android.com/training/data-storage/room/migrating-db-versions)

---

**Próximos Passos:**
1. Implementar as entidades necessárias
2. Criar os DAOs com as operações específicas
3. Configurar o banco de dados
4. Integrar com o Repository pattern
5. Adicionar testes unitários
