# 📦 DataStore - Guia de Configuração

Guia simples para configurar e usar DataStore no projeto UrlShortener.

## 🎯 O que é DataStore?

DataStore é a solução moderna do Android para armazenar dados de preferências de forma assíncrona e type-safe, substituindo SharedPreferences.

**Quando usar DataStore:**
- Configurações do usuário (tema, idioma)
- Preferências da aplicação
- Dados simples (strings, booleans, números)
- Cache de configurações

**Quando usar Room:**
- Dados complexos e relacionais
- Listas de URLs (como já implementado)
- Queries complexas

## ⚙️ Configuração

### 1. Dependências

Adicione ao `libs.versions.toml`:

```toml
[versions]
datastore = "1.1.1"

[libraries]
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
```

E no `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.androidx.datastore.preferences)
}
```

### 2. Implementação Básica

**data/preferences/UserPreferences.kt:**
```kotlin
package com.everpoets.urlshortener.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val SERVER_URL_KEY = stringPreferencesKey("server_url")
        private val AUTO_COPY_KEY = booleanPreferencesKey("auto_copy")
    }
    
    // Leitura
    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }
    
    val serverUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[SERVER_URL_KEY] ?: "https://url-shortener-server.onrender.com/api/"
    }
    
    val isAutoCopyEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_COPY_KEY] ?: true
    }
    
    // Escrita
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
    
    suspend fun setServerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }
    
    suspend fun setAutoCopy(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_COPY_KEY] = enabled
        }
    }
}
```

### 3. Configuração com Hilt

**di/PreferencesModule.kt:**
```kotlin
package com.everpoets.urlshortener.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
```

## 🚀 Uso Prático

### 1. No ViewModel

```kotlin
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    val isDarkMode = userPreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            userPreferences.setDarkMode(!isDarkMode.value)
        }
    }
}
```

### 2. No Compose

```kotlin
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Modo Escuro")
            Switch(
                checked = isDarkMode,
                onCheckedChange = { viewModel.toggleDarkMode() }
            )
        }
    }
}
```

### 3. Integração com ApiModule

Atualize o `ApiModule` para usar URL dinâmica:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        userPreferences: UserPreferences
    ): Retrofit {
        // Para URL dinâmica, você precisaria de uma abordagem diferente
        // ou manter a URL fixa e usar DataStore apenas para outras configs
        return Retrofit.Builder()
            .addConverterFactory(provideKotlinSerialization())
            .client(client)
            .baseUrl("https://url-shortener-server.onrender.com/api/")
            .build()
    }
}
```

## 📋 Casos de Uso no Projeto

### 1. Configurações de UI
- Tema escuro/claro
- Idioma da aplicação
- Tamanho da fonte

### 2. Comportamento da App
- Auto-copiar URLs encurtadas
- Mostrar notificações
- Limpar histórico automaticamente

### 3. Configurações de Rede
- Timeout personalizado
- Servidor alternativo
- Cache offline

## ✅ Boas Práticas

1. **Use Flows**: DataStore é assíncrono por natureza
2. **StateIn**: Converta Flows para State no ViewModel
3. **Singleton**: UserPreferences deve ser singleton
4. **Keys Centralizadas**: Mantenha as keys em companion object
5. **Valores Padrão**: Sempre forneça valores padrão
6. **Tratamento de Erro**: Use try-catch para operações de escrita

## 🔄 Migração do SharedPreferences

Se você tem SharedPreferences existentes:

```kotlin
// Migração única na primeira execução
suspend fun migrateFromSharedPreferences(context: Context) {
    val sharedPrefs = context.getSharedPreferences("old_prefs", Context.MODE_PRIVATE)
    
    dataStore.edit { preferences ->
        // Migrar valores existentes
        preferences[DARK_MODE_KEY] = sharedPrefs.getBoolean("dark_mode", false)
        preferences[AUTO_COPY_KEY] = sharedPrefs.getBoolean("auto_copy", true)
    }
    
    // Limpar SharedPreferences antigas
    sharedPrefs.edit().clear().apply()
}
```

## 🎯 Próximos Passos

1. Implementar `UserPreferences`
2. Criar `PreferencesModule`
3. Adicionar tela de configurações
4. Integrar com tema da aplicação
5. Testar persistência de dados
